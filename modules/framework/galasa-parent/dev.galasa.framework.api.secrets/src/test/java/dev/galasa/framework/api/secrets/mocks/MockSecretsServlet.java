/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.mocks;

import dev.galasa.framework.api.common.ResponseBuilder;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.secrets.SecretsServlet;
import dev.galasa.framework.spi.utils.ITimeService;

public class MockSecretsServlet extends SecretsServlet {

    public MockSecretsServlet(MockFramework framework, ITimeService timeService) {
        super(FilledMockEnvironment.createTestEnvironment(), timeService);
        super.framework = framework;
        setResponseBuilder(new ResponseBuilder(env));
    }
}
