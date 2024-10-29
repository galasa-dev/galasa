/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.cps;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkPropertyFile;
import dev.galasa.framework.spi.FrameworkPropertyFileException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;

/**
 * <p>
 * This class is used when the FPF class is being operated as the Key-Value
 * store for the Configuration property store. This class registers the
 * Configuration property store as the only CPS.
 * </p>
 * 
 *  
 *  
 */

public class FpfConfigurationPropertyStore implements IConfigurationPropertyStore {
    private FrameworkPropertyFile fpf;

    public FpfConfigurationPropertyStore(URI file) throws ConfigurationPropertyStoreException {
        try {
            fpf = new FrameworkPropertyFile(file);
        } catch (FrameworkPropertyFileException e) {
            throw new ConfigurationPropertyStoreException("Failed to create Framework property file", e);
        }
    }

    /**
     * <p>
     * This method implements the getProperty method from the framework property
     * file class, returning a string value from a key inside the property file, or
     * null if empty.
     * </p>
     * 
     * @param key
     * @throws ConfigurationPropertyStoreException
     */
    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        return fpf.get(key);
    }
    
    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        return fpf.getPrefix(prefix);
    }

    /**
     * <p>
     * This method implements the setProperty method from the framework property
     * file class.
     * </p>
     * 
     * @param key
     * @param value
     * @throws ConfigurationPropertyStoreException
     */
    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        try {
            fpf.set(key, value);
        } catch (FrameworkPropertyFileException e) {
            throw new ConfigurationPropertyStoreException("Unable to set property value", e);
        }
    }
    
    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        try {
            fpf.delete(key);
        } catch (FrameworkPropertyFileException e) {
            throw new ConfigurationPropertyStoreException("Unable to set property value", e);
        }
    }

    /**
     * <p>
     * This method returns all properties for a given namespace from the framework property
     * file class.
     * </p>
     * 
     * @param namespace
     * @return properties
     */
    @Override
    public Map<String,String> getPropertiesFromNamespace(String namespace) {
        return fpf.getPrefix(namespace);
    }

    /**
     * <p>
     * A simple method thta checks the provided URI to the CPS is a local file or
     * not.
     * </p>
     * 
     * @param uri - URI to the CPS
     * @return - boolean if File or not.
     */
    public static boolean isFileUri(URI uri) {
        return "file".equals(uri.getScheme());
    }

    /**
     * <p>
     * Return all Namespaces for the framework property file
     * </p>
     * 
     * @return - List of namespaces
     */
    public List<String> getNamespaces() {
        return fpf.getNamespaces();
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        try {
            this.fpf.shutdown();
        } catch (FrameworkPropertyFileException e) {
            throw new ConfigurationPropertyStoreException("Problem shutting down the CPS File", e);
        }
    }

}
