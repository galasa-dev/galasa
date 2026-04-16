/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.mocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

import dev.galasa.http.HttpClientException;
import dev.galasa.http.internal.IHttpRequestExecutor;

public class MockHttpRequestExecutor implements IHttpRequestExecutor {

    private ClassicHttpResponse mockResponse;
    private List<ClassicHttpRequest> requests = new ArrayList<>();

    public List<ClassicHttpRequest> getRequests() {
        return requests;
    }

    public void setMockResponse(ClassicHttpResponse mockResponse) {
        this.mockResponse = mockResponse;
    }

    @Override
    public ClassicHttpResponse execute(CloseableHttpClient httpClient, HttpClientContext httpContext,
            ClassicHttpRequest request) throws HttpClientException {
        requests.add(request);
        return mockResponse;
    }
}
