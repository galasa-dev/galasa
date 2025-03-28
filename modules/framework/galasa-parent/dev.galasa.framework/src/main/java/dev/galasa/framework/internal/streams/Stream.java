/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.StreamsException;

public class Stream implements IStream {

    private final StringValidator validator = new StringValidator();

    private String name;
    private String description;
    private URL mavenRepositoryUrl;
    private URL testCatalogUrl;
    private List<IOBR> obrs;
    private boolean isEnabled = true;

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) throws StreamsException {
        if (!validator.isAlphanumericWithDashesUnderscoresAndDots(name)) {
            throw new StreamsException("Invalid stream name provided. "+
                "Only alphanumeric (A-Z, a-z, 0-9), '.', '-', and '_' characters are permitted.");
        }
        this.name = name.trim();
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
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public List<IOBR> getObrs() {
        return this.obrs;
    }

    public void setObrsFromCommaSeparatedList(String commaSeparatedObrs) throws StreamsException {
        List<IOBR> formattedObrs = new ArrayList<>();
        if (commaSeparatedObrs != null && !commaSeparatedObrs.isBlank()) {
            for (String obrStr : commaSeparatedObrs.split(",")) {
                OBR obr = new OBR(obrStr);
                formattedObrs.add(obr);
            }
        }
        this.obrs = formattedObrs;
    }

    @Override
    public void validate() throws StreamsException {
        if (this.obrs == null || this.obrs.isEmpty()) {
            throw new StreamsException("No OBRs has been configured into the test stream");
        }

        if (this.mavenRepositoryUrl == null) {
            throw new StreamsException("No maven repository URL has been configured into the test stream");
        }

        if (this.testCatalogUrl == null) {
            throw new StreamsException("No testcatalog URL has been configured into the test stream");
        }
    }

}