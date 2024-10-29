/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.common.BaseRoute;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.QueryParameters;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;

public class AuthTokensDetailsRoute extends BaseRoute {
    private IAuthStoreService authStoreService;
    private DexGrpcClient dexGrpcClient;

    // Regex to match /auth/tokens/{tokenid} and /auth/tokens/{tokenid}/, where {tokenid}
    // is an ID that can contain only alphanumeric characters, underscores (_), and dashes (-)
    private static final String PATH_PATTERN = "\\/tokens\\/([a-zA-Z0-9\\-\\_]+)\\/?";

    public AuthTokensDetailsRoute(
        ResponseBuilder responseBuilder,
        DexGrpcClient dexGrpcClient,
        IAuthStoreService authStoreService
    ) {
        super(responseBuilder, PATH_PATTERN);
        this.dexGrpcClient = dexGrpcClient;
        this.authStoreService = authStoreService;
    }

    @Override
    public HttpServletResponse handleDeleteRequest(String pathInfo, QueryParameters queryParameters,
            HttpServletRequest request, HttpServletResponse response)
            throws FrameworkException {

        String tokenId = getTokenIdFromUrl(pathInfo);
        revokeToken(tokenId);

        String responseBody = "Successfully revoked token with ID '" + tokenId + "'";
        return getResponseBuilder().buildResponse(request, response, "text/plain", responseBody, HttpServletResponse.SC_OK);
    }

    private String getTokenIdFromUrl(String pathInfo) throws InternalServletException {
        try {
            // The URL path is '/auth/tokens/{tokenid}' so we'll grab the {tokenid} part of the path
            Matcher matcher = Pattern.compile(PATH_PATTERN).matcher(pathInfo);
            matcher.matches();
            return matcher.group(1);
        } catch (Exception ex) {
            // This should never happen since the URL's path will always contain a valid token ID
            // at this point, otherwise the route will not be matched
            ServletError error = new ServletError(GAL5065_FAILED_TO_GET_TOKEN_ID_FROM_URL);
            throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND, ex);
        }
    }

    private void revokeToken(String tokenId) throws InternalServletException {
        try {
            logger.info("Attempting to revoke token with ID '" + tokenId + "'");

            IInternalAuthToken tokenToRevoke = authStoreService.getToken(tokenId);
            if (tokenToRevoke == null) {
                ServletError error = new ServletError(GAL5066_ERROR_NO_SUCH_TOKEN_EXISTS);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            // Delete the Dex client associated with the token
            String dexClientId = tokenToRevoke.getDexClientId();
            dexGrpcClient.deleteClient(dexClientId);
            
            IInternalUser tokenOwner = tokenToRevoke.getOwner();
            String dexUserId = tokenOwner.getDexUserId();
            if (dexUserId != null) {
                // Revoke the refresh token
                dexGrpcClient.revokeRefreshToken(dexUserId, dexClientId);
            }

            // Delete the token's record in the auth store
            authStoreService.deleteToken(tokenId);

            logger.info("Revoked token with ID '" + tokenId + "' OK");
        } catch (AuthStoreException ex) {
            ServletError error = new ServletError(GAL5064_FAILED_TO_REVOKE_TOKEN);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
    }
}
