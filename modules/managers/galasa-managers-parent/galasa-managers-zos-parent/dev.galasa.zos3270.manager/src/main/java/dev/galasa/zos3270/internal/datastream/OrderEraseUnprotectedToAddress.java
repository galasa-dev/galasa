/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderEraseUnprotectedToAddress extends AbstractOrder {

    public static final byte    ID = 0x12;

    private final BufferAddress bufferAddress;

    public OrderEraseUnprotectedToAddress(ByteBuffer buffer) throws DatastreamException {
        this.bufferAddress = new BufferAddress(buffer);
    }

    public OrderEraseUnprotectedToAddress(BufferAddress bufferAddress) {
        this.bufferAddress = bufferAddress;
    }

    @Override
    public String toString() {
        return "EUA(" + bufferAddress + ")";
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
