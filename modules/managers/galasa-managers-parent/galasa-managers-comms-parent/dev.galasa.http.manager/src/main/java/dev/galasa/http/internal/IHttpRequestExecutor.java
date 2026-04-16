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

public interface IHttpRequestExecutor {
    ClassicHttpResponse execute(CloseableHttpClient httpClient, HttpClientContext httpContext, ClassicHttpRequest request) throws HttpClientException;
}
