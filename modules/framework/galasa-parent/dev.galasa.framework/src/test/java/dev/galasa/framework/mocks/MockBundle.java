package dev.galasa.framework.mocks;

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
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class MockBundle implements Bundle {

    private Map<String,Class<?>> loadedClasses ;
    private String symbolicName ;

    /**
     * 
     * @param loadedClasses The classes which could be returned from loadClass(name). The key to the map is the name.
     */
    public MockBundle( Map<String,Class<?>> loadedClasses , String symbolicName) {
        this.loadedClasses = loadedClasses ;
        this.symbolicName = symbolicName;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadedClasses.get(name);
    }

    @Override
    public String getSymbolicName() {
         return this.symbolicName;
    }

    //------------------- Unimplemented methods below ----------------------

    @Override
    public int compareTo(Bundle o) {
         throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }

    @Override
    public int getState() {
         throw new UnsupportedOperationException("Unimplemented method 'getState'");
    }

    @Override
    public void start(int options) throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void start() throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void stop(int options) throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void stop() throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void update(InputStream input) throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void update() throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void uninstall() throws BundleException {
         throw new UnsupportedOperationException("Unimplemented method 'uninstall'");
    }

    @Override
    public Dictionary<String, String> getHeaders() {
         throw new UnsupportedOperationException("Unimplemented method 'getHeaders'");
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
    public Enumeration<URL> getResources(String name) throws IOException {
         throw new UnsupportedOperationException("Unimplemented method 'getResources'");
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
    public BundleContext getBundleContext() {
         throw new UnsupportedOperationException("Unimplemented method 'getBundleContext'");
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
    public <A> A adapt(Class<A> type) {
         throw new UnsupportedOperationException("Unimplemented method 'adapt'");
    }

    @Override
    public File getDataFile(String filename) {
         throw new UnsupportedOperationException("Unimplemented method 'getDataFile'");
    }
    
}
