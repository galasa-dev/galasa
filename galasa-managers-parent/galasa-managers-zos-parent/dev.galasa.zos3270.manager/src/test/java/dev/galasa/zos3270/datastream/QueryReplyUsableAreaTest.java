/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.datastream.QueryReplyUsableArea;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class QueryReplyUsableAreaTest extends Zos3270TestBase {

    @Test
    public void testGoldenPath() throws TerminalInterruptedException {
        Screen screen = CreateTestScreen(80, 24, null);

        QueryReplyUsableArea qrua = new QueryReplyUsableArea(screen);

        String shouldBe = Hex.encodeHexString(new byte[] { (byte) 0x00, // Length1
                (byte) 0x17, // Length2
                (byte) 0x81, // Query Reply
                (byte) 0x81, // Usable Area
                (byte) 0x01, // 12/14 bit addressing
                (byte) 0x00, // Variable
                (byte) 0x00, (byte) 0x50, // Columns
                (byte) 0x00, (byte) 0x18, // Rows
                (byte) 0x01, // mm
                (byte) 0x00, (byte) 0x0a, (byte) 0x02, (byte) 0xe5, // Xr
                (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x6f, // Yr
                (byte) 0x09, // AW
                (byte) 0x0c, // AH
                (byte) 0x07, (byte) 0x80 }); // Buffsize

        String actual = Hex.encodeHexString(qrua.toByte());

        Assert.assertEquals("response from Usable area is incorrect", shouldBe, actual);

    }

}
