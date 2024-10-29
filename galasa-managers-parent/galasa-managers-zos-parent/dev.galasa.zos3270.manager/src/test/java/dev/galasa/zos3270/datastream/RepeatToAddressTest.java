/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class RepeatToAddressTest extends Zos3270TestBase {

    @Test
    public void testRA() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put((byte) 0x5d);
        buffer.put((byte) 0x7f);
        buffer.put((byte) 0xd4);
        buffer.flip();

        String result = new OrderRepeatToAddress(buffer, ebcdic).toString();

        String shouldbe = "RA(M,1919)";
        Assert.assertEquals("RA not translating correct to " + shouldbe, shouldbe, result);
    }

}
