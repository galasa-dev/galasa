/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

    private PrintWriter writer;
    private ServletOutputStream outputStream;
    private int status;
    private String contentType;
    private Map<String,String> headers;

    public MockHttpServletResponse() {
        this.outputStream = new MockServletOutputStream();
        this.writer = new PrintWriter(outputStream);
        this.headers = new HashMap<String,String>();
    }

    public MockHttpServletResponse(PrintWriter writer, ServletOutputStream outputStream) {
        this.writer = writer;
        this.outputStream = outputStream;
        this.headers = new HashMap<String,String>();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("Unimplemented method 'getCharacterEncoding'");
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
      return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.writer;
    }

    @Override
    public int getStatus() {
        return this.status;
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public void setContentType(String type) {
        this.contentType = type;
    }
    @Override
    public void setHeader(String name, String value) {
       addHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(SC_FOUND);
        addHeader("Location", location);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException("Unimplemented method 'setCharacterEncoding'");
    }

    @Override
    public void setContentLength(int len) {
        throw new UnsupportedOperationException("Unimplemented method 'setContentLength'");
    }

    @Override
    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException("Unimplemented method 'setContentLengthLong'");
    }


    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException("Unimplemented method 'setBufferSize'");
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException("Unimplemented method 'getBufferSize'");
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'flushBuffer'");
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException("Unimplemented method 'resetBuffer'");
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException("Unimplemented method 'isCommitted'");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException("Unimplemented method 'setLocale'");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException("Unimplemented method 'addCookie'");
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'containsHeader'");
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException("Unimplemented method 'encodeURL'");
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectURL'");
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException("Unimplemented method 'encodeUrl'");
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException("Unimplemented method 'encodeRedirectUrl'");
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void sendError(int sc) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'sendError'");
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException("Unimplemented method 'setDateHeader'");
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException("Unimplemented method 'addDateHeader'");
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException("Unimplemented method 'setIntHeader'");
    }

    @Override
    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException("Unimplemented method 'addIntHeader'");
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }


    @Override
    public Collection<String> getHeaders(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
    }
}