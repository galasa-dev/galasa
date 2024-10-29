/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.net.ssl.SSLSession;

/**
 * A mock implementation of the HttpResponse interface.
 */
public class MockHttpResponse<T> implements HttpResponse<T> {

    private T body;
    private HttpHeaders headers;
    private int statusCode;
    private URI uri;

    public MockHttpResponse(T body) {
        this.body = body;
    }

    public MockHttpResponse(T body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    public MockHttpResponse(T body, HttpHeaders headers) {
        this.body = body;
        this.headers = headers;
    }

    public MockHttpResponse(URI uri, HttpHeaders headers) {
        this.uri = uri;
        this.headers = headers;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public int statusCode() {
        return this.statusCode;
    }

    @Override
    public URI uri() {
        return this.uri;
    }

    @Override
    public HttpRequest request() {
        throw new UnsupportedOperationException("Unimplemented method 'request'");
    }

    @Override
    public Optional<HttpResponse<T>> previousResponse() {
        throw new UnsupportedOperationException("Unimplemented method 'previousResponse'");
    }

    @Override
    public Optional<SSLSession> sslSession() {
        throw new UnsupportedOperationException("Unimplemented method 'sslSession'");
    }

    @Override
    public Version version() {
        throw new UnsupportedOperationException("Unimplemented method 'version'");
    }

}
