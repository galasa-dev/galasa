/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class DummySocket extends Socket {

    private final DummySocketImpl impl;

    public boolean testClosed = false;
    public boolean connected  = true;

    public DummySocket(DummySocketImpl impl) throws SocketException {
        super(impl);
        this.impl = impl;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    // Java 21 tightened SocketImpl delegation; override methods that fail when
    // Socket(SocketImpl) is used with a custom impl in Java 21.
    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return impl.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return impl.getOutputStream();
    }

    @Override
    public synchronized void close() throws IOException {
        this.testClosed = true;

        super.close();
    }

}
