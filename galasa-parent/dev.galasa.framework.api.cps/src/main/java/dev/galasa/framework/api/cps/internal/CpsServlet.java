/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.cps.internal.routes.AddPropertyInNamespaceRoute;
import dev.galasa.framework.api.cps.internal.routes.AllNamespaceRoute;
import dev.galasa.framework.api.cps.internal.routes.AllPropertiesInNamesapceFilteredRoute;
import dev.galasa.framework.api.cps.internal.routes.AllPropertiesInNamespaceRoute;
import dev.galasa.framework.api.cps.internal.routes.NamespacesRoute;
import dev.galasa.framework.api.cps.internal.routes.PropertyRoute;
import dev.galasa.framework.api.cps.internal.routes.PropertyUpdateRoute;
import dev.galasa.framework.api.common.BaseServlet;

import dev.galasa.framework.spi.IFramework;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/*
 * Proxy Servlet for the /cps/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/cps/*" }, name = "Galasa CPS microservice")
public class CpsServlet extends BaseServlet {

	@Reference
	private IFramework framework;

	private static final long serialVersionUID = 1L;

	private Log  logger  =  LogFactory.getLog(this.getClass());
 
	protected IFramework getFramework() {
        return this.framework;
    }

	protected void setFramework(IFramework framework) {
        this.framework = framework;
    }

	@Override
	public void init() throws ServletException {
		logger.info("CPS Servlet initialising");

		super.init();
		
		addRoute(new NamespacesRoute(getResponseBuilder(), framework));
		addRoute(new PropertyUpdateRoute(getResponseBuilder(), framework));
		addRoute(new PropertyRoute(getResponseBuilder(), framework));
		addRoute(new AllNamespaceRoute(getResponseBuilder(), framework));
		addRoute(new AllPropertiesInNamespaceRoute(getResponseBuilder(), framework));
		addRoute(new AllPropertiesInNamesapceFilteredRoute(getResponseBuilder(), framework));
		addRoute(new AddPropertyInNamespaceRoute(getResponseBuilder(), framework));
	}

}
