/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.coreos.dex.api.DexOuterClass.Client;
import com.google.common.net.HttpHeaders;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.IOidcProvider;
import dev.galasa.framework.api.common.JwtWrapper;
import dev.galasa.framework.api.authentication.internal.TokenPayloadValidator;
import dev.galasa.framework.api.beans.TokenPayload;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.IBeanValidator;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.InternalUser;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalUser;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthRoute extends AbstractAuthRoute {

    // Query parameters
    public static final String QUERY_PARAMETER_CLIENT_ID = "client_id";
    public static final String QUERY_PARAMETER_CALLBACK_URL = "callback_url";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_CLIENT_ID,
        QUERY_PARAMETER_CALLBACK_URL
    );

    // Fields in a payload of a POST request we parse for meaning.
    private static final String JSON_FIELD_ID_TOKEN_KEY      = "id_token";
    private static final String JSON_FIELD_REFRESH_TOKEN_KEY = "refresh_token";

    // Regex to match endpoint /auth and /auth/
    private static final String PATH_PATTERN = "\\/?";

    // Allow auth-related DSS properties to live for 10 minutes before being deleted from the DSS
    private static final long AUTH_DSS_STATE_EXPIRY_SECONDS = 10 * 60;

    private static final IBeanValidator<TokenPayload> validator = new TokenPayloadValidator();

    private IAuthStoreService authStoreService;
    private IOidcProvider oidcProvider;
    private IDexGrpcClient dexGrpcClient;
    private Environment env;
    private IDynamicStatusStoreService dssService;

    public AuthRoute(
        ResponseBuilder responseBuilder,
        IOidcProvider oidcProvider,
        IAuthService authService,
        Environment env,
        IDynamicStatusStoreService dssService
    ) {
        super(responseBuilder, PATH_PATTERN);
        this.oidcProvider = oidcProvider;
        this.dexGrpcClient = authService.getDexGrpcClient();
        this.authStoreService = authService.getAuthStoreService();
        this.env = env;
        this.dssService = dssService;
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES ;
    }

    /**
     * Sending a GET request to /auth redirects to the OpenID Connect provider's
     * authorization endpoint to authenticate a user.
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("AuthRoute: handleGetRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        try {
            String clientId = sanitizeString(queryParams.getSingleString(QUERY_PARAMETER_CLIENT_ID, null));
            String clientCallbackUrl = sanitizeString(queryParams.getSingleString(QUERY_PARAMETER_CALLBACK_URL, null));

            // Make sure the required query parameters exist
            if (clientId == null || clientCallbackUrl == null || !isUrlValid(clientCallbackUrl)) {
                ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
                throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
            }

            String stateId = generateStateId(request.getRemoteAddr());

            // Get the redirect URL to the upstream connector and add it to the response's "Location" header
            String authUrl = oidcProvider.getConnectorRedirectUrl(clientId, AuthCallbackRoute.getExternalAuthCallbackUrl(), stateId);
            if (authUrl != null) {
                logger.info("Redirect URL to upstream connector received: " + authUrl);
                response.addHeader(HttpHeaders.LOCATION, authUrl);

                // Store the callback URL in the DSS to redirect to at the end of the authentication process
                dssService.put(stateId + DSS_CALLBACK_URL_PROPERTY_SUFFIX, clientCallbackUrl, AUTH_DSS_STATE_EXPIRY_SECONDS);

            } else {
                ServletError error = new ServletError(GAL5054_FAILED_TO_GET_CONNECTOR_URL);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (InterruptedException e) {
            logger.error("GET request to the OpenID Connect provider's authorization endpoint was interrupted.", e);

            ServletError error = new ServletError(GAL5000_GENERIC_API_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }

        return getResponseBuilder().buildResponse(request, response, null, null, HttpServletResponse.SC_FOUND);
    }

    /**
     * Sending a POST request to /auth issues a new bearer token using the provided
     * client ID, client secret, and refresh token.
     */
    @Override
    public HttpServletResponse handlePostRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("AuthRoute: handlePostRequest() entered.");
        HttpServletRequest request = requestContext.getRequest();

        // Check that the request body contains the required payload
        TokenPayload requestPayload = parseRequestBody(request, TokenPayload.class);
        validator.validate(requestPayload);

        JsonObject responseJson = new JsonObject();
        try {
            // Send a POST request to Dex's /token endpoint
            JsonObject tokenResponseBodyJson = sendTokenPost(requestPayload);

            // Return the JWT and refresh token as the servlet's response
            if (tokenResponseBodyJson != null && tokenResponseBodyJson.has(JSON_FIELD_ID_TOKEN_KEY) && tokenResponseBodyJson.has(JSON_FIELD_REFRESH_TOKEN_KEY)) {
                logger.info("Bearer and refresh tokens successfully received from issuer.");

                String jwt = tokenResponseBodyJson.get(JSON_FIELD_ID_TOKEN_KEY).getAsString();
                responseJson.addProperty("jwt", jwt);
                responseJson.addProperty(JSON_FIELD_REFRESH_TOKEN_KEY, tokenResponseBodyJson.get(JSON_FIELD_REFRESH_TOKEN_KEY).getAsString());

                // If we're refreshing an existing token, then we don't want to create a new entry in the tokens database.
                // We only want to store tokens in the tokens database when they are created.
                String tokenDescription = requestPayload.getDescription();
                if (requestPayload.getRefreshToken() == null && tokenDescription != null) {
                    addTokenToAuthStore(requestPayload.getClientId(), jwt, tokenDescription);
                }

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

        return getResponseBuilder().buildResponse(request, response, "application/json", gson.toJson(responseJson), HttpServletResponse.SC_OK);
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
                tokenResponse = oidcProvider.sendTokenPost(clientId, clientSecret, requestBodyJson.getCode(), AuthCallbackRoute.getExternalAuthCallbackUrl());
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
     * @param clientId the ID of the client that a user has authenticated with
     * @param jwt the JWT that was returned after authenticating with the client, identifying the user
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

    // Creates a random ID to identify an auth request
    private String generateStateId(String clientIp) {
        String stateId = UUID.randomUUID().toString();
        if (clientIp != null && !clientIp.isBlank()) {
            stateId += "-" + clientIp;
        }
        return stateId;
    }
}
