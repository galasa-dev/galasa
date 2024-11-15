/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import dev.galasa.framework.spi.Environment;

import java.util.HashMap;
import java.util.Map;
public class MockEnvironment implements Environment {

    private Map<String,String> envProps;

    public MockEnvironment() {
        this.envProps = new HashMap<String,String>();
    }

    public void setenv(String propertyName, String value ) {
        this.envProps.put(propertyName, value);
    }

    @Override
    public String getenv(String propertyName) {
        return this.envProps.get(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        throw new UnsupportedOperationException("Unimplemented method 'getProperty'");
    }

}
