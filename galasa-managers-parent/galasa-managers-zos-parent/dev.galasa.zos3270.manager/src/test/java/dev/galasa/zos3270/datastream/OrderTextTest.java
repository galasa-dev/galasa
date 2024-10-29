/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class OrderTextTest extends Zos3270TestBase {

    @Test
    public void testText() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.put((byte) 0xd4);
        buffer.put((byte) 0x89);
        buffer.put((byte) 0x92);
        buffer.put((byte) 0x85);
        buffer.flip();

        OrderText orderText = new OrderText(ebcdic);
        orderText.append(buffer.get());
        orderText.append(buffer.get());
        orderText.append(buffer.get());
        orderText.append(buffer.get());

        String result = orderText.toString();

        String shouldbe = "TEXT(Mike)";
        Assert.assertEquals("TEXT not translating correctly", shouldbe, result);
    }

}
