/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import java.io.IOException;
import java.net.URI;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.net.HttpHeaders;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.api.common.SupportedQueryParameterNames;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthCallbackRoute extends AbstractAuthRoute {

    // Query parameters
    public static final String QUERY_PARAMETER_CODE  = "code";
    public static final String QUERY_PARAMETER_STATE = "state";
    public static final SupportedQueryParameterNames SUPPORTED_QUERY_PARAMETER_NAMES = new SupportedQueryParameterNames(
        QUERY_PARAMETER_CODE,
        QUERY_PARAMETER_STATE
    );

    private static String externalApiServerUrl;
    private IDynamicStatusStoreService dssService;

    // Regex to match /auth/callback only
    private static final String PATH_PATTERN = "\\/callback";

    public AuthCallbackRoute(
        ResponseBuilder responseBuilder,
        String externalApiServerUrl,
        IDynamicStatusStoreService dssService
    ) {
        super(responseBuilder, PATH_PATTERN);
        this.dssService = dssService;
        AuthCallbackRoute.externalApiServerUrl = externalApiServerUrl;
    }

    /**
     * Returns the API server's external URL to this "/auth/callback" route.
     */
    public static String getExternalAuthCallbackUrl() {
        return externalApiServerUrl + "/auth/callback";
    }

    @Override
    public SupportedQueryParameterNames getSupportedQueryParameterNames() {
        return SUPPORTED_QUERY_PARAMETER_NAMES ;
    }

    /**
     * GET requests to /auth/callback are sent from Dex, and only return the
     * authorization code received during the authorization code flow, which can be
     * later used later in exchange for a JWT and a refresh token.
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpRequestContext requestContext, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("handleGetRequest() entered");
        HttpServletRequest request = requestContext.getRequest();

        String authCode = sanitizeString(queryParams.getSingleString(QUERY_PARAMETER_CODE, null));
        String state = sanitizeString(queryParams.getSingleString(QUERY_PARAMETER_STATE, null));

        if (state == null || authCode == null) {
            ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            String callbackUrlDssKey = state + DSS_CALLBACK_URL_PROPERTY_SUFFIX;
            String clientCallbackUrl = getValidatedCallbackUrl(callbackUrlDssKey);

            // We don't need the stored state anymore, so remove it from the DSS
            dssService.delete(callbackUrlDssKey);

            // If the callback URL already has query parameters, append to them
            String authCodeQuery = "code=" + authCode;
            clientCallbackUrl = appendQueryParameterToUrl(clientCallbackUrl, authCodeQuery);

            // Redirect the user back to the callback URL provided in the original /auth request
            response.addHeader(HttpHeaders.LOCATION, clientCallbackUrl);

        } catch (DynamicStatusStoreException e) {
            ServletError error = new ServletError(GAL5105_INTERNAL_DSS_ERROR);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        logger.info("handleGetRequest() exiting");
        return getResponseBuilder().buildResponse(request, response, HttpServletResponse.SC_FOUND);
    }

    private String getValidatedCallbackUrl(String callbackUrlDssKey) throws InternalServletException, DynamicStatusStoreException {
        // Make sure the state parameter is the same as the state that was previously stored in the DSS
        // using the 'dss.auth.STATEID.callback.url' property
        String clientCallbackUrl = dssService.get(callbackUrlDssKey);
        if (clientCallbackUrl == null) {
            logger.error("The provided 'state' query parameter does not match the stored state parameter");
            ServletError error = new ServletError(GAL5103_UNEXPECTED_STATE_PARAMETER_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        if (!isUrlValid(clientCallbackUrl)) {
            logger.error("The stored client callback URL is not valid");
            ServletError error = new ServletError(GAL5104_INVALID_CALLBACK_URL_PROVIDED);
            throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
        }

        logger.info("State query parameter matches previously-generated state");
        return clientCallbackUrl;
    }

    /**
     * Appends a given query parameter to a given URL and returns the resulting URL.
     */
    private String appendQueryParameterToUrl(String url, String queryParam) {
        if (URI.create(url).getQuery() != null) {
            url += "&" + queryParam;
        } else {
            url += "?" + queryParam;
        }
        return url;
    }
}
