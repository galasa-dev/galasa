/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

/**
 * For use with the dummy RAS file system
 *
 *  
 *
 */
public class SSHBasicAttributes implements BasicFileAttributes {

    protected SSHBasicAttributes(SSHFileSystem filesystem) {
        // TODO create the attributes
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#lastModifiedTime()
     */
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.fromMillis(System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#lastAccessTime()
     */
    @Override
    public FileTime lastAccessTime() {
        return FileTime.fromMillis(System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#creationTime()
     */
    @Override
    public FileTime creationTime() {
        return FileTime.fromMillis(System.currentTimeMillis());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#isRegularFile()
     */
    @Override
    public boolean isRegularFile() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#isSymbolicLink()
     */
    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#isOther()
     */
    @Override
    public boolean isOther() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#size()
     */
    @Override
    public long size() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.attribute.BasicFileAttributes#fileKey()
     */
    @Override
    public Object fileKey() {
        return null;
    }

}
