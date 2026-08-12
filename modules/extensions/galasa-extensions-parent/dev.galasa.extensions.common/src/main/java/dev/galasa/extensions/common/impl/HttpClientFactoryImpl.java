/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.impl;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import dev.galasa.extensions.common.api.HttpClientFactory;

public class HttpClientFactoryImpl implements HttpClientFactory {

    @Override
    public CloseableHttpClient createClient() {
        return HttpClients.createDefault();
    }

}
