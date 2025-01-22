/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.common.resources.CPSFacade;
import dev.galasa.framework.api.resources.routes.ResourcesRoute;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;
/*
 * Proxy Servlet for the /resources/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/resources/*" }, name = "Galasa Resources microservice")
public class ResourcesServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log  logger  =  LogFactory.getLog(this.getClass());

    private ITimeService timeService;

	public ResourcesServlet() {
		this(new SystemEnvironment(), new SystemTimeService());
	}

	public ResourcesServlet(Environment env, ITimeService timeService) {
		super(env);
		this.timeService = timeService;
	}
	
	protected IFramework getFramework() {
        return this.framework;
    }

	protected void setFramework(IFramework framework) {
        this.framework = framework;
    }

	@Override
	public void init() throws ServletException {
		logger.info("Resources servlet initialising");

		super.init();

		try {
			CPSFacade cpsFacade = new CPSFacade(framework);
			ICredentialsService credsService = framework.getCredentialsService();
			RBACService rbacService = framework.getRBACService();
            addRoute(new ResourcesRoute(getResponseBuilder(), cpsFacade, credsService, timeService, rbacService));
        } catch (ConfigurationPropertyStoreException | CredentialsException | RBACException e) {
            logger.error("Failed to initialise the Resources servlet", e);
            throw new ServletException("Failed to initialise the Resources servlet", e);
        }
        logger.info("Resources servlet initialised");
	}
}
