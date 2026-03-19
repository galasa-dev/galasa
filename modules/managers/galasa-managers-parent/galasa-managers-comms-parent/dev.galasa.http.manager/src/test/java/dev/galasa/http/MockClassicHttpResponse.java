/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHeader;

/**
 * Manual mock implementation of ClassicHttpResponse for testing
 */
public class MockClassicHttpResponse implements ClassicHttpResponse {

    private ProtocolVersion version;
    private int statusCode;
    private String reasonPhrase;
    private List<Header> headers;
    private HttpEntity entity;
    private boolean closed;
    private int closeCount;
    private IOException closeException;

    public MockClassicHttpResponse(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = new ArrayList<>();
        this.closed = false;
        this.closeCount = 0;
    }

    public void addHeader(String name, String value) {
        headers.add(new BasicHeader(name, value));
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public void setCloseException(IOException exception) {
        this.closeException = exception;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getCloseCount() {
        return closeCount;
    }

    @Override
    public int getCode() {
        return statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public Header[] getHeaders() {
        return headers.toArray(new Header[0]);
    }

    @Override
    public void close() throws IOException {
        closeCount++;
        closed = true;
        if (closeException != null) {
            throw closeException;
        }
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public void setCode(int code) {
        this.statusCode = code;
    }

    @Override
    public void setReasonPhrase(String reason) {
        this.reasonPhrase = reason;
    }

    @Override
    public void setVersion(ProtocolVersion version) {
        this.version = version;
    }

    // Unsupported methods - throw UnsupportedOperationException


    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("getLocale not implemented");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException("setLocale not implemented");
    }

    @Override
    public void addHeader(Header header) {
        throw new UnsupportedOperationException("addHeader(Header) not implemented");
    }

    @Override
    public void addHeader(String name, Object value) {
        throw new UnsupportedOperationException("addHeader(String, Object) not implemented");
    }

    @Override
    public void setHeader(Header header) {
        throw new UnsupportedOperationException("setHeader(Header) not implemented");
    }

    @Override
    public void setHeader(String name, Object value) {
        throw new UnsupportedOperationException("setHeader(String, Object) not implemented");
    }

    @Override
    public void setHeaders(Header... headers) {
        throw new UnsupportedOperationException("setHeaders not implemented");
    }

    @Override
    public boolean removeHeader(Header header) {
        throw new UnsupportedOperationException("removeHeader not implemented");
    }

    @Override
    public boolean removeHeaders(String name) {
        throw new UnsupportedOperationException("removeHeaders not implemented");
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("containsHeader not implemented");
    }

    @Override
    public int countHeaders(String name) {
        throw new UnsupportedOperationException("countHeaders not implemented");
    }

    @Override
    public Header getFirstHeader(String name) {
        throw new UnsupportedOperationException("getFirstHeader not implemented");
    }

    @Override
    public Header getLastHeader(String name) {
        throw new UnsupportedOperationException("getLastHeader not implemented");
    }

    @Override
    public Header[] getHeaders(String name) {
        throw new UnsupportedOperationException("getHeaders(String) not implemented");
    }

    @Override
    public org.apache.hc.core5.http.Header getHeader(String name) throws org.apache.hc.core5.http.ProtocolException {
        throw new UnsupportedOperationException("getHeader not implemented");
    }

    @Override
    public java.util.Iterator<Header> headerIterator() {
        throw new UnsupportedOperationException("headerIterator not implemented");
    }

    @Override
    public java.util.Iterator<Header> headerIterator(String name) {
        throw new UnsupportedOperationException("headerIterator(String) not implemented");
    }
}
