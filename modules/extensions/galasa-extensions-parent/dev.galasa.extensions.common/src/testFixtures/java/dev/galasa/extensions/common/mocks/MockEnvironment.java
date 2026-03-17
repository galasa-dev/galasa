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

    private Map<String,String> envVars;
    private Map<String,String> systemProps;

    public MockEnvironment() {
        this.envVars = new HashMap<String,String>();
        this.systemProps = new HashMap<String,String>();
    }

    public void setenv(String propertyName, String value ) {
        this.envVars.put(propertyName, value);
    }

    public void setProperty(String propertyName, String value) {
        this.systemProps.put(propertyName, value);
    }

    @Override
    public String getenv(String propertyName) {
        return this.envVars.get(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        return this.systemProps.get(propertyName);
    }

}
