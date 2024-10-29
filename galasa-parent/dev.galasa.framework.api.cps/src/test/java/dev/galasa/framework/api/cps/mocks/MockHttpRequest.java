/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class MockHttpRequest implements HttpServletRequest {

    private String path ;
    private String query ;

    public MockHttpRequest(String path) {
        this(path,null);
    }

    public MockHttpRequest(String path , String query ) {
        this.path = path;
        this.query = query;
    }

    @Override
    public String getAuthType() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Cookie[] getCookies() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public long getDateHeader(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getHeader(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getMethod() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getPathInfo() {
        return path ;
    }

    @Override
    public String getPathTranslated() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getContextPath() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getQueryString() {
        return this.query;
    }

    @Override
    public String getRemoteUser() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getRequestedSessionId() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getRequestURI() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getServletPath() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public HttpSession getSession() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String changeSessionId() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void logout() throws ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Object getAttribute(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public int getContentLength() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public long getContentLengthLong() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getContentType() {
        throw new MockMethodNotImplementedException();
    }

    public void setBody( String jsonPayload ) {
        this.jsonBody = jsonPayload ;
    }

    String jsonBody ;

    public static class MockServletInputStream extends ServletInputStream {

        byte[] bodyBytes ;
        int bodyBytesIndex = 0 ;

        public MockServletInputStream(String body) {
            this.bodyBytes = body.getBytes();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            int result;
            if( bodyBytesIndex < bodyBytes.length) {
                result = this.bodyBytes[bodyBytesIndex++];
            } else {
                result = -1;
            }
            return result;
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new MockServletInputStream(this.jsonBody);
    }

    @Override
    public String getParameter(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String[] getParameterValues(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getProtocol() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getScheme() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getServerName() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public int getServerPort() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getRemoteAddr() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getRemoteHost() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void removeAttribute(String name) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Locale getLocale() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isSecure() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getRealPath(String path) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public int getRemotePort() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getLocalName() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String getLocalAddr() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public int getLocalPort() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public ServletContext getServletContext() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isAsyncStarted() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public boolean isAsyncSupported() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new MockMethodNotImplementedException();
    }
}
