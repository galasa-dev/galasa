/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import dev.galasa.framework.spi.rbac.BuiltInAction;

/**
 * This is an abstract class that represents a route that is publicly accessible and does not require a JWT.
 * No RBAC limits can be assigned to public routes since requests will not contain an 'Authorization' header.
 */
public abstract class PublicRoute extends BaseRoute {

    public PublicRoute(ResponseBuilder responseBuilder, String path) {
        super(responseBuilder, path);
    }

    @Override
    public boolean isActionPermitted(BuiltInAction action, String loginId) throws InternalServletException {
        return true;
    }
}
