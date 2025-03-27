/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.streams;

import dev.galasa.framework.spi.streams.IOBR;

public class OBR implements IOBR {

    private static final String MAVEN_PREFIX = "mvn:";

    private String groupId;
    private String artifactId;
    private String version;

    public OBR(String obrString) {
        String formattedObrString = obrString.trim();

        // Trim off the trailing '/' if there is one
        if (formattedObrString.endsWith("/")) {
            formattedObrString = formattedObrString.substring(0, formattedObrString.length() - 1);
        }

        // Trim off the 'mvn:' prefix from the OBR
        if (formattedObrString.startsWith(MAVEN_PREFIX)) {
            formattedObrString = formattedObrString.substring(MAVEN_PREFIX.length());
        }

        // The OBR should now be in the form '<group-id>/<artifact-id>/<version>/obr', so split on the slashes
        String[] obrParts = formattedObrString.split("/");
        if (obrParts.length == 4) {
            this.groupId = obrParts[0].trim();
            this.artifactId = obrParts[1].trim();
            this.version = obrParts[2].trim();
        }
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return MAVEN_PREFIX + groupId + "/" + artifactId + "/" + version + "/obr";
    }
}
