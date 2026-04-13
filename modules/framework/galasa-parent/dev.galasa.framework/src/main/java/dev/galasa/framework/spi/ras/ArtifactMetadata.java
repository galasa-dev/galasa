/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

public class ArtifactMetadata {

    private String path;
    private String contentType;
    private Long size;

    public ArtifactMetadata(String path, String contentType) {
        this.path = path;
        this.contentType = contentType;
    }

    public ArtifactMetadata(String path, String contentType, Long size) {
        this(path, contentType);
        this.size = size;
    }

    public String getPath() {
        return this.path;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Long getSize() {
        return this.size;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(Long size) {
        this.size = size;
    }

}
