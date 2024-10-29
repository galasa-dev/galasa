/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderSetBufferAddress extends AbstractOrder {

    public static final byte    ID = 0x11;

    private final BufferAddress bufferAddress;

    public OrderSetBufferAddress(ByteBuffer buffer) throws DatastreamException {
        this.bufferAddress = new BufferAddress(buffer);
    }

    public OrderSetBufferAddress(BufferAddress bufferAddress) {
        this.bufferAddress = bufferAddress;
    }

    @Override
    public String toString() {
        return "SBA(" + bufferAddress + ")";
    }

    public int getBufferAddress() {
        return this.bufferAddress.getBufferAddress();
    }

    public byte[] getCharRepresentation() {
        byte[] repBA = this.bufferAddress.getCharRepresentation();

        byte[] rep = new byte[3];
        rep[0] = ID;
        rep[1] = repBA[0];
        rep[2] = repBA[1];

        return rep;
    }

    @Override
    public byte[] getBytes() {
        return getCharRepresentation();
    }

}
