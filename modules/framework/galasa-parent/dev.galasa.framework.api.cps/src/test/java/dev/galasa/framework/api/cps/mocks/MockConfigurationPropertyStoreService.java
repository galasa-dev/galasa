/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.cps.mocks;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

import java.util.List;
import java.util.Map;

public class MockConfigurationPropertyStoreService implements IConfigurationPropertyStoreService {

    @Override
    public String getProperty(String prefix, String suffix, String... infixes) throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Map<String, String> getPrefixedProperties(String prefix) throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void setProperty(String name, String value) throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public void deleteProperty(String name) throws ConfigurationPropertyStoreException {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public Map<String, String> getAllProperties() {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String[] reportPropertyVariants(String prefix, String suffix, String... infixes) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public String reportPropertyVariantsString(String prefix, String suffix, String... infixes) {
        throw new MockMethodNotImplementedException();
    }

    @Override
    public List<String> getCPSNamespaces() {
        throw new MockMethodNotImplementedException();
    }
}
