/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

public class MockHttpClient extends HttpClient {

    private HttpResponse<?> mockResponse;

    public MockHttpClient(HttpResponse<?> mockResponse) {
        this.mockResponse = mockResponse;
    }

    public void setMockResponse(HttpResponse<?> mockResponse) {
        this.mockResponse = mockResponse;
    }

    // Casting to HttpResponse<T> is safe since we're returning a mock response of the
    // same type in our tests, so let's suppress the type safety warning
    @SuppressWarnings("unchecked")
    @Override
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return (HttpResponse<T>) mockResponse;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        throw new UnsupportedOperationException("Unimplemented method 'cookieHandler'");
    }

    @Override
    public Optional<Duration> connectTimeout() {
        throw new UnsupportedOperationException("Unimplemented method 'connectTimeout'");
    }

    @Override
    public Redirect followRedirects() {
        throw new UnsupportedOperationException("Unimplemented method 'followRedirects'");
    }

    @Override
    public Optional<ProxySelector> proxy() {
        throw new UnsupportedOperationException("Unimplemented method 'proxy'");
    }

    @Override
    public SSLContext sslContext() {
        throw new UnsupportedOperationException("Unimplemented method 'sslContext'");
    }

    @Override
    public SSLParameters sslParameters() {
        throw new UnsupportedOperationException("Unimplemented method 'sslParameters'");
    }

    @Override
    public Optional<Authenticator> authenticator() {
        throw new UnsupportedOperationException("Unimplemented method 'authenticator'");
    }

    @Override
    public Version version() {
        throw new UnsupportedOperationException("Unimplemented method 'version'");
    }

    @Override
    public Optional<Executor> executor() {
        throw new UnsupportedOperationException("Unimplemented method 'executor'");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
        throw new UnsupportedOperationException("Unimplemented method 'sendAsync'");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
            PushPromiseHandler<T> pushPromiseHandler) {
        throw new UnsupportedOperationException("Unimplemented method 'sendAsync'");
    }
}
