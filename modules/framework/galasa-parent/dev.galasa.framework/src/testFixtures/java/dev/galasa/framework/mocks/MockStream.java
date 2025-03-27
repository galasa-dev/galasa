/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.spi.streams.IStream;

public class MockStream implements IStream {

    private String name;
    private String description;
    private String mavenRepositoryUrl;
    private String testCatalogUrl;
    private String obrLocation;
    private boolean isEnabled = true;

    public MockStream(String name, String description, String mavenRepositoryUrl, String testCatalogUrl, String obrLocation, boolean isEnabled) {
        this.name = name;
        this.description = description;
        this.mavenRepositoryUrl = mavenRepositoryUrl;
        this.testCatalogUrl = testCatalogUrl;
        this.obrLocation = obrLocation;
        this.isEnabled = isEnabled;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMavenRepositoryUrl() {
        return mavenRepositoryUrl;
    }

    @Override
    public String getTestCatalogUrl() {
        return testCatalogUrl;
    }

    @Override
    public String getObrLocation() {
        return obrLocation;
    }

    @Override
    public boolean getIsEnabled() {
        return isEnabled;
    }
    
}
