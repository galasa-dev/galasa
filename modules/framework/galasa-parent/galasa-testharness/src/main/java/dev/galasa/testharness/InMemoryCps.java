/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.testharness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;

public class InMemoryCps implements IConfigurationPropertyStore {

    public final Properties properties = new Properties();

    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        return this.properties.getProperty(key);
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        HashMap<String, String> props = new HashMap<>();

        for(Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix)) {
                props.put(key, (String) entry.getValue());
            }
        }

        return props;
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        this.properties.put(key, value);
    }
    
    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        this.properties.remove(key);
    }

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) {
        String prefix = namespace + ".";
        int length = prefix.length();

        HashMap<String, String> props = new HashMap<>();

        for(Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(prefix)) {
                props.put(key.substring(length), (String) entry.getValue());
            }
        }

        return props;
    }

    @Override
    public List<String> getNamespaces() {
        HashSet<String> namespaces = new HashSet<>();


        for(Entry<Object, Object> entry : this.properties.entrySet()) {
            String key = (String) entry.getKey();
            int pos = key.indexOf('.');
            if (pos > 0) {
                String namespace = key.substring(0, pos);
                if (!namespaces.contains(namespace)) {
                    namespaces.add(namespace);
                }
            }
        }
        
        return new ArrayList<>(namespaces);
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
    }

}
