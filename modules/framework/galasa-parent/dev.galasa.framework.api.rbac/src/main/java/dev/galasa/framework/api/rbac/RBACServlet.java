/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.rbac;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.rbac.internal.routes.ActionDetailsRoute;
import dev.galasa.framework.api.rbac.internal.routes.ActionsRoute;
import dev.galasa.framework.api.rbac.internal.routes.RoleDetailsRoute;
import dev.galasa.framework.api.rbac.internal.routes.RolesRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/*
 * REST API Servlet for the /secrets/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/rbac/*" }, name = "Galasa RBAC microservice")
public class RBACServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

    private ITimeService timeService;

	private static final long serialVersionUID = 1L;

	private Log logger = LogFactory.getLog(this.getClass());
 
    public RBACServlet() {
        this(new SystemEnvironment(), new SystemTimeService());
    }

    public RBACServlet(Environment env, ITimeService timeService) {
        super(env);
        this.timeService = timeService;
    }
    
	@Override
	public void init() throws ServletException {
		logger.info("RBAC servlet initialising");

        try {
            RBACService rbacService = framework.getRBACService();
            addRoute(new RolesRoute(getResponseBuilder(), rbacService, env, timeService));
            addRoute(new ActionsRoute(getResponseBuilder(), rbacService, env, timeService));
            addRoute(new RoleDetailsRoute(getResponseBuilder(), rbacService, env, timeService));
            addRoute(new ActionDetailsRoute(getResponseBuilder(), rbacService, env, timeService));
        } catch (RBACException e) {
            throw new ServletException("Failed to initialise the RBAC servlet");
        }
		logger.info("RBAC servlet initialised");
	}
}
