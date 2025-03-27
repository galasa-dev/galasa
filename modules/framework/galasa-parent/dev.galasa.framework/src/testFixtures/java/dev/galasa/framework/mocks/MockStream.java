/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.StreamsException;

public class MockStream implements IStream {

    private String name;
    private String description;
    private URL mavenRepositoryUrl;
    private URL testCatalogUrl;
    private List<IOBR> obrs;
    private boolean isEnabled = true;
    private boolean isValid = true;

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
    public List<IOBR> getObrs() {
        return this.obrs;
    }

    @Override
    public boolean isEnabled() {
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

    public void setObrs(List<IOBR> obrs) {
        this.obrs = obrs;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public void validate() throws StreamsException {
        if (!this.isValid) {
            throw new StreamsException("simulating an invalid stream!");
        }
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
}
