/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class MockHttpEntity extends BaseHttpEntity {

    private byte[] payloadMessageBytes;

    public MockHttpEntity(String payloadMessage) {
        payloadMessageBytes = payloadMessage.getBytes();
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        return new ByteArrayInputStream(this.payloadMessageBytes);
    }

    @Override
    public long getContentLength() {
        return this.payloadMessageBytes.length;
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
