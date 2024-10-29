/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import dev.galasa.ipnetwork.SSHException;

/**
 * Dummy Byte Channel for a null Result Archive Store
 *
 *  
 *
 */
public class SSHByteChannel implements SeekableByteChannel {

    private long                size     = 0;
    private long                position = 0;

    private final SSHFileSystem fileSystem;
    private final Path          path;

    private final ChannelSftp   channel;

    private final InputStream   inputStream;
    private final OutputStream  outputStream;

    public SSHByteChannel(Path path, Set<? extends OpenOption> options, SSHFileSystem fileSystem) throws SSHException {
        this.fileSystem = fileSystem;
        this.path = path.toAbsolutePath();

        this.channel = this.fileSystem.getFileChannel();

        boolean write = options.contains(StandardOpenOption.WRITE);

        try {
            if (write) {
                inputStream = null;

                outputStream = this.channel.put(this.path.toString());
            } else {
                outputStream = null;

                // Check the file exists and get the size
                SftpATTRS attrs = this.channel.lstat(this.path.toString());
                this.size = attrs.getSize();

                inputStream = this.channel.get(this.path.toString());
            }
        } catch (SftpException e) {
            throw new SSHException("Unable to open SSH file " + this.path, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#isOpen()
     */
    @Override
    public boolean isOpen() {
        return this.channel.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.Channel#close()
     */
    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        this.channel.disconnect();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#read(java.nio.ByteBuffer)
     */
    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        
        byte[] data = new byte[dst.remaining()];
        int len = this.inputStream.read(data);
        if (len < 0) {
            return len;
        }
        
        dst.put(data, 0, len);
        
        position = position + len;
        
        
        
//        int count = 0;
//        byte[] buffer = new byte[1];
//        for (int i = 0; i < dst.remaining(); i++) {
//            int len = this.inputStream.read(buffer);
//            if (len < 1) {
//                break;
//            }
//            dst.put(buffer, 0, len);
//            count = count + len;
//            position = position + len;
//        }

        return len;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#write(java.nio.ByteBuffer)
     */
    @Override
    public int write(ByteBuffer src) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }

        int len = src.remaining();
        byte[] data = new byte[len];
        src.get(data, 0, len);
        
        outputStream.write(data, 0, len);
        size = size + len;
        position = position + len;
        
        return len;
        
//        int count = 0;
//        while (src.remaining() > 0) { // TODO there has got be a more efficient way of doing this
//            outputStream.write(lsrc.get());
//            count++;
//            size++;
//            position++;
//        }
//
//        return count;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position()
     */
    @Override
    public long position() throws IOException {
        return position;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#position(long)
     */
    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        throw new UnsupportedOperationException("need to write");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#size()
     */
    @Override
    public long size() throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        return this.size;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.channels.SeekableByteChannel#truncate(long)
     */
    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        throw new UnsupportedOperationException("need to write");
    }

}
