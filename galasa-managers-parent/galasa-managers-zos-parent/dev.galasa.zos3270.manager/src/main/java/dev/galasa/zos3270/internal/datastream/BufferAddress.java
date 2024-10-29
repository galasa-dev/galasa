/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;

public class BufferAddress {

    protected static final byte[] chars   = new byte[] { (byte) 0x40, (byte) 0xc1, (byte) 0xc2, (byte) 0xc3,
            (byte) 0xc4, (byte) 0xc5, (byte) 0xc6, (byte) 0xc7, (byte) 0xc8, (byte) 0xc9, (byte) 0x4a, (byte) 0x4b,
            (byte) 0x4c, (byte) 0x4d, (byte) 0x4e, (byte) 0x4f, (byte) 0x50, (byte) 0xd1, (byte) 0xd2, (byte) 0xd3,
            (byte) 0xd4, (byte) 0xd5, (byte) 0xd6, (byte) 0xd7, (byte) 0xd8, (byte) 0xd9, (byte) 0x5a, (byte) 0x5b,
            (byte) 0x5c, (byte) 0x5d, (byte) 0x5e, (byte) 0x5f, (byte) 0x60, (byte) 0x61, (byte) 0xe2, (byte) 0xe3,
            (byte) 0xe4, (byte) 0xe5, (byte) 0xe6, (byte) 0xe7, (byte) 0xe8, (byte) 0xe9, (byte) 0x6a, (byte) 0x6b,
            (byte) 0x6c, (byte) 0x6d, (byte) 0x6e, (byte) 0x6f, (byte) 0xf0, (byte) 0xf1, (byte) 0xf2, (byte) 0xf3,
            (byte) 0xf4, (byte) 0xf5, (byte) 0xf6, (byte) 0xf7, (byte) 0xf8, (byte) 0xf9, (byte) 0x7a, (byte) 0x7b,
            (byte) 0x7c, (byte) 0x7d, (byte) 0x7e, (byte) 0x7f };

    private int                   address = 0;

    public BufferAddress(ByteBuffer buffer) throws DatastreamException {
        byte[] data = new byte[4];
        try {
            data[0] = 0;
            data[1] = 0;
            data[2] = buffer.get();
            data[3] = buffer.get();
        } catch (BufferUnderflowException e) {
            throw new DatastreamException("Buffer Address terminated too early", e);
        }

        ByteBuffer toInt = ByteBuffer.wrap(data);

        int preConv = toInt.getInt();

        if ((preConv & 0xc000) == 0) {
            this.address = preConv;
        } else {
            int left = (preConv & 0x3f00) >> 2;
            int right = (preConv & 0x3f);

            this.address = left | right;
        }
    }

    public BufferAddress(int address) {
        this.address = address;
    }

    public byte[] getCharRepresentation() {
        int left = (address & 0xfc0) >> 6;
        int right = (address & 0x3f);

        byte[] output = new byte[2];
        output[0] = chars[left];
        output[1] = chars[right];

        return output;
    }

    public int getBufferAddress() {
        return this.address;
    }

    public ByteBuffer getByteBufferAddress() {
        return ByteBuffer.wrap(getCharRepresentation());
    }

    @Override
    public String toString() {
        return Integer.toString(address);
    }

    public static byte[] getAddressChars() {
        return chars;
    }

}
