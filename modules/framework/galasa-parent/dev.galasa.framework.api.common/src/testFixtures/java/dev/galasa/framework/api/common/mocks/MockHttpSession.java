/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

public class MockHttpSession implements HttpSession {

    Map<String, Object> attributes = new HashMap<>();

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void invalidate() {
        attributes.clear();
    }

    @Override
    public long getCreationTime() {
        throw new UnsupportedOperationException("Unimplemented method 'getCreationTime'");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    @Override
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException("Unimplemented method 'getLastAccessedTime'");
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException("Unimplemented method 'getServletContext'");
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException("Unimplemented method 'setMaxInactiveInterval'");
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException("Unimplemented method 'getMaxInactiveInterval'");
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getValue'");
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getAttributeNames'");
    }

    @Override
    public String[] getValueNames() {
        throw new UnsupportedOperationException("Unimplemented method 'getValueNames'");
    }

    @Override
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException("Unimplemented method 'putValue'");
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'removeAttribute'");
    }

    @Override
    public void removeValue(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'removeValue'");
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException("Unimplemented method 'isNew'");
    }

    @SuppressWarnings("deprecation")
    @Override
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("Unimplemented method 'getSessionContext'");
    }
    
}
