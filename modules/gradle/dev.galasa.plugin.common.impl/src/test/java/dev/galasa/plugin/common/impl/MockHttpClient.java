/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

public class MockHttpClient extends CloseableHttpClient {

    private int requestsProcessedCount = 0;

    public void incrementRequestsProcessedCount() {
        requestsProcessedCount += 1;
    }

    public int getRequestsProcessed() {
        return this.requestsProcessedCount;
    }

    @Override
    public void close() throws IOException {
        // Do nothing...
    }

    @Override
    public void close(CloseMode closeMode) {
        // Do nothing...
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, ClassicHttpRequest request, HttpContext context)
            throws IOException {
        try {
            return CloseableHttpResponse.adapt(performRequest(request));
        } catch (URISyntaxException | ParseException e) {
            throw new IOException(e);
        }
    }

    public ClassicHttpResponse performRequest(ClassicHttpRequest request) throws IOException, URISyntaxException, ParseException {
        throw new UnsupportedOperationException("Unimplemented method 'performRequest'");
    }
}
