/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import java.io.IOException;

import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

public class MockCloseableHttpResponse extends BasicClassicHttpResponse {

    boolean isClosed = false;

    public MockCloseableHttpResponse() {
        super(200);
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
    }

}
