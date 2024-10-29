/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;


public class RasTestClass {
    
    public String testclass;
    public String bundle;

    public RasTestClass(String testClass, String bundleName) {
        this.testclass = testClass;
        this.bundle = bundleName;
    }

    public String getTestClass() {
        return this.testclass;
    }

    public void setTestClass(String testClass) {
        this.testclass = testClass;
    }

    public String getBundleName() {
        return this.bundle;
    }

    public void setBundleName(String bundleName) {
        this.bundle = bundleName;
    }

    
}