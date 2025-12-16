/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACService;

/**
 * This is an abstract class that represents a route protected under a JWT challenge and RBAC.
 * Requests to protected routes will always contain an 'Authorization' header with a bearer token. 
 */
public abstract class ProtectedRoute extends BaseRoute {

    protected RBACService rbacService;
    protected RBACValidator rbacValidator;

    public ProtectedRoute(
        ResponseBuilder responseBuilder,
        String path,
        RBACService rbacService
    ) {
        super(responseBuilder, path);
        this.rbacService = rbacService;
        this.rbacValidator = new RBACValidator(rbacService);
    }

    protected void validateActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
        logger.info("Checking to make sure user "+loginId+" has action "+action.getAction().getId());
        rbacValidator.validateActionPermitted(action, loginId);
    }

    @Override
    public boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
        return rbacValidator.isActionPermitted(action, loginId);
    }
}
