/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.Map;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Property;

public class MockCapability implements Capability {

    private String capabilityName;
    private Map<String, Object> propertiesMap;

    public MockCapability(String capabilityName, Map<String, Object> propertiesMap) {
        this.capabilityName = capabilityName;
        this.propertiesMap = propertiesMap;
    }

    @Override
    public String getName() {
        return this.capabilityName;
    }

    @Override
    public Map<String, Object> getPropertiesAsMap() {
        return this.propertiesMap;
    }

    @Override
    public Map<String, String> getDirectives() {
        throw new UnsupportedOperationException("Unimplemented method 'getDirectives'");
    }

    @Override
    public Property[] getProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }
}
