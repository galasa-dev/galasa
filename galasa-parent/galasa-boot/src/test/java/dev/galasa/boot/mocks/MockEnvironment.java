/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.util.*;

import dev.galasa.boot.Environment;

/**
 * An implementation of the Environment interface which allows code to get
 * environment variables and system properties from the real system.
 */
public class MockEnvironment implements Environment {

    Map<String,String> envProps ;
    Map<String,String> sysProps ;

    public MockEnvironment(Map<String,String> envProps,Map<String,String> sysProps) {
        this.envProps = envProps;
        this.sysProps = sysProps;
    }

    public MockEnvironment() {
        this.envProps = new HashMap<String,String>();
        this.sysProps = new HashMap<String,String>();
    }

    public void setenv(String propertyName, String value ) {
        this.envProps.put(propertyName, value);
    }

    public void setProperty(String propertyName, String value) {
        this.sysProps.put(propertyName,value);
    }

    @Override
    public String getenv(String propertyName) {
        return this.envProps.get(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        return this.sysProps.get(propertyName);
    }


    
}
