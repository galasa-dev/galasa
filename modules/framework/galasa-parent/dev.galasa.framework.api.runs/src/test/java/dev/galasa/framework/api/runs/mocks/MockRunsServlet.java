/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.runs.mocks;

import java.net.http.HttpClient;

import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.IServletUnderTest;
import dev.galasa.framework.api.runs.RunsServlet;
import dev.galasa.framework.spi.IFramework;

public class MockRunsServlet extends RunsServlet implements IServletUnderTest {

    public MockRunsServlet(Environment env, IFramework framework) {
        super.env = env;
        setResponseBuilder(new ResponseBuilder(env));
        super.framework = framework;
    }

    public MockRunsServlet(Environment env, IFramework framework, HttpClient httpClient) {
        super(env, httpClient);
        setResponseBuilder(new ResponseBuilder(env));
        super.framework = framework;
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
