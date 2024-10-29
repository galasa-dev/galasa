/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * For use with the dummy RAS file system
 *
 *  
 *
 */
public class ResultArchiveStoreBasicAttributesView implements BasicFileAttributeView {

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributeView#name()
     */
    @Override
    public String name() {
        return "ras";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributeView#readAttributes()
     */
    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return new ResultArchiveStoreBasicAttributes();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributeView#setTimes(java.nio.file.
     * attribute.FileTime, java.nio.file.attribute.FileTime,
     * java.nio.file.attribute.FileTime)
     */
    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        // Dummy RA, not going to do anything
    }

}