/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

public class DummySocketImpl extends SocketImpl {

    private final ByteArrayInputStream  byteArrayInputStream;
    private final ByteArrayOutputStream byteArrayOutputStream;
    public int                          getInputStreamCount = 0;
    public int                          closeCount          = 0;

    public DummySocketImpl(ByteArrayInputStream byteArrayInputStream, ByteArrayOutputStream byteArrayOutputStream) {
        this.byteArrayInputStream = byteArrayInputStream;
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return null;
    }

    @Override
    protected void create(boolean stream) throws IOException {
    }

    @Override
    protected void connect(String host, int port) throws IOException {
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
    }

    @Override
    protected void listen(int backlog) throws IOException {
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        if (this.byteArrayInputStream == null) {
            throw new IOException("Dummy socket not initialised");
        }

        getInputStreamCount++;

        return this.byteArrayInputStream;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return this.byteArrayOutputStream;
    }

    @Override
    protected int available() throws IOException {
        return this.byteArrayInputStream.available();
    }

    @Override
    protected void close() throws IOException {
        closeCount++;
        if (this.byteArrayInputStream != null) {
            this.byteArrayInputStream.close();
        }
        if (this.byteArrayOutputStream != null) {
            this.byteArrayOutputStream.close();
        }
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
    }

}
