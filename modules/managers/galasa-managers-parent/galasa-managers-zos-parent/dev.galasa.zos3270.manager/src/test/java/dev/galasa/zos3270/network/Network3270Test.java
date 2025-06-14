/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.network;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.mocks.MockNetwork;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class Network3270Test extends Zos3270TestBase {

    @Mock
    private Network network;

    @Before
    public void init(){
        MockitoAnnotations.openMocks(this);
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

    @Test
    public void testTerminalDeviceTypeWithoutNameRequestBuildsCorrectByteStream() throws Exception {
        // Given...
        ByteArrayOutputStream inboundStream = new ByteArrayOutputStream();
        inboundStream.write(NetworkThread.IAC);
        inboundStream.write(NetworkThread.SB);
        inboundStream.write(NetworkThread.TN3270E);
        inboundStream.write(NetworkThread.SEND);
        inboundStream.write(NetworkThread.DEVICE_TYPE);
        inboundStream.write(NetworkThread.IAC);
        inboundStream.write(NetworkThread.SE);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(inboundStream.toByteArray());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Network network = new MockNetwork(outputStream);

        List<String> mockDeviceTypes = new ArrayList<>();
        String mockDeviceType = "my-3270-device-type";
        byte[] mockDeviceTypeAsBytes = mockDeviceType.getBytes("us-ascii");
        mockDeviceTypes.add(mockDeviceType);

        NetworkThread networkThread = new NetworkThread(null, CreateTestScreen(), network, inputStream, mockDeviceTypes);

        // When...
        networkThread.processMessage(inputStream);

        // Then...
        // We expect to have sent a telnet command of IAC SB TN3270E DEVICE-TYPE REQUEST <device-type> IAC SE
        ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
        expectedBytes.write(NetworkThread.IAC);
        expectedBytes.write(NetworkThread.SB);
        expectedBytes.write(NetworkThread.TN3270E);
        expectedBytes.write(NetworkThread.DEVICE_TYPE);
        expectedBytes.write(NetworkThread.REQUEST);
        expectedBytes.write(mockDeviceTypeAsBytes);
        expectedBytes.write(NetworkThread.IAC);
        expectedBytes.write(NetworkThread.SE);

        assertThat(outputStream.toByteArray()).isEqualTo(expectedBytes.toByteArray());
    }

    @Test
    public void testTerminalDeviceTypeWithNameRequestBuildsCorrectByteStream() throws Exception {
        // Given...
        ByteArrayOutputStream inboundStream = new ByteArrayOutputStream();
        inboundStream.write(NetworkThread.IAC);
        inboundStream.write(NetworkThread.SB);
        inboundStream.write(NetworkThread.TN3270E);
        inboundStream.write(NetworkThread.SEND);
        inboundStream.write(NetworkThread.DEVICE_TYPE);
        inboundStream.write(NetworkThread.IAC);
        inboundStream.write(NetworkThread.SE);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(inboundStream.toByteArray());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Network network = new MockNetwork(outputStream);

        Charset ascii7 = Charset.forName("us-ascii");
        List<String> mockDeviceTypes = new ArrayList<>();
        String mockDeviceType = "my-3270-device-type";
        byte[] mockDeviceTypeAsBytes = mockDeviceType.getBytes(ascii7);
        mockDeviceTypes.add(mockDeviceType);

        String mockDeviceName = "THIS_IS_A_DEVICE_NAME";
        byte[] mockDeviceNameAsBytes = mockDeviceName.getBytes(ascii7);

        Screen mockScreen = CreateTestScreen();
        TerminalSize primarySize = new TerminalSize(mockScreen.getPrimaryColumns(), mockScreen.getPrimaryRows());
        TerminalSize alternateSize = new TerminalSize(mockScreen.getAlternateColumns(), mockScreen.getAlternateRows());
        Terminal terminal = new Terminal("terminal1", "host", 0, false, primarySize, alternateSize, null, ebcdic);
        terminal.setRequestedDeviceName(mockDeviceName);

        NetworkThread networkThread = new NetworkThread(terminal, mockScreen, network, inputStream, mockDeviceTypes);

        // When...
        networkThread.processMessage(inputStream);

        // Then...
        // We expect to have sent a telnet command of IAC SB TN3270E DEVICE-TYPE REQUEST <device-type> CONNECT <device-name> IAC SE
        ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
        expectedBytes.write(NetworkThread.IAC);
        expectedBytes.write(NetworkThread.SB);
        expectedBytes.write(NetworkThread.TN3270E);
        expectedBytes.write(NetworkThread.DEVICE_TYPE);
        expectedBytes.write(NetworkThread.REQUEST);
        expectedBytes.write(mockDeviceTypeAsBytes);
        expectedBytes.write(NetworkThread.CONNECT);
        expectedBytes.write(mockDeviceNameAsBytes);
        expectedBytes.write(NetworkThread.IAC);
        expectedBytes.write(NetworkThread.SE);

        assertThat(outputStream.toByteArray()).isEqualTo(expectedBytes.toByteArray());
    }
}
