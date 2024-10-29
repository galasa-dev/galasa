/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import java.io.ByteArrayInputStream;



public class MockHttpEntity extends BaseHttpEntity {

    private MockHttpHeader contentTypeJsonHeader ;
    private byte[] payloadMessageBytes ;

    public MockHttpEntity(String payloadMessage) {

        contentTypeJsonHeader = new MockHttpHeader("Content-Type","application/json");

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
    public Header getContentType() {
        return contentTypeJsonHeader;
    }
}

