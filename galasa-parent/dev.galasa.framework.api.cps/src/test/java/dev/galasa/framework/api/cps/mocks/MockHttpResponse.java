/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

import com.google.gson.stream.JsonReader;
import dev.galasa.framework.spi.utils.GalasaGson;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MockHttpResponse implements HttpServletResponse {



    private ByteArrayOutputStream payload = new ByteArrayOutputStream();
    private PrintWriter writer = new PrintWriter(payload);

    private final GalasaGson gson = new GalasaGson();;

    private int statusCode ;

    public int getStatus() {
        return statusCode ;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public void setStatus(int sc) {
        this.statusCode = sc;
    }

    public JsonReader getPayloadAsJsonReader() {
        writer.flush(); // Make sure all half-written data gets to the underlying byte array.

        String payloadGotBack = payload.toString();
        JsonReader jReader = gson.newJsonReader(new StringReader(payloadGotBack));
        return jReader;
    }


    static class ErrorMessage {
        private String error;

        public ErrorMessage(String error) {
            this.error = error;
        }
    }

    public String getPayloadAsErrorMessage() {
        writer.flush(); // Make sure all half-written data gets to the underlying byte array.

        String payloadGotBack = payload.toString();
        JsonReader jReader = gson.newJsonReader(new StringReader(payloadGotBack));

        ErrorMessage errorMessage = gson.fromJson(jReader , ErrorMessage.class);

        assertThat(errorMessage).isNotNull();
        return errorMessage.error;
    }

    public String getPayloadAsString() {
        writer.flush(); // Make sure all half-written data gets to the underlying byte array.
        String payloadGotBack = payload.toString();
        return payloadGotBack;
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String encodeUrl(String url) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void sendError(int sc) throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new MockMethodNotImplementedException();
    }

    public Map<String, String> headers = new HashMap<String, String>();


    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void addIntHeader(String name, int value) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getContentType() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setContentLength(int len) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setContentLengthLong(long len) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setContentType(String type) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setBufferSize(int size) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public int getBufferSize() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void resetBuffer() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isCommitted() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void reset() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setLocale(Locale loc) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Locale getLocale() {
        throw new MockMethodNotImplementedException();
    }
}