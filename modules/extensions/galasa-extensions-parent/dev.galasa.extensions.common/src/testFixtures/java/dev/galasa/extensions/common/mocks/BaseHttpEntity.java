/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

// A base concrete class which throws exceptions for all methods in the interface.
public class BaseHttpEntity implements HttpEntity {

    @Override
    public boolean isStreaming() {
        return false;
    }

    @Override
    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Unimplemented method 'getContent'");
    }

    @Override
    public String getContentEncoding() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentEncoding'");
    }

    @Override
    public long getContentLength() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentLength'");
    }

    @Override
    public String getContentType() {
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
    public void writeTo(OutputStream outstream) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'writeTo'");
    }

    @Override
    public Supplier<List<? extends Header>> getTrailers() {
        return null;
    }

    @Override
    public Set<String> getTrailerNames() {
        return null;
    }

    @Override
    public void close() throws IOException {
        // Do nothing...
    }

}
