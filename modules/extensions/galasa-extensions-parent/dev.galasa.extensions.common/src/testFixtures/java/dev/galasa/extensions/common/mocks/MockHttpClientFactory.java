/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import org.apache.http.impl.client.CloseableHttpClient;

import dev.galasa.extensions.common.api.HttpClientFactory;

public class MockHttpClientFactory implements HttpClientFactory {
    private CloseableHttpClient clientToReturn ;

    public MockHttpClientFactory(CloseableHttpClient clientToReturn) {
        this.clientToReturn = clientToReturn;
    }

    @Override
    public CloseableHttpClient createClient() {
        return this.clientToReturn;
    }
}