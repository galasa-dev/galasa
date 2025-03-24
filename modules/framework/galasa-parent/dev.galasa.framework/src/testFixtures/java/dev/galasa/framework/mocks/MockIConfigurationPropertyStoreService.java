/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockIConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {

    private Map<String, String> properties;

    public MockIConfigurationPropertyStoreService() {
        this.properties = new HashMap<>();
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
        return null ;
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        Map<String, String> matchingProperties = new HashMap<>();

        for (Entry<String, String> entry : this.properties.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                matchingProperties.put(key, entry.getValue());
            }
        }
        return matchingProperties;
    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException {
        this.properties.put(name, value);
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
    }

    @Override
    public Map<String, String> getAllProperties() throws ConfigurationPropertyStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getAllProperties'");
    }

    @Override
    public String[] reportPropertyVariants(@NotNull String prefix, @NotNull String suffix, String... infixes) {
               throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariants'");
    }

    @Override
    public String reportPropertyVariantsString(@NotNull String prefix, @NotNull String suffix, String... infixes) {
               throw new UnsupportedOperationException("Unimplemented method 'reportPropertyVariantsString'");
    }

    @Override
    public List<String> getCPSNamespaces() throws ConfigurationPropertyStoreException {
               throw new UnsupportedOperationException("Unimplemented method 'getCPSNamespaces'");
    }
    
}
