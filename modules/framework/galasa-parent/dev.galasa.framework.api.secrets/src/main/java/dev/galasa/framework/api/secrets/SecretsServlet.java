/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.secrets.internal.routes.SecretDetailsRoute;
import dev.galasa.framework.api.secrets.internal.routes.SecretsRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
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
"osgi.http.whiteboard.servlet.pattern=/secrets/*" }, name = "Galasa Secrets microservice")
public class SecretsServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    private ITimeService timeService;

    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(this.getClass());

    public SecretsServlet() {
        this(new SystemEnvironment(), new SystemTimeService());
    }

    public SecretsServlet(Environment env, ITimeService timeService) {
        super(env);
        this.timeService = timeService;
    }
 
    @Override
    public void init() throws ServletException {
        logger.info("Secrets servlet initialising");

        try {
            ICredentialsService credentialsService = framework.getCredentialsService();
            RBACService rbacService = framework.getRBACService();
            addRoute(new SecretsRoute(getResponseBuilder(), credentialsService, timeService, rbacService));
            addRoute(new SecretDetailsRoute(getResponseBuilder(), credentialsService, timeService, rbacService));
        } catch (CredentialsException | RBACException e) {
            throw new ServletException("Failed to initialise the Secrets servlet");
        }
		logger.info("Secrets servlet initialised");
	}
}
