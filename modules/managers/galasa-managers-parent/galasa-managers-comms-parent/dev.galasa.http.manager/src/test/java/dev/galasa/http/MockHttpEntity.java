/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Manual mock implementation of HttpEntity for testing
 */
public class MockHttpEntity implements HttpEntity {

    private final byte[] content;
    private final IOException contentException;
    private final long contentLength;

    public MockHttpEntity(byte[] content) {
        this.content = content;
        this.contentException = null;
        this.contentLength = content.length;
    }

    public MockHttpEntity(IOException exception) {
        this.content = null;
        this.contentException = exception;
        this.contentLength = -1;
    }

    @Override
    public InputStream getContent() throws IOException {
        if (contentException != null) {
            throw contentException;
        }
        return new ByteArrayInputStream(content);
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        throw new UnsupportedOperationException("writeTo not implemented");
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        throw new UnsupportedOperationException("getTrailerNames not implemented");
    }

    @Override
    public Supplier<java.util.List<? extends Header>> getTrailers() {
        throw new UnsupportedOperationException("getTrailers not implemented");
    }

    @Override
    public void close() throws IOException {
        // No-op for mock
    }
}
