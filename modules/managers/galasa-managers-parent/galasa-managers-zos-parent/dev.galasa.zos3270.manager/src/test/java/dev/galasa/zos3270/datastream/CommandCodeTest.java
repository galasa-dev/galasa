/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.CommandEraseWriteAlternate;
import dev.galasa.zos3270.spi.DatastreamException;

public class CommandCodeTest {

    @Test
    public void testCommandCodeDecipherValid() throws Exception {
        Assert.assertEquals("ERASE_WRITE is not correct", CommandEraseWrite.class,
                AbstractCommandCode.getCommandCode((byte) 0xf5).getClass());
        Assert.assertEquals("ERASE_WRITE_ALTERNATE is not correct", CommandEraseWriteAlternate.class,
                AbstractCommandCode.getCommandCode((byte) 0x7e).getClass());
    }

    @Test
    public void testCommandCodeDecipherUnsupported() throws Exception {
        unsupportedCC((byte) 0x0f);
        unsupportedCC((byte) 0x6f);
        Assert.assertTrue("Dummy assert to make sonarqube happy", true);
    }

    private void unsupportedCC(byte cc) {
        try {
            AbstractCommandCode.getCommandCode(cc);
            fail("Should have been unsupported");
        } catch (DatastreamException e) {
            Assert.assertTrue("Message incorrect", e.getMessage().contains("Unsupported command code=" + cc));
        }
    }

    @Test
    public void testCommandCodeDecipherInvalid() throws Exception {
        try {
            AbstractCommandCode.getCommandCode((byte) 9);
            fail("Should have been unsupported");
        } catch (DatastreamException e) {
            Assert.assertTrue("Message incorrect", e.getMessage().contains("Unrecognised command code=9"));
        }
    }

}
