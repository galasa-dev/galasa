/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.mocks;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.monitors.MonitorsServlet;

public class MockMonitorsServlet extends MonitorsServlet {

    public MockMonitorsServlet(MockFramework framework, MockKubernetesApiClient kubeApiClient) {
        super(FilledMockEnvironment.createTestEnvironment(), kubeApiClient);
        super.framework = framework;
        setResponseBuilder(new ResponseBuilder(env));
    }
}
