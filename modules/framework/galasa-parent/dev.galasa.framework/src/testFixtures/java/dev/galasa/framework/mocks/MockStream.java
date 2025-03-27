/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dev.galasa.framework.spi.streams.IStream;

public class MockStream implements IStream {

    private String name;
    private String description;
    private URL mavenRepositoryUrl;
    private URL testCatalogUrl;
    private List<String> obrs;
    private boolean isEnabled = true;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public URL getMavenRepositoryUrl() {
        return mavenRepositoryUrl;
    }

    @Override
    public URL getTestCatalogUrl() {
        return testCatalogUrl;
    }

    @Override
    public List<String> getObrs() {
        return this.obrs;
    }

    @Override
    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMavenRepositoryUrl(String mavenRepositoryUrl) throws MalformedURLException {
        this.mavenRepositoryUrl = new URL(mavenRepositoryUrl);
    }

    public void setTestCatalogUrl(String testCatalogUrl) throws MalformedURLException {
        this.testCatalogUrl = new URL(testCatalogUrl);
    }

    public void setObrs(List<String> obrs) {
        this.obrs = obrs;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public boolean isValid() {
        boolean isValid = (
            (this.obrs != null && !this.obrs.isEmpty())
            && (this.testCatalogUrl != null && this.mavenRepositoryUrl != null)
        );
        return isValid;
    }
}
