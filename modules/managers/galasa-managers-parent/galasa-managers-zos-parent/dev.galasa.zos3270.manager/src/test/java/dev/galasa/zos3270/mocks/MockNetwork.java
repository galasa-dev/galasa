/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.mocks;

import java.io.IOException;
import java.io.OutputStream;

import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.spi.NetworkException;

public class MockNetwork extends Network {

    private OutputStream outputStream;

    public MockNetwork(OutputStream outputStream) {
        super(null, 0, null);
        this.outputStream = outputStream;
    }

    @Override
    public void sendIac(byte[] outboundIac) throws NetworkException {
        synchronized(outputStream) {
            try {
                outputStream.write(outboundIac);
                outputStream.flush();
            } catch (IOException e) {
                throw new NetworkException("Unable to write outbound iac", e);
            }
        }
    }
}
