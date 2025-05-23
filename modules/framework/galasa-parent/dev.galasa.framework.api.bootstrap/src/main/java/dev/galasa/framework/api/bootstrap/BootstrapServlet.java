/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.bootstrap;

import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.api.bootstrap.routes.BootstrapExternalRoute;
/*
 * Proxy Servlet for the /bootstrap endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
    "osgi.http.whiteboard.servlet.pattern=/bootstrap/*" }, configurationPid = {
            "dev.galasa.bootstrap" }, configurationPolicy = ConfigurationPolicy.REQUIRE, name = "Galasa Bootstrap")
public class BootstrapServlet extends BaseServlet {

	@Reference
	protected IFramework framework;

	private static final long serialVersionUID = 1L;

	protected Log logger = LogFactory.getLog(this.getClass());

    @Override
	public void init() throws ServletException {
		super.init();

        addRoute(new BootstrapExternalRoute(getResponseBuilder()));
		logger.info("Bootstrap Servlet Initialised");
	}


    @Activate
    public void activate(Map<String, Object> properties) {
        onModified(properties);

        logger.info("Galasa Bootstrap API activated");
    }

    @Modified
    public void onModified(Map<String, Object> properties) {
        logger.info("Updated properties in Galasa Bootstrap API");
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivated Galasa Bootstrap API");
    }
}