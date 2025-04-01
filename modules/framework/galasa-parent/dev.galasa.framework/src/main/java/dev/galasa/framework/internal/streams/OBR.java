/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.StreamsException;

public class OBR implements IOBR {

    private static final String MAVEN_PREFIX = "mvn:";

    private final StringValidator validator = new StringValidator();

    private String groupId;
    private String artifactId;
    private String version;

    public OBR(String obrString) throws StreamsException {
        String formattedObrString = obrString.trim();
        String[] obrParts = getValidatedObrParts(formattedObrString);

        this.groupId = obrParts[0].trim();
        this.artifactId = obrParts[1].trim();
        this.version = obrParts[2].trim();
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return MAVEN_PREFIX + groupId + "/" + artifactId + "/" + version + "/obr";
    }

    private String[] getValidatedObrParts(String obrUrl) throws StreamsException {
        if (!obrUrl.startsWith(MAVEN_PREFIX)) {
            throw new StreamsException("Invalid OBR URL provided. Missing 'mvn:' protocol prefix.");
        }

        // Trim off the 'mvn:' prefix
        String validatedObrUrl = obrUrl;
        validatedObrUrl = validatedObrUrl.substring(MAVEN_PREFIX.length());

        // Trim off the trailing slash if there is one
        if (validatedObrUrl.endsWith("/")) {
            validatedObrUrl = validatedObrUrl.substring(0, validatedObrUrl.length() - 1);
        }

        // The OBR should now be in the form 'groupId/artifactId/version/obr', so split on the slashes
        String[] obrParts = validatedObrUrl.split("/");
        if (obrParts.length != 4) {
            throw new StreamsException("Invalid OBR URL provided. OBRs should be in the form 'mvn:groupId/artifactId/version/obr'.");
        }

        // Validate each part of the OBR
        for (String obrPart : obrParts) {
            if (!validator.isAlphanumericWithDashesUnderscoresAndDots(obrPart)) {
                throw new StreamsException("Invalid OBR provided. "+
                    "Only alphanumeric (A-Z, a-z, 0-9), '.', '-', and '_' characters are permitted.");
            }
        }
        return obrParts;
    }
}
