/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.users;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.users.internal.routes.UserRoute;
import dev.galasa.framework.api.users.internal.routes.UsersRoute;
import dev.galasa.framework.auth.spi.AuthServiceFactory;
import dev.galasa.framework.auth.spi.IAuthService;
import dev.galasa.framework.auth.spi.IAuthServiceFactory;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;

@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
        "osgi.http.whiteboard.servlet.pattern=/users/*" }, name = "Galasa Users microservice")
public class UsersServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    public static final String QUERY_PARAM_LOGIN_ID = "loginId";
    public static final String QUERY_PARAMETER_LOGIN_ID_VALUE_MYSELF = "me";

    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(getClass());

    private IAuthServiceFactory factory;

    public UsersServlet() {
        this(new SystemEnvironment());
    }

    public UsersServlet(Environment env) {
        super(env);
    }

    @Override
    public void init() throws ServletException {
        logger.info("Galasa Users API initialising");

        if (factory == null) {
            factory = new AuthServiceFactory(framework, env);
        }

        IAuthService authService = factory.getAuthService();
        RBACService rbacService ;
        try {
            rbacService = framework.getRBACService();
        } catch ( RBACException ex) {
            throw new ServletException(ex);
        }
        
        addRoute(new UsersRoute(getResponseBuilder(), env, authService, rbacService));
        addRoute(new UserRoute(getResponseBuilder(), env, authService, rbacService));

        logger.info("Galasa Users API initialised");
    }

    protected void setAuthServiceFactory(IAuthServiceFactory factory) {
        this.factory = factory;
    }

}
