/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.util;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class DummySocket extends Socket {

    public boolean testClosed = false;
    public boolean connected  = true;

    public DummySocket(DummySocketImpl impl) throws SocketException {
        super(impl);
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public synchronized void close() throws IOException {
        this.testClosed = true;

        super.close();
    }

}
