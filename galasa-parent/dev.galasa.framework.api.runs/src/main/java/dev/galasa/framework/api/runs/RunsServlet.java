/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.runs.routes.GroupRunsRoute;
import dev.galasa.framework.spi.IFramework;

/*
* Proxy servlet for /runs/* endpoints
*/
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/runs/*" }, name = "Galasa Schedule Runs microservice")
public class RunsServlet extends BaseServlet {

    @Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

    protected Environment env = new SystemEnvironment();

	private Log  logger  =  LogFactory.getLog(this.getClass());

	@Override
	public void init() throws ServletException {
		logger.info("Schedule Runs Servlet initialising");

		super.init();
		addRoute(new GroupRunsRoute(getResponseBuilder(), framework, env));
	}
}