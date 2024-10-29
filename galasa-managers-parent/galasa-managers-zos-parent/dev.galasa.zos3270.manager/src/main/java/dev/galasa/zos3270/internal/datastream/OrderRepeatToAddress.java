/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderRepeatToAddress extends AbstractOrder {

    private final Charset ebcdic;

    public static final byte     ID     = 0x3c;

    private final BufferAddress  bufferAddress;

    private final char           repeatChar;

    public OrderRepeatToAddress(ByteBuffer buffer, Charset codePage) throws DatastreamException {
        this.ebcdic = codePage;
        this.bufferAddress = new BufferAddress(buffer);

        byte[] charByte = new byte[] { buffer.get() };
        if (charByte[0] == -1) {
            charByte[0] = 0x00;
        }

        repeatChar = ebcdic.decode(ByteBuffer.wrap(charByte)).array()[0];
    }

    public OrderRepeatToAddress(char repeatChar, BufferAddress bufferAddress, Charset codePage) {
        this.ebcdic = codePage;
        this.bufferAddress = bufferAddress;
        this.repeatChar = repeatChar;
    }

    @Override
    public String toString() {
        return "RA(" + repeatChar + "," + bufferAddress + ")";
    }

    public int getBufferAddress() {
        return this.bufferAddress.getBufferAddress();
    }

    public char getChar() {
        return this.repeatChar;
    }

    public byte[] getBytes() {
        byte[] ba = this.bufferAddress.getCharRepresentation();

        byte[] buffer = new byte[4];
        buffer[0] = ID;
        buffer[1] = ba[0];
        buffer[2] = ba[1];
        buffer[3] = String.valueOf(repeatChar).getBytes(ebcdic)[0];

        return buffer;
    }

}
