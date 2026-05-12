/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import static dev.galasa.extensions.common.Errors.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import dev.galasa.auth.couchdb.internal.beans.AuthDBNameViewDesign;
import dev.galasa.auth.couchdb.internal.beans.FrontEndClient;
import dev.galasa.auth.couchdb.internal.beans.UserDoc;
import dev.galasa.extensions.common.api.HttpClientFactory;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbStore;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.extensions.common.couchdb.pojos.IdRev;
import dev.galasa.extensions.common.couchdb.pojos.PutPostResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewResponse;
import dev.galasa.extensions.common.couchdb.pojos.ViewRow;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;

/**
 * When CouchDB is being used to store user-related information, including
 * information
 * about authentication tokens (but not the tokens themselves), this class is
 * called
 * upon to implement the auth store.
 *
 * This class registers the auth store as the only auth store in the framework,
 * and is
 * only used when Galasa is running in an ecosystem. It gets all of its data
 * from a
 * CouchDB server.
 */
public class CouchdbAuthStore extends CouchdbStore implements IAuthStore {

    public static final String TOKENS_DATABASE_NAME = "galasa_tokens";
    public static final String USERS_DATABASE_NAME = "galasa_users";
    public static final String COUCHDB_AUTH_ENV_VAR = "GALASA_AUTHSTORE_TOKEN";
    public static final String COUCHDB_AUTH_TYPE = "Basic";

    public static final String TOKENS_DB_VIEW_NAME = "loginId-view";
    public static final String USERS_DB_VIEW_NAME = "loginId-view";

    private Log logger;

    public static final int DEFAULT_TOKEN_LIFESPAN_DAYS = 90;

    private ITimeService timeService;

    public CouchdbAuthStore(
            URI authStoreUri,
            HttpClientFactory httpClientFactory,
            HttpRequestFactory requestFactory,
            LogFactory logFactory,
            CouchdbValidator validator,
            ITimeService timeService) throws CouchdbException {
        super(authStoreUri, requestFactory, httpClientFactory);
        this.logger = logFactory.getLog(getClass());
        this.timeService = timeService;

        validator.checkCouchdbDatabaseIsValid(this.storeUri, this.httpClient, this.httpRequestFactory, timeService);

        // One-off migration: Add expiry times to legacy tokens that don't have them
        // This could be removed after Galasa 1.0.0 is released
        // Note: Migration failures are logged but don't prevent auth store initialisation
        try {
            migrateLegacyTokens();
        } catch (Exception e) {
            logger.warn("Token expiry migration encountered an error but auth store initialisation will continue", e);
        }
    }

    /**
     * One-off initialisation method that adds expiry times to legacy tokens that
     * don't have them. This migration runs during auth store initialisation to 
     * ensure all tokens have expiry times set. If the migration fails, it logs 
     * the error but does not prevent the auth store from initialising.
     */
    private void migrateLegacyTokens() {
        logger.info("Starting token expiry migration during auth store initialisation");

        int tokensProcessed = 0;
        int tokensMigrated = 0;
        int tokensFailed = 0;

        List<IUser> users = null;
        try {
            users = getAllUsers();
        } catch (AuthStoreException e) {
            logger.warn("Failed to get users during token migration: " + e.getMessage());
        }

        // If there are no users, skip migration
        if (users == null || users.isEmpty()) {
            logger.info("No users found, skipping token expiry migration");
            return;
        }

        // For each user, get their tokens
        for (IUser user : users) {
            String loginId = user.getLoginId();
            String userNumber = user.getUserNumber();

            List<IInternalAuthToken> tokens = null;
            try {
                tokens = getTokensByLoginId(loginId);
            } catch (Exception e) {
                logger.warn("Failed to retrieve tokens for user " + userNumber
                        + ", skipping user and continuing with others: " + e.getMessage());
                continue;
            }

            for (IInternalAuthToken token : tokens) {
                tokensProcessed++;

                if (token.getExpiryTime() == null) {
                    // Calculate expiry time: current time + constant amount of days
                    Instant newExpiryTime = timeService.now().plus(DEFAULT_TOKEN_LIFESPAN_DAYS, ChronoUnit.DAYS);
                    logger.info("Setting expiry time to: " + newExpiryTime);

                    // Update the token with the new expiry time
                    try {
                        updateTokenWithExpiryTime(token.getTokenId(), newExpiryTime);
                    } catch (AuthStoreException e) {
                        tokensFailed++;
                        logger.warn("Failed to migrate token " + token.getTokenId() + " for user " + userNumber
                                + ": " + e.getMessage());
                        continue;
                    }
                    tokensMigrated++;

                    logger.info("Successfully migrated token " + token.getTokenId());
                }
            }
        }

        logger.info("Token expiry migration complete. Processed " + tokensProcessed + " tokens, migrated "
                + tokensMigrated + " tokens, " + tokensFailed + " tokens failed");
    }

    /**
     * Internal method to update a token's expiry time. This is used only during
     * the migration process and is not exposed via the IAuthStore interface.
     */
    private void updateTokenWithExpiryTime(String tokenId, Instant expiryTime) throws AuthStoreException {
        try {
            // Get the existing token document
            CouchdbAuthToken existingToken = getDocumentFromDatabase(TOKENS_DATABASE_NAME, tokenId,
                    CouchdbAuthToken.class);

            if (existingToken == null) {
                String errorMessage = ERROR_TOKEN_NOT_FOUND.getMessage("Token not found: " + tokenId);
                throw new AuthStoreException(errorMessage);
            }

            // Create an updated token with the new expiry time
            CouchdbAuthToken updatedToken = new CouchdbAuthToken(
                    tokenId,
                    existingToken.getDexClientId(),
                    existingToken.getDescription(),
                    existingToken.getCreationTime(),
                    expiryTime,
                    (CouchdbUser) existingToken.getOwner());

            // Convert to JSON and update the document
            String tokenJson = gson.toJson(updatedToken);

            // Get the document revision for the update
            IdRev tokenIdRev = getDocumentFromDatabase(TOKENS_DATABASE_NAME, tokenId, IdRev.class);
            if (tokenIdRev == null || tokenIdRev._rev == null) {
                String errorMessage = ERROR_FAILED_TO_RETRIEVE_TOKENS
                        .getMessage("Could not get revision for token: " + tokenId);
                throw new AuthStoreException(errorMessage);
            }

            // Build and send the update request
            HttpPut request = httpRequestFactory
                    .getHttpPutRequest(storeUri + "/" + TOKENS_DATABASE_NAME + "/" + tokenId);
            request.setHeader("If-Match", tokenIdRev._rev);
            request.setEntity(new StringEntity(tokenJson, StandardCharsets.UTF_8));

            String responseEntity = sendHttpRequest(request, HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED);
            PutPostResponse putResponse = gson.fromJson(responseEntity, PutPostResponse.class);

            if (!putResponse.ok || putResponse.id == null || putResponse.rev == null) {
                String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INVALID_RESP.getMessage();
                throw new AuthStoreException(errorMessage);
            }
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_TOKENS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
    }

    @Override
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        logger.info("Retrieving tokens from CouchDB");
        List<ViewRow> tokenDocuments = new ArrayList<>();
        List<IInternalAuthToken> tokens = new ArrayList<>();

        try {
            // Get all of the documents in the tokens database
            tokenDocuments = getAllDocsFromDatabase(TOKENS_DATABASE_NAME);

            // Build up a list of all the tokens using the document IDs
            for (ViewRow row : tokenDocuments) {
                tokens.add(getAuthTokenFromDocument(row.key));
            }

            logger.info("Tokens retrieved from CouchDB OK");
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_TOKENS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
        return tokens;
    }

    public List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException {
        logger.info("Retrieving tokens from CouchDB");
        List<ViewRow> tokenDocuments = new ArrayList<>();
        List<IInternalAuthToken> tokens = new ArrayList<>();

        // Token view emits loginIds in lowercase so we should search by lowercase.
        String lowercaseLoginId = loginId.toLowerCase();

        try {
            // Get all of the documents in the tokens database
            tokenDocuments = getAllDocsByLoginId(TOKENS_DATABASE_NAME, lowercaseLoginId, TOKENS_DB_VIEW_NAME);

            // Build up a list of all the tokens using the document IDs
            for (ViewRow row : tokenDocuments) {
                tokens.add(getAuthTokenFromDocument(row.id));
            }

            logger.info("Tokens retrieved from CouchDB OK");
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_TOKENS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
        return tokens;
    }

    @Override
    public void shutdown() throws AuthStoreException {
        try {
            httpClient.close();
        } catch (IOException e) {
            String errorMessage = ERROR_GALASA_AUTH_STORE_SHUTDOWN_FAILED.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
    }

    @Override
    public void storeToken(String clientId, String description, IInternalUser owner, int tokenLifespanDays)
            throws AuthStoreException {
        // Create the JSON payload representing the token to store
        CouchdbUser couchdbUser = new CouchdbUser(owner);
        Instant now = timeService.now();
        Instant expiryTime = now.plus(tokenLifespanDays, ChronoUnit.DAYS);
        String tokenJson = gson.toJson(new CouchdbAuthToken(clientId, description, now, expiryTime, couchdbUser));

        try {
            createDocument(TOKENS_DATABASE_NAME, tokenJson);
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_CREATE_TOKEN_DOCUMENT.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        try {
            deleteDocumentFromDatabase(TOKENS_DATABASE_NAME, tokenId);
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_DELETE_TOKEN_DOCUMENT.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
    }

    /**
     * Gets an auth token from a CouchDB document with the given document ID.
     * The document is assumed to be within the tokens database in the CouchDB
     * server.
     *
     * @param documentId the ID of the document containing the details of an auth
     *                   token
     * @return the auth token stored within the given document
     * @throws AuthStoreException if there was a problem accessing the auth store or
     *                            its response
     */
    private IInternalAuthToken getAuthTokenFromDocument(String documentId) throws CouchdbException {
        return getDocumentFromDatabase(TOKENS_DATABASE_NAME, documentId, CouchdbAuthToken.class);
    }

    @Override
    public IInternalAuthToken getTokenByDexClientId(String clientId) throws AuthStoreException {
        logger.info("Retrieving token by Dex client ID from CouchDB");

        IInternalAuthToken result = null;
        try {
            // Get all tokens and search for one with matching clientId
            List<IInternalAuthToken> allTokens = getTokens();

            for (IInternalAuthToken token : allTokens) {
                if (clientId.equals(token.getDexClientId())) {
                    result = token;
                    break;
                }
            }

        } catch (AuthStoreException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_TOKENS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }

        return result;
    }

    @Override
    public List<IUser> getAllUsers() throws AuthStoreException {
        logger.info("Retrieving all users from couchdb");

        List<ViewRow> userDocuments = new ArrayList<>();
        List<IUser> users = new ArrayList<>();

        try {
            userDocuments = getAllDocsFromDatabase(USERS_DATABASE_NAME);

            for (ViewRow row : userDocuments) {
                UserDoc couchdbUserDocBean = getUserFromDocument(row.id);
                UserImpl wrappedUser = new UserImpl(couchdbUserDocBean);
                users.add(wrappedUser);
            }

            logger.info("Users retrieved from CouchDB OK");

        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_USERS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }

        return users;
    }

    @Override
    public void createUser(String loginId, String clientName, String roleId) throws AuthStoreException {

        logger.info("createUser: Creating user loginId:" + loginId + " clientName:" + clientName + " roleId:" + roleId);
        FrontEndClient client = new FrontEndClient();

        client.setClientName(clientName);
        client.setLastLogin(Instant.now());

        String userJson = gson.toJson(new UserDoc(loginId, List.of(client), roleId));

        try {
            createDocument(USERS_DATABASE_NAME, userJson);
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_CREATE_USER_DOCUMENT.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }

    }

    @Override
    public IUser getUserByLoginId(String loginId) throws AuthStoreException {
        logger.info("Retrieving user by loginId (case-insensitive) from CouchDB");
        List<ViewRow> userDocument;
        IUser user = null;

        try {
            // Convert the loginId to lowercase for the lookup
            String lowerCaseLoginId = loginId.toLowerCase();
            
            // Fetch documents matching the lowercase loginId using the lowercase view
            userDocument = getAllDocsByLoginId(USERS_DATABASE_NAME, lowerCaseLoginId, USERS_DB_VIEW_NAME);

            // Since loginIds are unique (case-insensitive), there should be only one document.
            if (userDocument != null && !userDocument.isEmpty()) {
                ViewRow row = userDocument.get(0); // Get the first entry since loginId is unique

                // Fetch the user document from the CouchDB using the ID from the row
                UserDoc fetchedUser = getUserFromDocument(row.id);

                if (row.value != null) {
                    AuthDBNameViewDesign nameViewDesign = gson.fromJson(gson.toJson(row.value),
                            AuthDBNameViewDesign.class);
                    fetchedUser.setVersion(nameViewDesign._rev); // Set the version from the CouchDB rev
                }

                // Assign fetchedUser to the user variable
                // The UserImpl will contain the actual case-accurate loginId from the database
                user = new UserImpl(fetchedUser);
            }

            logger.info("User retrieved from CouchDB OK (case-insensitive lookup)");

        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_RETRIEVE_USERS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }

        return user;
    }

    @Override
    public IUser updateUser(IUser user) throws AuthStoreException {
        // Take a clone of the user we are passed, so we can guarantee we are using our
        // bean which serialises to the correct format.
        UserImpl userImpl = new UserImpl(user);
        updateUser(httpClient, storeUri, userImpl);
        return userImpl;
    }

    /**
     * Sends a GET request to CouchDB's
     * /{db}/_design/docs/_view/loginId-view?key={loginId} endpoint and returns the
     * "rows" list in the response,
     * which corresponds to the list of documents within the given database.
     *
     * @param dbName  the name of the database to retrieve the documents of
     * @param loginId the loginId of the user to retrieve the doucemnts of
     * @return a list of rows corresponding to documents within the database
     * @throws CouchdbException if there was a problem accessing the
     *                          CouchDB store or its response
     */
    protected List<ViewRow> getAllDocsByLoginId(String dbName, String loginId, String viewName)
            throws CouchdbException {

        String encodedLoginId = URLEncoder.encode("\"" + loginId + "\"", StandardCharsets.UTF_8);
        String url = storeUri + "/" + dbName + "/_design/docs/_view/" + viewName + "?key=" + encodedLoginId;

        HttpGet getDocs = httpRequestFactory.getHttpGetRequest(url);
        getDocs.addHeader("Content-Type", "application/json");

        String responseEntity = sendHttpRequest(getDocs, HttpStatus.SC_OK);

        ViewResponse docByLoginId = gson.fromJson(responseEntity, ViewResponse.class);
        List<ViewRow> viewRows = docByLoginId.rows;

        if (viewRows == null) {
            String errorMessage = ERROR_FAILED_TO_GET_DOCUMENTS_FROM_DATABASE.getMessage(dbName);
            throw new CouchdbException(errorMessage);
        }

        return viewRows;
    }

    private void updateUser(CloseableHttpClient httpClient, URI couchdbUri, UserImpl user)
            throws AuthStoreException {

        user.validate();

        HttpEntityEnclosingRequestBase request = buildUpdateUserDocRequest(user, couchdbUri);

        PutPostResponse putResponse = sendPutRequestToCouchDb(request);

        validateCouchdbResponseJson(user.getUserNumber(), putResponse);

        // The version of the document in couchdb has updated, so lets update our
        // version in the doc we were sent,
        // so that the same document could be used for another update.
        user.setVersion(putResponse.rev);
    }

    private PutPostResponse sendPutRequestToCouchDb(HttpEntityEnclosingRequestBase request) throws AuthStoreException {
        PutPostResponse putResponse;
        try {
            String entity = sendHttpRequest(request, HttpStatus.SC_CREATED);
            putResponse = gson.fromJson(entity, PutPostResponse.class);

        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }
        return putResponse;
    }

    private HttpPut buildUpdateUserDocRequest(UserImpl user, URI couchdbUri) {
        HttpPut request;

        String jsonStructure = user.toJson(gson);
        request = httpRequestFactory
                .getHttpPutRequest(couchdbUri + "/" + USERS_DATABASE_NAME + "/" + user.getUserNumber());
        request.setHeader("If-Match", user.getVersion());

        request.setEntity(new StringEntity(jsonStructure, StandardCharsets.UTF_8));
        return request;
    }

    private void validateCouchdbResponseJson(String expectedUserNumber, PutPostResponse putResponse)
            throws AuthStoreException {
        if (putResponse.id == null || putResponse.rev == null) {
            String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_INVALID_RESP.getMessage();
            throw new AuthStoreException(errorMessage);
        }
        if (!expectedUserNumber.equals(putResponse.id)) {
            String errorMessage = ERROR_FAILED_TO_UPDATE_USER_DOCUMENT_MISMATCH_DOC_ID.getMessage();
            throw new AuthStoreException(errorMessage);
        }
    }

    @Override
    public IUser getUser(String userNumber) throws AuthStoreException {

        try {

            logger.info("Retrieving user from CouchDB");

            IUser user = null;
            UserDoc userGotBack = getUserFromDocument(userNumber);

            if (userGotBack != null) {
                user = new UserImpl(userGotBack);
            }

            logger.info("User fecthed from CouchDB");
            return user;

        } catch (CouchdbException e) {

            String errorMessage = ERROR_FAILED_TO_RETRIEVE_USERS.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);

        }

    }

    @Override
    public void deleteUser(IUser user) throws AuthStoreException {

        try {
            deleteDocumentFromDatabase(USERS_DATABASE_NAME, user.getUserNumber());
        } catch (CouchdbException e) {
            String errorMessage = ERROR_FAILED_TO_DELETE_USER_DOCUMENT.getMessage(e.getMessage());
            throw new AuthStoreException(errorMessage, e);
        }

    }

    @Override
    public IFrontEndClient createClient(String clientName) {
        return new FrontEndClient(clientName, timeService.now());
    }

    /**
     * Gets a user from a CouchDB document with the given document ID.
     * The document is assumed to be within the users database in the CouchDB
     * server.
     *
     * @param documentId the ID of the document containing the details of a user
     * @return the user stored within the given document
     * @throws UsersStoreException if there was a problem accessing the users store
     *                             or its response
     */
    private UserDoc getUserFromDocument(String documentId) throws CouchdbException {
        return getDocumentFromDatabase(USERS_DATABASE_NAME, documentId, UserDoc.class);
    }

}
