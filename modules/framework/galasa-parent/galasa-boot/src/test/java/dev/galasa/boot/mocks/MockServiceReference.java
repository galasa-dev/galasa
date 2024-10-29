/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class MockServiceReference<T> implements ServiceReference<T> {

    private T service;
    private Bundle bundle;

    public MockServiceReference(T service, Bundle bundle) {
        this.bundle = bundle;
        this.service = service;
    }

    public T getService() {
        return this.service;
    }

    @Override
    public Bundle getBundle() {
        return this.bundle;
    }

    @Override
    public Object getProperty(String key) {
        throw new UnsupportedOperationException("Unimplemented method 'getProperty'");
    }

    @Override
    public String[] getPropertyKeys() {
        throw new UnsupportedOperationException("Unimplemented method 'getPropertyKeys'");
    }

    @Override
    public Bundle[] getUsingBundles() {
        throw new UnsupportedOperationException("Unimplemented method 'getUsingBundles'");
    }

    @Override
    public boolean isAssignableTo(Bundle bundle, String className) {
        throw new UnsupportedOperationException("Unimplemented method 'isAssignableTo'");
    }

    @Override
    public int compareTo(Object reference) {
        throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }

    @Override
    public Dictionary<String, Object> getProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public <A> A adapt(Class<A> type) {
        throw new UnsupportedOperationException("Unimplemented method 'adapt'");
    }
}
