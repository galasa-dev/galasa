/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks.cps;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MockConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {
    Map<String,String> props ;

    public MockConfigurationPropertyStoreService(Map<String,String> props) {
        this.props = props;
    }

    @Override
    public @Null String getProperty(@NotNull String prefix, @NotNull String suffix, String... infixes)
            throws ConfigurationPropertyStoreException {
        String key = prefix + "." + suffix ;
        String value = props.get(key);
        return value ;
    }

    @Override
    public @NotNull Map<String, String> getPrefixedProperties(@NotNull String prefix)
            throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getPrefixedProperties'");
    }

    @Override
    public void setProperty(@NotNull String name, @NotNull String value)
            throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'setProperty'");
    }

    @Override
    public void deleteProperty(@NotNull String name) throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteProperty'");
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