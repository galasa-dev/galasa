/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users.internal.routes;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.ProtectedRoute;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.rbac.RBACService;

public abstract class AbstractUsersRoute extends ProtectedRoute {

    protected IAuthService authService ;
    protected IAuthStoreService authStoreService;
    protected String baseServletUrl;

    public AbstractUsersRoute(ResponseBuilder responseBuilder, String path, IAuthService authService, Environment env, RBACService rbacService) {
        super(responseBuilder,path, rbacService);
        this.authService = authService;
        this.authStoreService = authService.getAuthStoreService();

        baseServletUrl = env.getenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL);
    }


}
