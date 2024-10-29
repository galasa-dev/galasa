/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dummy Byte Channel for a null Result Archive Store
 *
 * @author Michael Baylis
 *
 */
public class CouchdbRasReadByteChannel implements SeekableByteChannel {

    private final Path                cachePath;
    private final SeekableByteChannel cacheByteChannel;

    public CouchdbRasReadByteChannel(Path cachePath) throws IOException {
        this.cachePath = cachePath;
        this.cacheByteChannel = Files.newByteChannel(cachePath);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return cacheByteChannel.isOpen();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close() throws IOException {
        cacheByteChannel.close();
        try {
            Files.delete(cachePath);
        } catch (Exception e) {
        } // *** Hide any delete problems
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        return cacheByteChannel.read(dst);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        return cacheByteChannel.write(src);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position() throws IOException {
        return cacheByteChannel.position();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        return cacheByteChannel.position(newPosition);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size() throws IOException {
        return cacheByteChannel.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        return cacheByteChannel.truncate(size);
    }

}
