/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.spi.Screen;

public class InboundTest {

    @Test
    public void testSettingCodePageRendersSquareBracketsOK() throws Exception {

        // Given...
        // A screen that looks like this:
        // 0006    SQLTIMES
        // 0007    SQLTIMES [0]              6622
        // 0008    SQLTIMES [1]              55339
        // 0009    SQLTIMES [2]              4589
        String inboundDataStream = "f1401102d0290342f541f2c000f0f0f0f61102d5290342f541f2c020404040e2d8d3e3c9d4c5e2404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040110320290342f541f2c000f0f0f0f7110325290342f541f2c020404040e2d8d3e3c9d4c5e240adf0bd40404040404040404040404040110342290342f541f2c000f6f6f2f2110347290342f541f2c02040404040404040404040404040404040404040404040404040404040404040404040404040404040110370290342f541f2c000f0f0f0f8110375290342f541f2c020404040e2d8d3e3c9d4c5e240adf1bd40404040404040404040404040110392290342f541f2c000f5f5f3f3f9110398290342f541f2c0204040404040404040404040404040404040404040404040404040404040404040404040404040401103c0290342f541f2c000f0f0f0f91103c5290342f541f2c020404040e2d8d3e3c9d4c5e240adf2bd404040404040404040404040401103e2290342f541f2c000f4f5f8f91103e7290342f541f2c02040404040404040404040404040404040404040404040404040404040404040404040404040404040110410290342f541f2c000";
        byte[] inboundAsBytes = Hex.decodeHex(inboundDataStream);

        Network network = new Network("here", 1, "a");

        // Set the screen's code page to EBCDIC 1047
        Charset codePage = Charset.forName("1047");
        TerminalSize terminalSize = new TerminalSize(80, 24);
        Screen screen = new Screen(terminalSize, new TerminalSize(0, 0), network, codePage);

        NetworkThread networkThread = new NetworkThread(null, screen, null, null);
        ByteBuffer buffer = ByteBuffer.wrap(inboundAsBytes);
        Inbound3270Message inboundMessage = networkThread.process3270Data(buffer);

        // When...
        screen.processInboundMessage(inboundMessage);
        System.out.println(screen.printScreen());

        // Then...
        assertThat(screen.printScreen()).contains("SQLTIMES", "[0]", "[1]", "[2]");
    }

    @Test
    public void testSscpLuDataStreamRendersOK() throws Exception {

        // Given...
        Charset codePage = Charset.forName("1047");
        String mockScreenText = "\n"+
            "*** WELCOME TO SIMBANK TERMINAL ID = ABC123 \n"+
            "********* This is a welcome message. Hello world!\n"+
            " ===> ";

        String ebcdicScreen = Hex.encodeHexString(mockScreenText.getBytes(codePage));
        String sscpLuDataHeader = "0700000000";
        String iacEor = Hex.encodeHexString(new byte[]{ NetworkThread.IAC, NetworkThread.EOR });

        String inboundDataStream = sscpLuDataHeader + ebcdicScreen + iacEor;
        byte[] inboundAsBytes = Hex.decodeHex(inboundDataStream);

        Network network = new Network("here", 1, "a");

        TerminalSize terminalSize = new TerminalSize(80, 24);
        Screen screen = new Screen(terminalSize, new TerminalSize(0, 0), network, codePage);

        NetworkThread networkThread = new NetworkThread(null, screen, null, null);

        InputStream inputStream = new ByteArrayInputStream(inboundAsBytes);
        
        // When...
        networkThread.processMessage(inputStream);
        String screenStr = screen.printScreenTextWithCursor();
        System.out.println(screenStr);

        int cursorPosition = screen.getCursor();
        System.out.println("Cursor is at position: " + cursorPosition);

        // Then...
        assertThat(screenStr).contains("WELCOME TO SIMBANK", "\n");
        assertThat(cursorPosition).isEqualTo(246);
    }

    @Test
    public void testSscpLuDataStreamEndingWithNewlinePutsCursorInCorrectPosition() throws Exception {

        // Given...
        Charset codePage = Charset.forName("1047");
        String mockScreenText = "\n"+
            "*** WELCOME TO SIMBANK TERMINAL ID = ABC123 \n"+
            "********* This is a welcome message. Hello world!\n"+
            " ===>\n";

        String ebcdicScreen = Hex.encodeHexString(mockScreenText.getBytes(codePage));
        String sscpLuDataHeader = "0700000000";
        String iacEor = Hex.encodeHexString(new byte[]{ NetworkThread.IAC, NetworkThread.EOR });

        String inboundDataStream = sscpLuDataHeader + ebcdicScreen + iacEor;
        byte[] inboundAsBytes = Hex.decodeHex(inboundDataStream);

        Network network = new Network("here", 1, "a");

        TerminalSize terminalSize = new TerminalSize(80, 24);
        Screen screen = new Screen(terminalSize, new TerminalSize(0, 0), network, codePage);

        NetworkThread networkThread = new NetworkThread(null, screen, null, null);

        InputStream inputStream = new ByteArrayInputStream(inboundAsBytes);
        
        // When...
        networkThread.processMessage(inputStream);
        String screenStr = screen.printScreenTextWithCursor();
        System.out.println(screenStr);

        int cursorPosition = screen.getCursor();
        System.out.println("Cursor is at position: " + cursorPosition);

        // Then...
        assertThat(screenStr).contains("WELCOME TO SIMBANK", "\n");
        assertThat(cursorPosition).isEqualTo(320);
    }
}
