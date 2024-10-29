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
import javax.servlet.http.HttpSession;

import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

public class AuthCallbackRoute extends BaseRoute {

    private static String externalApiServerUrl;

    // Regex to match /auth/callback only
    private static final String PATH_PATTERN = "\\/callback";

    public AuthCallbackRoute(ResponseBuilder responseBuilder, String externalApiServerUrl) {
        super(responseBuilder, PATH_PATTERN);
        AuthCallbackRoute.externalApiServerUrl = externalApiServerUrl;
    }

    /**
     * Returns the API server's external URL to this "/auth/callback" route.
     */
    public static String getExternalAuthCallbackUrl() {
        return externalApiServerUrl + "/auth/callback";
    }

    /**
     * GET requests to /auth/callback are sent from Dex, and only return the
     * authorization code received during the authorization code flow, which can be
     * later used later in exchange for a JWT and a refresh token.
     */
    @Override
    public HttpServletResponse handleGetRequest(String pathInfo, QueryParameters queryParams,
            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, FrameworkException {

        logger.info("handleGetRequest() entered");

        String authCode = queryParams.getSingleString("code", null);
        String state = queryParams.getSingleString("state", null);

        if (state != null && authCode != null) {

            // Make sure the state parameter is the same as the state that was previously stored in the session
            HttpSession session = request.getSession();
            if (isStateParameterValid(session, state)) {
                logger.info("State query parameter matches previously-generated state");

                String clientCallbackUrl = (String) session.getAttribute("callbackUrl");
                if (clientCallbackUrl != null) {

                    // If the callback URL already has query parameters, append to them
                    String authCodeQuery = "code=" + authCode;
                    clientCallbackUrl = appendQueryParameterToUrl(clientCallbackUrl, authCodeQuery);

                    // We don't need the session anymore, so invalidate it
                    session.invalidate();

                    // Redirect the user back to the callback URL provided in the original /auth request
                    response.addHeader("Location", clientCallbackUrl);
                    return getResponseBuilder().buildResponse(request, response, null, null,
                            HttpServletResponse.SC_FOUND);
                } else {
                    logger.error("Unable to redirect back to the client application (failed to retrieve callback URL from session)");
                }
            } else {
                logger.error("The provided 'state' query parameter does not match the state parameter stored in the session");
            }
        }
        ServletError error = new ServletError(GAL5400_BAD_REQUEST, request.getServletPath());
        throw new InternalServletException(error, HttpServletResponse.SC_BAD_REQUEST);
    }

    /**
     * Checks whether the provided state parameter matches the state parameter that was stored
     * when initiating an authentication flow.
     */
    private boolean isStateParameterValid(HttpSession session, String state) {
        String storedState = (String) session.getAttribute("state");
        return (storedState != null && state.equals(storedState));
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
