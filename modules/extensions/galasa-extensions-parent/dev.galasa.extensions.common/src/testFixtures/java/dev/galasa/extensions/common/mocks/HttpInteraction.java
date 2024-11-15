/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;

// An expected request and mock response delivered over the http interface.
public interface HttpInteraction {
    void validateRequest(HttpHost target, HttpRequest request) throws RuntimeException;
    public MockCloseableHttpResponse getResponse();
}
