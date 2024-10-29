/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

/**
 * An implementation of the Environment interface which allows code to get
 * environment variables from the real system.
 */
public class SystemEnvironment implements Environment {

    @Override
    public String getenv(String propertyName) {
        return System.getenv(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }
    
}
