/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.mocks;

import java.net.http.HttpClient;

import dev.galasa.framework.api.common.Environment;
import dev.galasa.framework.api.common.mocks.MockHttpClient;
import dev.galasa.framework.api.streams.StreamsServlet;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;

public class MockStreamsServlet extends StreamsServlet {

    public MockStreamsServlet(
        IFramework framework,
        Environment env,
        IConfigurationPropertyStoreService configurationPropertyStoreService
    ) {
        this(framework, env, configurationPropertyStoreService, new MockHttpClient(null));
    }

    public MockStreamsServlet(
        IFramework framework,
        Environment env,
        IConfigurationPropertyStoreService configurationPropertyStoreService,
        HttpClient httpClient
    ) {
        super(env, httpClient);
        this.framework = framework;
        this.configurationPropertyStoreService = configurationPropertyStoreService;
    }

}
