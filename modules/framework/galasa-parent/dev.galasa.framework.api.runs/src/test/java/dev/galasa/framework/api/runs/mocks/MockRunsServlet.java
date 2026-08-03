/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.mocks;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ITestCatalogFetcher;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.runs.RunsServlet;
import dev.galasa.framework.api.runs.routes.RunsPortfoliosRoute;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

public class MockRunsServlet extends RunsServlet implements IServletUnderTest {

    private final ITestCatalogFetcher catalogFetcher;

    public MockRunsServlet(Environment env, IFramework framework) {
        super.env = env;
        setResponseBuilder(new ResponseBuilder(env));
        super.framework = framework;
        this.catalogFetcher = null;
    }

    public MockRunsServlet(Environment env, IFramework framework, ITestCatalogFetcher catalogFetcher) {
        super.env = env;
        setResponseBuilder(new ResponseBuilder(env));
        super.framework = framework;
        this.catalogFetcher = catalogFetcher;
    }

    @Override
    protected RunsPortfoliosRoute createRunsPortfoliosRoute(
            IStreamsService streamsService,
            ICredentialsService credentialsService) throws StreamsException, RBACException {
        return new RunsPortfoliosRoute(getResponseBuilder(), streamsService, framework.getRBACService(), catalogFetcher);
    }

    @Override
    public void setFramework(IFramework framework) {
        super.framework = framework;
    }

    @Override
    public void setFileSystem(IFileSystem fileSystem) {
        throw new UnsupportedOperationException("Unimplemented method 'setFileSystem'");
    }

}
