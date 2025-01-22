/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.common.HttpRequestContext;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.rbac.RBACService;

public class AuthTokensDetailsRoute extends ProtectedRoute {
    private IAuthService authService;

    // Regex to match /auth/tokens/{tokenid} and /auth/tokens/{tokenid}/, where {tokenid}
    // is an ID that can contain only alphanumeric characters, underscores (_), and dashes (-)
    private static final String PATH_PATTERN = "\\/tokens\\/([a-zA-Z0-9\\-\\_]+)\\/?";

    public AuthTokensDetailsRoute(
        ResponseBuilder responseBuilder,
        IAuthService authService,
        RBACService rbacService
    ) {
        super(responseBuilder, PATH_PATTERN, rbacService);
        this.authService = authService;
    }

    @Override
    public HttpServletResponse handleDeleteRequest(String pathInfo,
            HttpRequestContext requestContext, HttpServletResponse response)
            throws FrameworkException {

        HttpServletRequest request = requestContext.getRequest();

        String tokenId = getTokenIdFromUrl(pathInfo);
        authService.revokeToken(tokenId);

        String responseBody = "Successfully revoked token with ID '" + tokenId + "'";
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_OK);
    }

    private String getTokenIdFromUrl(String pathInfo) throws InternalServletException {
        try {
            // The URL path is '/auth/tokens/{tokenid}' so we'll grab the {tokenid} part of the path
            Matcher matcher = this.getPathRegex().matcher(pathInfo);
            matcher.matches();
            return matcher.group(1);
        } catch (Exception ex) {
            // This should never happen since the URL's path will always contain a valid token ID
            // at this point, otherwise the route will not be matched
            ServletError error = new ServletError(GAL5065_FAILED_TO_GET_TOKEN_ID_FROM_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, ex);
        }
    }
}
