/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.nio.file.FileSystem;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.ras.ResultArchiveStorePath;

public class CouchdbArtifactPath extends ResultArchiveStorePath {

    private String  contentType;
    private int     length;

    private String  artifactRecordId;

    private boolean directory = false;

    public CouchdbArtifactPath(@NotNull FileSystem fileSystem, String pathName) {
        super(fileSystem, pathName);
    }

    protected CouchdbArtifactPath(FileSystem fileSystem, boolean b, List<String> nameElements, int i, int size) {
        super(fileSystem, b, nameElements, i, size);
    }

    protected CouchdbArtifactPath(@NotNull FileSystem fileSystem, String pathName, JsonObject artifactDetails,
            String artifactRecordId) {
        super(fileSystem, pathName);
        this.artifactRecordId = artifactRecordId;

        JsonElement ct = artifactDetails.get("content_type");
        if (ct != null) {
            this.contentType = ct.getAsString();
        } else {
            this.contentType = "unknown";
        }

        JsonElement len = artifactDetails.get("length");
        if (len != null) {
            this.length = len.getAsInt();
        } else {
            this.length = 0;
        }
    }

    @Override
    public CouchdbArtifactPath getParent() {
        if (this.nameElements.isEmpty()) {
            return null;
        }

        CouchdbArtifactPath parentPath = new CouchdbArtifactPath(this.fileSystem, this.absolute, this.nameElements, 0,
                this.nameElements.size() - 1);
        parentPath.setDirectory(true);

        return parentPath;
    }

    private void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public CouchdbArtifactPath toAbsolutePath() {
        if (this.absolute) {
            return this;
        }

        return new CouchdbArtifactPath(this.fileSystem, true, this.nameElements, 0, this.nameElements.size());
    }

    public String getArtifactRecordId() {
        return this.artifactRecordId;
    }

    public CoucbDbBasicAttributes readAttributes() {
        return new CoucbDbBasicAttributes(this);
    }

    public boolean isDirectory() {
        return directory;
    }

    public long getLength() {
        return this.length;
    }

    public String getContentType() {
        return contentType;
    }
    
    @Override
    protected ResultArchiveStorePath newPathObject(String newPath) {
        return new CouchdbArtifactPath(fileSystem, newPath);
    }
    
    @Override
    protected ResultArchiveStorePath newPathObject(boolean absolute, List<String> nameElements, int start, int end) {
        return new CouchdbArtifactPath(this.fileSystem, absolute, nameElements, start, end);
    }

}
