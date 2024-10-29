/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

public class MockOsgiFramework implements Framework {

    private MockBundleContext bundleContext;

    public MockOsgiFramework(MockBundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public int getState() {
        throw new UnsupportedOperationException("Unimplemented method 'getState'");
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        throw new UnsupportedOperationException("Unimplemented method 'getRegisteredServices'");
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        throw new UnsupportedOperationException("Unimplemented method 'getServicesInUse'");
    }

    @Override
    public boolean hasPermission(Object permission) {
        throw new UnsupportedOperationException("Unimplemented method 'hasPermission'");
    }

    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException("Unimplemented method 'getResource'");
    }

    @Override
    public Dictionary<String, String> getHeaders(String locale) {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        throw new UnsupportedOperationException("Unimplemented method 'loadClass'");
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'getResources'");
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int signersType) {
        throw new UnsupportedOperationException("Unimplemented method 'getSignerCertificates'");
    }

    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException("Unimplemented method 'getVersion'");
    }

    @Override
    public File getDataFile(String filename) {
        throw new UnsupportedOperationException("Unimplemented method 'getDataFile'");
    }

    @Override
    public int compareTo(Bundle o) {
        throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }

    @Override
    public void init() throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }

    @Override
    public void init(FrameworkListener... listeners) throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }

    @Override
    public FrameworkEvent waitForStop(long timeout) throws InterruptedException {
        throw new UnsupportedOperationException("Unimplemented method 'waitForStop'");
    }

    @Override
    public void start() throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void start(int options) throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void stop() throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void stop(int options) throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void uninstall() throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'uninstall'");
    }

    @Override
    public void update() throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void update(InputStream in) throws BundleException {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public long getBundleId() {
        throw new UnsupportedOperationException("Unimplemented method 'getBundleId'");
    }

    @Override
    public String getLocation() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
    }

    @Override
    public String getSymbolicName() {
        throw new UnsupportedOperationException("Unimplemented method 'getSymbolicName'");
    }

    @Override
    public Enumeration<String> getEntryPaths(String path) {
        throw new UnsupportedOperationException("Unimplemented method 'getEntryPaths'");
    }

    @Override
    public URL getEntry(String path) {
        throw new UnsupportedOperationException("Unimplemented method 'getEntry'");
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException("Unimplemented method 'getLastModified'");
    }

    @Override
    public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
        throw new UnsupportedOperationException("Unimplemented method 'findEntries'");
    }

    @Override
    public <A> A adapt(Class<A> type) {
        throw new UnsupportedOperationException("Unimplemented method 'adapt'");
    }
}
