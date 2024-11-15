/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import java.io.IOException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.*;

public class MockCloseableHttpResponse implements CloseableHttpResponse {

    boolean isClosed = false ;
    StatusLine statusLine ;
    HttpEntity entity ;



    @Override
    public HttpEntity getEntity() {
        return this.entity;
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public StatusLine getStatusLine() {
        return this.statusLine;
    }

    @Override
    public void setEntity(HttpEntity newEntity) {
        this.entity = newEntity;
    }

    @Override
    public void setLocale(Locale arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'setLocale'");
    }

    @Override
    public void setReasonPhrase(String arg0) throws IllegalStateException {
        throw new UnsupportedOperationException("Unimplemented method 'setReasonPhrase'");
    }

    @Override
    public void setStatusCode(int arg0) throws IllegalStateException {
        throw new UnsupportedOperationException("Unimplemented method 'setStatusCode'");
    }

    @Override
    public void setStatusLine(StatusLine newStatusLine) {
        this.statusLine = newStatusLine;
    }

    @Override
    public void setStatusLine(ProtocolVersion arg0, int arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'setStatusLine'");
    }

    @Override
    public void setStatusLine(ProtocolVersion arg0, int arg1, String arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'setStatusLine'");
    }

    @Override
    public void addHeader(Header arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'addHeader'");
    }

    @Override
    public void addHeader(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'addHeader'");
    }

    @Override
    public boolean containsHeader(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'containsHeader'");
    }

    @Override
    public Header[] getAllHeaders() {
        throw new UnsupportedOperationException("Unimplemented method 'getAllHeaders'");
    }

    @Override
    public Header getFirstHeader(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getFirstHeader'");
    }

    @Override
    public Header[] getHeaders(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Header getLastHeader(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getLastHeader'");
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpParams getParams() {
        throw new UnsupportedOperationException("Unimplemented method 'getParams'");
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException("Unimplemented method 'getProtocolVersion'");
    }

    @Override
    public HeaderIterator headerIterator() {
        throw new UnsupportedOperationException("Unimplemented method 'headerIterator'");
    }

    @Override
    public HeaderIterator headerIterator(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'headerIterator'");
    }

    @Override
    public void removeHeader(Header arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'removeHeader'");
    }

    @Override
    public void removeHeaders(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'removeHeaders'");
    }

    @Override
    public void setHeader(Header arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'setHeader'");
    }

    @Override
    public void setHeader(String arg0, String arg1) {
        throw new UnsupportedOperationException("Unimplemented method 'setHeader'");
    }

    @Override
    public void setHeaders(Header[] arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'setHeaders'");
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setParams(HttpParams arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'setParams'");
    }

    @Override
    public void close() throws IOException {
        isClosed = true ;
    }

}

