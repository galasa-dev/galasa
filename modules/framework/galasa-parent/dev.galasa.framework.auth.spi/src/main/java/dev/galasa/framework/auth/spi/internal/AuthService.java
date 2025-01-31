/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi.internal;

import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5064_FAILED_TO_REVOKE_TOKEN;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5066_ERROR_NO_SUCH_TOKEN_EXISTS;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5125_ACTION_NOT_PERMITTED;
import static dev.galasa.framework.api.common.ServletErrorMessage.GAL5126_INTERNAL_RBAC_ERROR;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

public class AuthService implements IAuthService {

    private IAuthStoreService authStoreService;
    private IDexGrpcClient dexGrpcClient;
    private RBACService rbacService;

    private final Log logger = LogFactory.getLog(getClass());

    public AuthService(IAuthStoreService authStoreService, IDexGrpcClient dexGrpcClient, RBACService rbacService) {
        this.authStoreService = authStoreService;
        this.dexGrpcClient = dexGrpcClient;
        this.rbacService = rbacService;
    }

    @Override
    public void revokeToken(String tokenId, String requestingUserLoginId) throws InternalServletException {
        try {
            logger.info("Attempting to revoke token with ID '" + tokenId + "'");

            IInternalAuthToken tokenToRevoke = authStoreService.getToken(tokenId);
            if (tokenToRevoke == null) {
                ServletError error = new ServletError(GAL5066_ERROR_NO_SUCH_TOKEN_EXISTS);
                throw new InternalServletException(error, HttpServletResponse.SC_NOT_FOUND);
            }

            validateRequestingUserIsPermittedToDeleteToken(tokenToRevoke, requestingUserLoginId);

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

    private void validateRequestingUserIsPermittedToDeleteToken(IInternalAuthToken tokenToRevoke, String requestingUserLoginId) throws InternalServletException { 
        IInternalUser user = tokenToRevoke.getOwner();
        if( user != null) {
            String tokenOwnerLoginId = user.getLoginId();
            if( tokenOwnerLoginId!=null) {
                if( requestingUserLoginId != null) {
                    if( !requestingUserLoginId.equals(tokenOwnerLoginId)) {
                        // The user is not deleting their own token, they are deleting someone else's
                        String usersEditOtherActionId = BuiltInAction.USER_EDIT_OTHER.getAction().getId();
                        try {
                            if (!rbacService.isActionPermitted(requestingUserLoginId,usersEditOtherActionId)) {
                                // Oh no! The current user is not allowed to delete this token.
                                ServletError error = new ServletError(GAL5125_ACTION_NOT_PERMITTED, usersEditOtherActionId);
                                throw new InternalServletException(error, HttpServletResponse.SC_FORBIDDEN);
                            }
                        } catch(RBACException ex) {
                            // Internal problem with the RBAC service.
                            ServletError error = new ServletError(GAL5126_INTERNAL_RBAC_ERROR);
                            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
                        }
                    }
                }
            }
        }
    }

    @Override
    public IDexGrpcClient getDexGrpcClient() {
        return dexGrpcClient;
    }

    @Override
    public IAuthStoreService getAuthStoreService() {
        return authStoreService;
    }
}
