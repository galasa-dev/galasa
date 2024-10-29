/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

@SuppressWarnings("deprecation")
public class MockHttpClient implements HttpClient {

    private int requestsProcessedCount = 0 ;

    public void incrementRequestsProcessedCount() {
        requestsProcessedCount+=1;
    }

    public int getRequestsProcessed() {
        return this.requestsProcessedCount;
    }

    @Override
    public HttpParams getParams() {
        throw new UnsupportedOperationException("Unimplemented method 'getParams'");
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        throw new UnsupportedOperationException("Unimplemented method 'getConnectionManager'");
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
            throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
            throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
            throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler,
            HttpContext context) throws IOException, ClientProtocolException {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
    
}
