/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

public class MockHttpServletRequest implements HttpServletRequest {

    private Map<String, String[]> parameterMap = new HashMap<>();
    private Map<String, String> headerMap = new HashMap<>();
    private MockServletInputStream inputStream;
    private String pathInfo;
    private String payload;
    private String method = "GET"; 
    private String contextPath = "/api";
    private String contentType = "application/json";
    private String clientIp;

    private MockHttpSession session;

    public MockHttpServletRequest(String pathInfo) {
        this.pathInfo = pathInfo;
    }
    
    public MockHttpServletRequest(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public MockHttpServletRequest(Map<String, String[]> parameterMap, String pathInfo) {
        this.parameterMap = parameterMap;
        this.pathInfo = pathInfo;
    }

    public MockHttpServletRequest(Map<String, String[]> parameterMap, String pathInfo, Map<String, String> headerMap) {
        this.parameterMap = parameterMap;
        this.pathInfo = pathInfo;
        this.headerMap = headerMap;
    }

    public MockHttpServletRequest(Map<String, String[]> parameterMap, String pathInfo, MockHttpSession session) {
        this(parameterMap, pathInfo);
        this.session = session;
    }

    public MockHttpServletRequest(String servletPath, Map<String, String> headerMap) {
        this.pathInfo = servletPath;
        this.headerMap = headerMap;
    }

    public MockHttpServletRequest(String payload, String pathInfo) {
        this.payload = payload;
        this.pathInfo = pathInfo;
    }

    public MockHttpServletRequest(String pathInfo, String content, String method) {
        this.pathInfo = pathInfo;
        this.payload = content;
        this.method = method;
        this.inputStream = new MockServletInputStream(content);
    }

    public MockHttpServletRequest(String pathInfo, String content, String method, Map<String, String> headerMap) {
        this(pathInfo,content,method);
        this.headerMap = headerMap;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        Reader stringReader = new StringReader(payload);
        return new BufferedReader(stringReader);
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public String getServletPath() {
        return this.pathInfo;
    }

    @Override
    public String getHeader(String name) {
        return headerMap.get(name);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    @Override
    public int getContentLength() {
        return this.payload.length();
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getRequestURI() {
        return this.contextPath + this.pathInfo;
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "mock.galasa.server";
    }

    @Override
    public StringBuffer getRequestURL() {
        String requestUrl = getScheme() + "://" + getServerName() + getPathInfo();
        return new StringBuffer().append(requestUrl);
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (create && session == null) {
            session = new MockHttpSession();
        }

        if (session != null) {
            return session;
        }
        return null;
    }

    @Override
    public HttpSession getSession() {
        if (session == null) {
            session = new MockHttpSession();
        }
        return session;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setQueryParameter(String parameter, String value) {
        this.parameterMap.put(parameter, new String[] { value });
    }

    public void setHeader(String header, String value) {
        this.headerMap.put(header, value);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getRemoteAddr() {
        return this.clientIp;
    }

    public void setRemoteAddr(String clientIp) {
        this.clientIp = clientIp;
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getAttribute'");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getAttributeNames'");
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException("Unimplemented method 'getCharacterEncoding'");
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException("Unimplemented method 'setCharacterEncoding'");
    }

    @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException("Unimplemented method 'getContentLengthLong'");
    }

    @Override
    public String getParameter(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getParameter'");
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getParameterNames'");
    }

    @Override
    public String[] getParameterValues(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getParameterValues'");
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException("Unimplemented method 'getProtocol'");
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException("Unimplemented method 'getServerPort'");
    }

    @Override
    public String getRemoteHost() {
        throw new UnsupportedOperationException("Unimplemented method 'getRemoteHost'");
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new UnsupportedOperationException("Unimplemented method 'setAttribute'");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'removeAttribute'");
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocale'");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocales'");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Unimplemented method 'isSecure'");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException("Unimplemented method 'getRequestDispatcher'");
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException("Unimplemented method 'getRealPath'");
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Unimplemented method 'getRemotePort'");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalName'");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalAddr'");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalPort'");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Unimplemented method 'getServletContext'");
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Unimplemented method 'startAsync'");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException("Unimplemented method 'isAsyncStarted'");
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException("Unimplemented method 'isAsyncSupported'");
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Unimplemented method 'getAsyncContext'");
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException("Unimplemented method 'getDispatcherType'");
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthType'");
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException("Unimplemented method 'getCookies'");
    }

    @Override
    public long getDateHeader(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getDateHeader'");
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderNames'");
    }

    @Override
    public int getIntHeader(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getIntHeader'");
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException("Unimplemented method 'getPathTranslated'");
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException("Unimplemented method 'getQueryString'");
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException("Unimplemented method 'getRemoteUser'");
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException("Unimplemented method 'isUserInRole'");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipal'");
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException("Unimplemented method 'getRequestedSessionId'");
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Unimplemented method 'changeSessionId'");
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdValid'");
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromCookie'");
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromURL'");
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException("Unimplemented method 'isRequestedSessionIdFromUrl'");
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'getParts'");
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'getPart'");
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException("Unimplemented method 'upgrade'");
    }
}