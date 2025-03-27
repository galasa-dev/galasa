/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.mocks;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.streams.StreamsServlet;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

public class MockStreamsServlet extends StreamsServlet {

    public MockStreamsServlet(IFramework framework, Environment env, IConfigurationPropertyStoreService configurationPropertyStoreService) {
        super(env);
        this.framework = framework;
        this.configurationPropertyStoreService = configurationPropertyStoreService;
    }

}
