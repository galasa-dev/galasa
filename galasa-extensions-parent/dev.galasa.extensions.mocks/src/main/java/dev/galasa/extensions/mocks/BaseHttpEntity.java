/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

// A base concrete class which throws exceptions for all methods in the interface.
public class BaseHttpEntity implements HttpEntity {

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void consumeContent() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'consumeContent'");
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Unimplemented method 'getContent'");
    }

    @Override
    public Header getContentEncoding() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentEncoding'");
    }

    @Override
    public long getContentLength() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentLength'");
    }

    @Override
    public Header getContentType() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentType'");
    }

    @Override
    public boolean isChunked() {
        throw new UnsupportedOperationException("Unimplemented method 'isChunked'");
    }

    @Override
    public boolean isRepeatable() {
        throw new UnsupportedOperationException("Unimplemented method 'isRepeatable'");
    }

    @Override
    public void writeTo(OutputStream arg0) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'writeTo'");
    }

}
