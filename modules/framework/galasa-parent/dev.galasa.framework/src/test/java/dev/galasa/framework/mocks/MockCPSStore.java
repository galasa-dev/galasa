/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockCPSStore implements IConfigurationPropertyStore, IConfigurationPropertyStoreService {

    Map<String,String> properties ;

    public MockCPSStore(@NotNull Map<String,String> properties ) {
        this.properties = properties ;
    }

    @Override
    public @Null String getProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        return this.properties.get(key);
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getPrefixedProperties'");
    }

    @Override
    public void setProperty(@NotNull String key, @NotNull String value) throws ConfigurationPropertyStoreException {
        this.properties.put(key, value);
    }

    @Override
    public void deleteProperty(@NotNull String key) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
    }

    @Override
    public Map<String, String> getPropertiesFromNamespace(String namespace) {
        throw new UnsupportedOperationException("Unimplemented method 'getPropertiesFromNamespace'");
    }

    @Override
    public List<String> getNamespaces() {
        throw new UnsupportedOperationException("Unimplemented method 'getNamespaces'");
    }

    @Override
    public void shutdown() throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {

        String result = null ;

        List<String> infixList = new ArrayList<>();
        for (String infix : infixes ) {
            infixList.add(infix);
        }

        infixList.add("");

        for (String infix : infixList ) {
            String key ;
            if (infix.trim().isEmpty()) {
                key = prefix + "." + suffix;
            } else {
                key = prefix + "." + infix + "." + suffix;
            }
            String prop = this.properties.get(key);
            if (prop != null) {
                result = prop;
                break;
            }
        }

        return result;
    }

    @Override
    public Map<String, String> getAllProperties() {
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
    public List<String> getCPSNamespaces() {
        throw new UnsupportedOperationException("Unimplemented method 'getCPSNamespaces'");
    }

}
