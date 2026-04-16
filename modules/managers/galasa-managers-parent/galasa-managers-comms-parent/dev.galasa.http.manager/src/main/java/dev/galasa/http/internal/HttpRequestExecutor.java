/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import java.io.IOException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

import dev.galasa.http.HttpClientException;

public class HttpRequestExecutor implements IHttpRequestExecutor {

    @Override
    public ClassicHttpResponse execute(CloseableHttpClient httpClient, HttpClientContext httpContext, ClassicHttpRequest request) throws HttpClientException {
        try {
            return httpClient.executeOpen(null, request, httpContext);
        } catch (IOException e) {
            throw new HttpClientException("Error executing http request", e);
        }
    }
    
}
