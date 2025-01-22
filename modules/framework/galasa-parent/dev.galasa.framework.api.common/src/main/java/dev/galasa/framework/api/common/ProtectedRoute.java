/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

/**
 * This is an abstract class that represents a route protected under a JWT challenge and RBAC.
 * Requests to protected routes will always contain an 'Authorization' header with a bearer token. 
 */
public abstract class ProtectedRoute extends BaseRoute {

    protected RBACService rbacService;
    protected Environment env;

    public ProtectedRoute(
        ResponseBuilder responseBuilder,
        String path,
        RBACService rbacService,
        Environment env
    ) {
        super(responseBuilder, path);
        this.rbacService = rbacService;
        this.env = env;
    }

    protected void validateActionPermitted(BuiltInAction action, HttpServletRequest request) throws InternalServletException {
        if (!isActionPermitted(action, request)) {
            ServletError error = new ServletError(GAL5125_ACTION_NOT_PERMITTED, action.getAction().getId());
            throw new InternalServletException(error, HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public boolean isActionPermitted(BuiltInAction action, HttpServletRequest request) throws InternalServletException {
        boolean isActionPermitted = false;
        String jwt = JwtWrapper.getBearerTokenFromAuthHeader(request);
        if (jwt != null) {
            try {
                String loginId = new JwtWrapper(jwt, env).getUsername();
                isActionPermitted = rbacService.isActionPermitted(loginId, action.getAction().getId());
            } catch (RBACException e) {
                ServletError error = new ServletError(GAL5126_INTERNAL_RBAC_ERROR);
                throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
        return isActionPermitted;
    }
}
