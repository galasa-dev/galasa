/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.mocks;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {

    private Map<String, String> properties;

    public MockConfigurationPropertyStoreService(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
        String propertyKey = prefix + "." + suffix;
        if (infixes.length > 0) {
            propertyKey = prefix + "." + String.join(".", infixes) + "." + suffix;
        }
        return this.properties.get(propertyKey);
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getPrefixedProperties'");
    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String value) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'setProperty'");
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
    }

    @Override
    public void deletePrefixedProperties(@NotNull String prefix) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deletePrefixedProperties'");
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

    @Override
    public void setProperties(Map<String, String> propertiesToSet) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'setProperties'");
    }
}
