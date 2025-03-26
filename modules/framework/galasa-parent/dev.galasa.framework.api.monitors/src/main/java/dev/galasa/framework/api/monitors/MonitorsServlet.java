/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.monitors.internal.IKubernetesApiClient;
import dev.galasa.framework.api.monitors.internal.KubernetesApiClient;
import dev.galasa.framework.api.monitors.internal.routes.MonitorsDetailsRoute;
import dev.galasa.framework.api.monitors.internal.routes.MonitorsRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/*
 * REST API Servlet for the /monitors/* endpoints
 */
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/monitors/*" }, name = "Galasa Monitors microservice")
public class MonitorsServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    private static final long serialVersionUID = 1L;

    private Log logger = LogFactory.getLog(this.getClass());

    private IKubernetesApiClient kubeApiClient;

    public MonitorsServlet() {
        this(new SystemEnvironment(), new KubernetesApiClient());
    }

    public MonitorsServlet(Environment env, IKubernetesApiClient kubeApiClient) {
        super(env);
        this.kubeApiClient = kubeApiClient;
    }
 
    @Override
    public void init() throws ServletException {
        logger.info("Monitors servlet initialising");

        String kubeNamespace = env.getenv(EnvironmentVariables.GALASA_KUBERNETES_NAMESPACE);

        try {
            RBACService rbacService = framework.getRBACService();
            addRoute(new MonitorsRoute(getResponseBuilder(), rbacService, kubeApiClient, kubeNamespace));
            addRoute(new MonitorsDetailsRoute(getResponseBuilder(), rbacService, kubeApiClient, kubeNamespace));
        } catch (RBACException e) {
            throw new ServletException("Failed to initialise the monitors servlet");
        }
		logger.info("Monitors servlet initialised");
	}
}
