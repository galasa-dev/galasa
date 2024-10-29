/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.spi.DatastreamException;

public class SetBufferAddressTest {

    @Test
    public void testRA() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put((byte) 0x5d);
        buffer.put((byte) 0x7f);
        buffer.flip();

        String result = new OrderSetBufferAddress(buffer).toString();

        String shouldbe = "SBA(1919)";
        Assert.assertEquals("SBA not translating correct to " + shouldbe, shouldbe, result);
    }

}
