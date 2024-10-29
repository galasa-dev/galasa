/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;

/**
 * Dummy Byte Channel for a null Result Archive Store
 *
 *  
 *
 */
public class ResultArchiveStoreByteChannel implements SeekableByteChannel {

    private static final String DUMMY_EXCEPTION = "Not available in dummy RAS channel";

    private boolean             open            = true;

    private int                 position        = 0;

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return this.open;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close() throws IOException {
        this.open = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!this.open) {
            throw new ClosedChannelException();
        }
        throw new IOException(DUMMY_EXCEPTION);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!this.open) {
            throw new ClosedChannelException();
        }
        final byte[] data = new byte[src.remaining()];
        src.get(data);
        this.position += data.length;
        return data.length;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position() throws IOException {
        return size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (!this.open) {
            throw new ClosedChannelException();
        }
        throw new IOException(DUMMY_EXCEPTION);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size() throws IOException {
        if (!this.open) {
            throw new ClosedChannelException();
        }
        return this.position;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (!this.open) {
            throw new ClosedChannelException();
        }
        throw new IOException(DUMMY_EXCEPTION);
    }

}
