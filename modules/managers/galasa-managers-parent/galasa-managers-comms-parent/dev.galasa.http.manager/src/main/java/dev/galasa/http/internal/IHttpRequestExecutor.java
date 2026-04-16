/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http.internal;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;

import dev.galasa.http.HttpClientException;

/**
 * An executor for HTTP requests. This interface was primarily added to make
 * unit testing easier by allowing the execution of HTTP requests to be mocked out.
 */
public interface IHttpRequestExecutor {
    /**
     * Executes an HTTP request using the given HTTP client and context.
     *
     * @param httpClient  the HTTP client to use for executing the request
     * @param httpContext the HTTP client context containing request-specific
     *                    settings
     * @param request     the HTTP request to execute
     * @return the HTTP response from executing the request
     * @throws HttpClientException if an error occurs while executing the request
     */
    ClassicHttpResponse execute(CloseableHttpClient httpClient, HttpClientContext httpContext, ClassicHttpRequest request) throws HttpClientException;
}
