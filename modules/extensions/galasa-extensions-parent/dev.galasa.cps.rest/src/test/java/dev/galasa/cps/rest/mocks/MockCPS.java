/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.rest.mocks;

import java.util.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;


/** 
 * A CPS implementation which holds a hashmap as the basis of it's property store.
 * 
 * It sits in memory and leaves no debris, so is suitable for use with unit tests.
 */
public class MockCPS implements IConfigurationPropertyStore {

    public MockCPS() {
        this(new HashMap<String,String>());
    }

    public MockCPS(Map<String,String> properties) {
        // Take a clone of the in-coming properties so we have a mutable map.
        this.properties = new HashMap<>(properties);

        // Check that the test isn't adding an invalid property.
        for( Map.Entry<String,String> property : properties.entrySet()) {
            String propName = property.getKey();
            String[] parts = propName.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Test failed. The test data could not be loaded into the mockCPS. Property '"+propName+"' should have a '.' character inside.");
            }
        }
    }

    public int callCounterForGetNamespaces = 0 ;
    public int callCounterForGetPropertiesFromNamespace = 0 ;
    public int callCounterForShutdown = 0 ;
    public int callCounterForSetProperty = 0 ;
    public int callCounterForDeleteProperty = 0 ;
    public int callCounterForGetProperty = 0;

    public Map<String,String> properties ;

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) throws ConfigurationPropertyStoreException {

        Map<String,String> results = new HashMap<String,String>();

        for( Map.Entry<String,String> property : properties.entrySet()) {
            String propName = property.getKey();
            String propValue = property.getValue();

            String propNamespace = propName.split("\\.")[0];

            if (propNamespace.equals(namespace)) {
                results.put(propName,propValue);
            }
        }

        callCounterForGetPropertiesFromNamespace+=1;

        return results;
    }

    @Override
    public List<String> getNamespaces() throws ConfigurationPropertyStoreException {
        callCounterForGetNamespaces +=1;

        List<String> namespaces = new ArrayList<>();

        // Gather all the unique namespaces by looking at the property keys.
        Set<String> namespacesSet = new HashSet<>();
        for( Map.Entry<String,String> property : properties.entrySet()) {
            String propName = property.getKey();
            String propNamespace = propName.split("\\.")[0];

            namespacesSet.add(propNamespace);
        }

        namespaces.addAll(namespacesSet);

        return namespaces;
    }

    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {

        callCounterForGetProperty +=1 ;
        return properties.get(key);
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {

        Map<String, String> prefixedProps = new HashMap<>();
        for( Map.Entry<String,String> property : properties.entrySet()) {
            String propName = property.getKey();
            String propValue = property.getValue();
            if (propName.startsWith(prefix)) {
                prefixedProps.put(propName,propValue);
            }
        }
        return prefixedProps;
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        callCounterForSetProperty +=1 ;

        properties.put(key,value);
    }

    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        callCounterForDeleteProperty += 1;

        this.properties.remove(key);
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        callCounterForShutdown +=1;
    }
    
}
