/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import dev.galasa.framework.spi.streams.IStream;

public class Stream implements IStream {

    private String name;
    private String description;
    private String mavenRepositoryUrl;
    private String testCatalogUrl;
    private String obrLocation;
    private boolean isEnabled = true;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getMavenRepositoryUrl(){
        return this.mavenRepositoryUrl;
    }

    public void setMavenRepositoryUrl(String mavenRepositoryUrl) {
        this.mavenRepositoryUrl = mavenRepositoryUrl;
    }

    public String getTestCatalogUrl() {
        return this.testCatalogUrl;
    }

    public void setTestCatalogUrl(String testCatalogUrl) {
        this.testCatalogUrl = testCatalogUrl;
    }

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getObrLocation() {
        return this.obrLocation;
    }

    public void setObrLocation(String obrLocation) {
        this.obrLocation = obrLocation;
    }

}