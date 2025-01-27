/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.coreos.dex.api.DexOuterClass.Client;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.authentication.internal.TokenPayloadValidator;
import dev.galasa.framework.api.beans.AuthToken;
import dev.galasa.framework.api.beans.TokenPayload;
import dev.galasa.framework.api.beans.User;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.JwtWrapper;
import dev.galasa.framework.api.common.PublicRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.InternalUser;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class AuthTokensRoute extends PublicRoute {

    private IAuthStoreService authStoreService;
    private IOidcProvider oidcProvider;
    private IDexGrpcClient dexGrpcClient;
    private Environment env;


    private static final String POST_BODY_FIELD_ID_TOKEN_KEY = "id_token";
    private static final String POST_BODY_FIELD_REFRESH_TOKEN_KEY = "refresh_token";


    // Regex to match /auth/tokens and /auth/tokens/ only
    private static final String PATH_PATTERN = "\\/tokens\\/?";

    private static final String REST_API_CLIENT = "rest-api";
    private static final String WEB_UI_CLIENT = "web-ui";

    private static final IBeanValidator<TokenPayload> validator = new TokenPayloadValidator();

    public static final String QUERY_PARAM_LOGIN_ID = "loginId";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAM_LOGIN_ID
    );

    private Log logger = LogFactory.getLog(this.getClass());
    private ITimeService timeService;
    private  RBACService rbacService;

    public AuthTokensRoute(
            ResponseBuilder responseBuilder,
            IOidcProvider oidcProvider,
            IAuthService authService,
            ITimeService timeService,
            RBACService rbacService,
            Environment env) {
        super(responseBuilder, PATH_PATTERN);
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = authService.getDexGrpcClient();
        this.authStoreService = authService.getAuthStoreService();
        this.env = env;
        this.rbacService = rbacService;

        this.timeService = timeService;
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES ;
    }

    /**
     * GET requests to /auth/tokens return all the tokens stored in the tokens
     * database, sorted by creation date order by default.
     * This endpoint takes an optional query parameter 'loginId' for e.g
     * loginId=admin
     * Passing it returns a filtered list of token records stored in the auth store
     * that matches the given login ID
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws FrameworkException {

        logger.info("handleGetRequest() entered");
        HttpServletRequest request = requestContext.getRequest();

        List<IInternalAuthToken> authTokensFromAuthStore = new ArrayList<>();

        if (queryParams.isParameterPresent(QUERY_PARAM_LOGIN_ID)) {

            String loginId = queryParams.getSingleString(QUERY_PARAM_LOGIN_ID, null);
            validateLoginId(loginId, pathInfo);
            authTokensFromAuthStore = getTokensByLoginId(loginId);

        } else {
            authTokensFromAuthStore = getAllTokens();
        }

        // Convert the token received from the auth store into the token bean that will
        // be returned as JSON
        List<AuthToken> tokensToReturn = convertAuthStoreTokenIntoTokenBeans(authTokensFromAuthStore);

        return getResponseBuilder().buildResponse(request, response, "application/json",
                getTokensAsJsonString(tokensToReturn), HttpServletResponse.SC_OK);
    }

    private List<IInternalAuthToken> getAllTokens() throws FrameworkException {

        try {
            // Retrieve all the tokens and put them into a mutable list before sorting them
            // based on their creation time
            List<IInternalAuthToken> tokens = new ArrayList<>(authStoreService.getTokens());
            Collections.sort(tokens, Comparator.comparing(IInternalAuthToken::getCreationTime));

            return tokens;

        } catch (AuthStoreException e) {
            ServletError error = new ServletError(GAL5053_FAILED_TO_RETRIEVE_TOKENS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

    }

    public List<IInternalAuthToken> getTokensByLoginId(String loginId)
            throws FrameworkException {

        logger.info("fetching access tokens by loginId");

        try {

            List<IInternalAuthToken> tokens = new ArrayList<>(authStoreService.getTokensByLoginId(loginId));
            Collections.sort(tokens, Comparator.comparing(IInternalAuthToken::getCreationTime));

            logger.info("Access tokens by loginId fetched from auth store");
            return tokens;

        } catch (AuthStoreException e) {
            ServletError error = new ServletError(GAL5053_FAILED_TO_RETRIEVE_TOKENS);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

    }

    /**
     * Sending a POST request to /auth/tokens issues a new bearer token using a
     * client ID, client secret, and refresh token.
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws ServletException, IOException, FrameworkException {

        logger.info("AuthRoute: handlePostRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        boolean isNewAccessTokenBeingCreated = false;

        // Check that the request body contains the required payload
        TokenPayload requestPayload = parseRequestBody(request, TokenPayload.class);
        validator.validate(requestPayload);

        JsonObject responseJson = new JsonObject();
        try {
            // Send a POST request to Dex's /token endpoint
            JsonObject tokenResponseBodyJson = sendTokenPost(requestPayload);

            // Return the JWT and refresh token as the servlet's response
            if (tokenResponseBodyJson != null && tokenResponseBodyJson.has(POST_BODY_FIELD_ID_TOKEN_KEY)
                    && tokenResponseBodyJson.has(POST_BODY_FIELD_REFRESH_TOKEN_KEY)) {
                logger.info("Bearer and refresh tokens successfully received from issuer.");

                String jwt = tokenResponseBodyJson.get(POST_BODY_FIELD_ID_TOKEN_KEY).getAsString();
                responseJson.addProperty("jwt", jwt);
                responseJson.addProperty(POST_BODY_FIELD_REFRESH_TOKEN_KEY, tokenResponseBodyJson.get(POST_BODY_FIELD_REFRESH_TOKEN_KEY).getAsString());

                // If we're refreshing an existing token, then we don't want to create a new
                // entry in the tokens database.
                // We only want to store tokens in the tokens database when they are created.
                String tokenDescription = requestPayload.getDescription();
                if (requestPayload.getRefreshToken() == null && tokenDescription != null) {
                    addTokenToAuthStore(requestPayload.getClientId(), jwt, tokenDescription);
                    isNewAccessTokenBeingCreated = true;
                }

                boolean isWebUiLogin = isLoggingIntoWebUI(requestPayload.getRefreshToken(), tokenDescription);
                recordUserJustLoggedIn(isWebUiLogin , jwt, this.timeService, this.env, isNewAccessTokenBeingCreated);

            } else {
                logger.info("Unable to get new bearer and refresh tokens from issuer.");

                ServletError error = new ServletError(GAL5055_FAILED_TO_GET_TOKENS_FROM_ISSUER);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (InterruptedException e) {
            logger.error("POST request to the OpenID Connect provider's token endpoint was interrupted.", e);

            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        return getResponseBuilder().buildResponse(request, response, "application/json", gson.toJson(responseJson),
                HttpServletResponse.SC_OK);
    }

    /**
     * Converts a list of authentication tokens into a JSON string
     *
     * @param tokens the tokens to convert
     * @return a JSON representation of the tokens within a "tokens" JSON array
     */
    private String getTokensAsJsonString(List<AuthToken> tokens) {
        JsonArray tokensArray = new JsonArray();
        for (AuthToken token : tokens) {
            String tokenJson = gson.toJson(token);
            tokensArray.add(JsonParser.parseString(tokenJson));
        }

        JsonObject tokensObj = new JsonObject();
        tokensObj.add("tokens", tokensArray);

        return gson.toJson(tokensObj);
    }

    /**
     * Sends a POST request to the JWT issuer's /token endpoint and returns the
     * response's body as a JSON object.
     *
     * @param requestBodyJson the request payload containing the required parameters
     *                        for the /token endpoint
     */
    private JsonObject sendTokenPost(TokenPayload requestBodyJson)
            throws IOException, InterruptedException, InternalServletException {
        String refreshToken = requestBodyJson.getRefreshToken();
        String clientId = requestBodyJson.getClientId();
        Client dexClient = dexGrpcClient.getClient(clientId);

        JsonObject response = null;
        if (dexClient != null) {
            String clientSecret = dexClient.getSecret();

            // Refresh tokens and authorization codes can be used in exchange for JWTs.
            // At this point, we either have a refresh token or an authorization code,
            // so perform the relevant POST request
            HttpResponse<String> tokenResponse = null;
            if (refreshToken != null) {
                tokenResponse = oidcProvider.sendTokenPost(clientId, clientSecret, refreshToken);
            } else {
                tokenResponse = oidcProvider.sendTokenPost(clientId, clientSecret, requestBodyJson.getCode(),
                        AuthCallbackRoute.getExternalAuthCallbackUrl());
            }

            if (tokenResponse != null) {
                response = gson.fromJson(tokenResponse.body(), JsonObject.class);
            }
        }
        return response;
    }

    /**
     * Records a new Galasa token in the auth store.
     *
     * @param clientId    the ID of the client that a user has authenticated with
     * @param jwt         the JWT that was returned after authenticating with the
     *                    client, identifying the user
     * @param description the description of the Galasa token provided by the user
     * @throws InternalServletException
     */
    private void addTokenToAuthStore(String clientId, String jwt, String description) throws InternalServletException {
        logger.info("Storing new token record in the auth store");
        JwtWrapper jwtWrapper = new JwtWrapper(jwt, env);
        IInternalUser user = new InternalUser(jwtWrapper.getUsername(), jwtWrapper.getSubject());

        try {
            authStoreService.storeToken(clientId, description, user);
        } catch (AuthStoreException e) {
            ServletError error = new ServletError(GAL5056_FAILED_TO_STORE_TOKEN_IN_AUTH_STORE, description);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
        logger.info("Stored token record in the auth store OK");
    }

    private List<AuthToken> convertAuthStoreTokenIntoTokenBeans(List<IInternalAuthToken> authStoreTokens) {

        List<AuthToken> tokensToReturn = new ArrayList<>();

        for (IInternalAuthToken token : authStoreTokens) {

            User user = new User(token.getOwner().getLoginId());
            tokensToReturn.add(new AuthToken(
                    token.getTokenId(),
                    token.getDescription(),
                    token.getCreationTime(),
                    user));
        }

        return tokensToReturn;

    }

    // This method is protected so we can unit test it easily.
    protected void recordUserJustLoggedIn(boolean isWebUI, String jwt, ITimeService timeService, Environment env, boolean isNewAccessTokenBeingCreated)
            throws InternalServletException, AuthStoreException, RBACException {

        JwtWrapper jwtWrapper = new JwtWrapper(jwt, env);
        String loginId = jwtWrapper.getUsername();
        IUser user;

        String clientName = REST_API_CLIENT;
        if (isWebUI) {
            clientName = WEB_UI_CLIENT;
        }

        user = authStoreService.getUserByLoginId(loginId);

        if (user == null) {
            String newUserRoleId = getDefaultRoleId();
            logger.info("recordUserJustLoggedIn() ; User logged in for first time. Creating a user record. Role is "+newUserRoleId);
            authStoreService.createUser(loginId, clientName, newUserRoleId);
        } else {

            // Only update the document if the user has not created a new Galasa Access Token
            // or is using the web-ui
            if(!isNewAccessTokenBeingCreated || clientName.equals(WEB_UI_CLIENT)){
                IFrontEndClient client = user.getClient(clientName);
                if (client == null) {
                    client = authStoreService.createClient(clientName);
                    user.addClient(client);
                }

                client.setLastLogin(timeService.now());
                authStoreService.updateUser(user);
            }
        }
    }

    private String getDefaultRoleId() throws AuthStoreException {
        String defaultRoleId ;
        try {
            defaultRoleId = rbacService.getDefaultRoleId();
        } catch( RBACException ex){
            throw new AuthStoreException(ex);
        }
        return defaultRoleId;
    }

    private void validateLoginId(String loginId, String servletPath) throws InternalServletException {

        if (loginId == null || loginId.trim().length() == 0) {
            ServletError error = new ServletError(GAL5067_ERROR_INVALID_LOGINID, servletPath);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private boolean isLoggingIntoWebUI(String refreshToken, String tokenDescription) {

        return (refreshToken == null && tokenDescription == null);

    }

}
