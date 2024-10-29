/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public enum Api {
    AUTHENTICATION("auth", "authentication"),
    TESTCATALOG("testcatalog", "testcatalog"),
    RUN("run", "run");

    private final String suffix;
    private final String property;

    private Api(String suffix, String property) {
        this.suffix = suffix;
        this.property = property;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getProperty() {
        return property;
    }
}
