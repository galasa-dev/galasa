/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;

public class StructuredField3270DS extends StructuredField {

    private Inbound3270Message inbound3270Message;

    public StructuredField3270DS(byte[] structuredFieldData, Charset codePage) throws NetworkException {
        if (structuredFieldData.length < 3) {
            throw new DatastreamException("Structured Field 3270 DS length < 3 bytes");
        }

        AbstractCommandCode commandCode = AbstractCommandCode.getCommandCode(structuredFieldData[2]);

        ByteBuffer buffer = ByteBuffer.wrap(structuredFieldData, 3, structuredFieldData.length - 3);

        inbound3270Message = NetworkThread.process3270Datastream(commandCode, buffer, codePage);
    }

    public Inbound3270Message getInboundMessage() {
        return this.inbound3270Message;
    }

}
