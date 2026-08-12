/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;

// An expected request and mock response delivered over the http interface.
public interface HttpInteraction {
    void validateRequest(HttpHost target, HttpRequest request) throws RuntimeException;
    public MockCloseableHttpResponse getResponse();
}
