/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import dev.galasa.framework.spi.streams.IOBR;

public class MockOBR implements IOBR {

    private String groupId;
    private String artifactId;
    private String version;

    public MockOBR(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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
        return "mvn:" + this.groupId + "/" + this.artifactId + "/" + this.version + "/obr";
    }
}
