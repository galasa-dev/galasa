/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

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
public class SSHBasicAttributesView implements BasicFileAttributeView {

    private final SSHFileSystem fileSystem;

    protected SSHBasicAttributesView(SSHFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

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
        return new SSHBasicAttributes(fileSystem);
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
