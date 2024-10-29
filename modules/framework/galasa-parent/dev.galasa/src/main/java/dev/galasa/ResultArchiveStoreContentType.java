/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa;

import java.nio.file.attribute.FileAttribute;

/**
 * Used to set the File Type for a Stored Artifact in the Result Archive Store
 *
 *  
 *
 */
public class ResultArchiveStoreContentType implements FileAttribute<String> {

    public static final String                        ATTRIBUTE_NAME = "contentType";

    public static final ResultArchiveStoreContentType TEXT           = new ResultArchiveStoreContentType("plain/text");
    public static final ResultArchiveStoreContentType XML            = new ResultArchiveStoreContentType(
            "application/xml");
    public static final ResultArchiveStoreContentType JSON           = new ResultArchiveStoreContentType(
            "application/json");
    public static final ResultArchiveStoreContentType BINARY         = new ResultArchiveStoreContentType(
            "application/octet-stream");
    public static final ResultArchiveStoreContentType ZIP            = new ResultArchiveStoreContentType(
            "application/zip");
    public static final ResultArchiveStoreContentType PNG            = new ResultArchiveStoreContentType("image/png");

    protected final String                            value;

    /**
     * Set the Content Type, eg application/xml
     *
     * @param value - the content type - should be similar to HTTP ContentTypes
     */
    public ResultArchiveStoreContentType(String value) {
        if ((value == null) || value.trim().isEmpty()) {
            this.value = "plain/text";
        } else {
            this.value = value;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.FileAttribute#name()
     */
    @Override
    public String name() {
        return ATTRIBUTE_NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.FileAttribute#value()
     */
    @Override
    public String value() {
        return this.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RasContentType=" + this.value;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ResultArchiveStoreContentType)) {
            return false;
        }
        return this.value.equals(((ResultArchiveStoreContentType) o).value);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

}
