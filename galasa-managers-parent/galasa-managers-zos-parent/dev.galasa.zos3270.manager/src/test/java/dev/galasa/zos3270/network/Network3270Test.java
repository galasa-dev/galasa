/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.network;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class Network3270Test extends Zos3270TestBase {

    @Mock
    private Network network;

    @Before
    public void init(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testProcessMessage() throws NetworkException, IOException, TerminalInterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(AbstractCommandCode.ERASE_WRITE);
        baos.write(0x00);
        baos.write(OrderInsertCursor.ID);
        baos.write(NetworkThread.IAC);
        baos.write(NetworkThread.EOR);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        NetworkThread networkThread = new NetworkThread(null, CreateTestScreen(), null, bais);
        networkThread.processMessage(bais);

        Assert.assertTrue("Will test the screen at this point, later", true);
    }

    @Test
    public void testShortHeader() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x00);
        baos.write(0x00);
        baos.write(NetworkThread.IAC);
        baos.write(NetworkThread.EOR);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, null, bais);
            networkThread.processMessage(bais);
            fail("Should have thrown an error because header < 5");
        } catch (NetworkException e) {
            Assert.assertEquals("Error message incorrect", "Missing 5 bytes of the TN3270E datastream header",
                    e.getMessage());
        }

    }

    @Test
    public void testUnknownHeader() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0xff);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(0x00);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, network, bais);
            networkThread.processMessage(bais);
            fail("Should have thrown an error because unknown error");
        } catch (NetworkException e) {
            Assert.assertEquals("Error message incorrect", "Unrecognised IAC Command - ff00",
                    e.getMessage());
        }

    }

    @Test
    public void testDoTimingMark() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //x'fffd06 is IAC DO TIMING_MARK
        baos.write(0xff);
        baos.write(0xfd);
        baos.write(0x06);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, network, bais);
            networkThread.processMessage(bais);
        } catch (NetworkException e) {
            fail("Failed to process a IAC DO TIMING_MARK");
        }
    }

    @Test
    public void testWontTimingMark() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //x'fffc06 is IAC WONT TIMING_MARK
        baos.write(0xff);
        baos.write(0xfc);
        baos.write(0x06);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, network, bais);
            networkThread.processMessage(bais);
        } catch (NetworkException e) {
            fail("Failed to ignore a IAC WONT TIMING_MARK");
        }
    }

    @Test
    public void testShortTimingMark() throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //x'fffd is IAC DO
        baos.write(0xff);
        baos.write(0xfd);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        try {
            NetworkThread networkThread = new NetworkThread(null, null, network, bais);
            networkThread.processMessage(bais);
            fail("Should have thrown an exception due to a short IAC DO COMMAND");
        } catch (NetworkException e) {
            Assert.assertEquals("Error message incorrect", "Unrecognised IAC DO terminated early - fffd",
                    e.getMessage());
        }
    }

}
