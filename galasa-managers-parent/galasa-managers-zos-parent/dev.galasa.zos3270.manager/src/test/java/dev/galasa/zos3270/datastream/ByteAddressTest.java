/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.spi.DatastreamException;

public class ByteAddressTest {

    @Test
    public void testCorner0() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x40);
        buffer.put((byte) 0x40);
        buffer.flip();

        BufferAddress sba = new BufferAddress(buffer);

        Assert.assertEquals("Should be address 0", 0, sba.getBufferAddress());
    }

    @Test
    public void testCorner79() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0xc1);
        buffer.put((byte) 0x4f);
        buffer.flip();

        BufferAddress sba = new BufferAddress(buffer);

        Assert.assertEquals("Should be address 79", 79, sba.getBufferAddress());
    }

    @Test
    public void testCorner1919() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x5d);
        buffer.put((byte) 0x7f);
        buffer.flip();

        BufferAddress sba = new BufferAddress(buffer);

        Assert.assertEquals("Should be address 1919", 1919, sba.getBufferAddress());
    }

    @Test
    public void testRange() throws DatastreamException {
        byte[] bytes = new byte[2];

        byte[] baChars = BufferAddress.getAddressChars();

        for (int high = 0; high < 64; high++) {
            for (int low = 0; low < 64; low++) {
                int address = (high * 64) + low;

                bytes[0] = baChars[high];
                bytes[1] = baChars[low];

                BufferAddress sba = new BufferAddress(ByteBuffer.wrap(bytes));

                Assert.assertEquals("Calculated Buffer Address address is incorrect", address, sba.getBufferAddress());

                byte[] chars = sba.getCharRepresentation();
                Assert.assertEquals("Char Buffer Address adress byte 0 is wrong", bytes[0], chars[0]);
                Assert.assertEquals("Char Buffer Address adress byte 1 is wrong", bytes[1], chars[1]);
            }
        }

    }

    @Test
    public void testEarlyTermination() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x5d);
        buffer.flip();

        try {
            new BufferAddress(buffer);
            fail("Should have terminated early exception");
        } catch (DatastreamException e) {
            Assert.assertTrue("Terminated early exception incorrect", e.getMessage().contains("terminated too early"));
        }
    }

    @Test
    public void testBit14Address() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x3f);
        buffer.put((byte) 0xff);
        buffer.flip();

        BufferAddress sba = new BufferAddress(buffer);

        Assert.assertEquals("Should be address 16383", 16383, sba.getBufferAddress());
    }

}
