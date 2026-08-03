/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs;

import java.net.http.HttpClient;
import java.time.Duration;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.api.common.BaseServlet;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.SystemEnvironment;
import dev.galasa.framework.api.common.TestCatalogFetcher;
import dev.galasa.framework.api.runs.routes.GroupRunsRoute;
import dev.galasa.framework.api.runs.routes.RunsPortfoliosRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

/*
* Proxy servlet for /runs/* endpoints
*/
@Component(service = Servlet.class, scope = ServiceScope.PROTOTYPE, property = {
"osgi.http.whiteboard.servlet.pattern=/runs/*" }, name = "Galasa Schedule Runs microservice")
public class RunsServlet extends BaseServlet {

    @Reference
    protected IFramework framework;

    private static final long serialVersionUID = 1L;
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(60);

    private Log logger = LogFactory.getLog(this.getClass());

    private final HttpClient httpClient;

    public RunsServlet() {
        this(new SystemEnvironment(), HttpClient.newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    public RunsServlet(Environment env, HttpClient httpClient) {
        super(env);
        this.httpClient = httpClient;
    }

    @Override
    public void init() throws ServletException {
        logger.info("Schedule Runs Servlet initialising");

        super.init();
        try {

            IStreamsService streamsService = framework.getStreamsService();
            ICredentialsService credentialsService = framework.getCredentialsService();

            addRoute(createRunsPortfoliosRoute(streamsService, credentialsService));
            addRoute(new GroupRunsRoute(getResponseBuilder(), framework, env));
        } catch (RBACException e) {
            throw new ServletException("Failed to initialise schedule runs servlet");
        } catch (Exception e) {
            throw new ServletException("Failed to initialise schedule runs servlet", e);
        }
        logger.info("Schedule Runs Servlet initialised");
    }

    protected RunsPortfoliosRoute createRunsPortfoliosRoute(
            IStreamsService streamsService,
            ICredentialsService credentialsService) throws StreamsException, RBACException {
        ITestCatalogFetcher fetcher = new TestCatalogFetcher(httpClient, credentialsService);
        return new RunsPortfoliosRoute(getResponseBuilder(), streamsService,
                framework.getRBACService(), fetcher);
    }
}
