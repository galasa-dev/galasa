/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.StreamsException;

public class Stream implements IStream {

    private String name;
    private String description;
    private URL mavenRepositoryUrl;
    private URL testCatalogUrl;
    private List<String> obrs;
    private boolean isEnabled = true;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public URL getMavenRepositoryUrl(){
        return this.mavenRepositoryUrl;
    }

    public void setMavenRepositoryUrl(String mavenRepositoryUrl) throws StreamsException {
        try {
            this.mavenRepositoryUrl = new URL(mavenRepositoryUrl);
        } catch (MalformedURLException e) {
            throw new StreamsException("Invalid maven repository URL provided", e);
        }
    }

    @Override
    public URL getTestCatalogUrl() {
        return this.testCatalogUrl;
    }

    public void setTestCatalogUrl(String testCatalogUrl) throws StreamsException {
        try {
            this.testCatalogUrl = new URL(testCatalogUrl);
        } catch (MalformedURLException e) {
            throw new StreamsException("Invalid testcatalog URL provided", e);
        }
    }

    @Override
    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public List<String> getObrs() {
        return this.obrs;
    }

    public void setObrs(String commaSeparatedObrs) {
        List<String> obrs = new ArrayList<>();
        if (commaSeparatedObrs != null && !commaSeparatedObrs.isBlank()) {
            obrs = Arrays.asList(commaSeparatedObrs.split(","));
        }
        this.obrs = obrs;
    }

    @Override
    public boolean isValid() {
        boolean isValid = (
            (this.obrs != null && !this.obrs.isEmpty())
            && (this.mavenRepositoryUrl != null && this.testCatalogUrl != null)
        );

        return isValid;
    }

}