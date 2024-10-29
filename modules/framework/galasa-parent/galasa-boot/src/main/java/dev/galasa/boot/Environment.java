/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

/** 
 * An interface through which we can get environment variable values. 
 * Allows for easy mocking of the System class, which is notoriously hard to mock
 * out for unit tests.
 */
public interface Environment {

    /** Gets the value of a given environment variable. */
    String getenv(String propertyName);

    /** Gets a system property */
    String getProperty(String propertyName);
}
