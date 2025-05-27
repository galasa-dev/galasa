/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.spi.Screen;

public class OutboundTest {

    @Test
    public void testReadSscpLuOutputBufferContainsCorrectData() throws Exception {

        // Given...
        Charset codePage = Charset.forName("1047");
        String mockScreenText = "\n"+
            "*** WELCOME TO SIMBANK TERMINAL ID = ABC123 \n"+
            "********* This is a welcome message. Hello world!\n"+
            " ===> ";

        String ebcdicScreen = Hex.encodeHexString(mockScreenText.getBytes(codePage));
        String sscpLuDataHeader = "0700000000";
        String iacEorTrailer = Hex.encodeHexString(new byte[]{ NetworkThread.IAC, NetworkThread.EOR });

        String inboundDataStream = sscpLuDataHeader + ebcdicScreen + iacEorTrailer;
        byte[] inboundAsBytes = Hex.decodeHex(inboundDataStream);

        Network network = new Network("here", 1, "a");

        TerminalSize terminalSize = new TerminalSize(80, 24);
        Screen screen = new Screen(terminalSize, new TerminalSize(0, 0), network, codePage);

        NetworkThread networkThread = new NetworkThread(null, screen, null, null);

        InputStream inputStream = new ByteArrayInputStream(inboundAsBytes);
        networkThread.processMessage(inputStream);

        String inputText = "Hello there!";
        
        // When...
        screen.type(inputText);
        byte[] outputBytes = screen.aid(AttentionIdentification.ENTER);

        // Then...
        BufferAddress cursorAddress = new BufferAddress(screen.getCursor());
        byte[] cursorBytes = cursorAddress.getCharRepresentation();
        assertThat(cursorBytes).hasSize(2);
        
        // Outbound messages contain a read header consisting of 3 bytes:
        // -----------------------------------------------
        //   Attention Identifier (AID) | Cursor Address
        //            1 byte                  2 bytes
        // -----------------------------------------------
        assertThat(outputBytes).isNotEmpty();
        assertThat(outputBytes[0]).isEqualTo(AttentionIdentification.ENTER.getKeyValue());
        assertThat(outputBytes[1]).isEqualTo(cursorBytes[0]);
        assertThat(outputBytes[2]).isEqualTo(cursorBytes[1]);

        // After the read header, the actual input data to be sent is included
        byte[] outputDataBytes = Arrays.copyOfRange(outputBytes, 3, outputBytes.length);
        String outputDataString = new String(outputDataBytes, codePage);
        assertThat(outputDataString).isEqualTo(inputText);
    }

    @Test
    public void testReadSscpLuOutputBufferAtEndOfScreenStaysInScreenBounds() throws Exception {

        // Given...
        Charset codePage = Charset.forName("1047");
        String mockScreenText = "===> ";
        String inputText = "bob";

        String ebcdicScreen = Hex.encodeHexString(mockScreenText.getBytes(codePage));
        String sscpLuDataHeader = "0700000000";
        String iacEorTrailer = Hex.encodeHexString(new byte[]{ NetworkThread.IAC, NetworkThread.EOR });

        String inboundDataStream = sscpLuDataHeader + ebcdicScreen + iacEorTrailer;
        byte[] inboundAsBytes = Hex.decodeHex(inboundDataStream);

        Network network = new Network("here", 1, "a");

        TerminalSize terminalSize = new TerminalSize(mockScreenText.length() + inputText.length(), 1);
        Screen screen = new Screen(terminalSize, new TerminalSize(0, 0), network, codePage);

        NetworkThread networkThread = new NetworkThread(null, screen, null, null);

        InputStream inputStream = new ByteArrayInputStream(inboundAsBytes);
        networkThread.processMessage(inputStream);

        
        // When...
        screen.type(inputText);
        byte[] outputBytes = screen.aid(AttentionIdentification.ENTER);

        // Then...
        BufferAddress cursorAddress = new BufferAddress(screen.getCursor());
        byte[] cursorBytes = cursorAddress.getCharRepresentation();
        assertThat(cursorBytes).hasSize(2);
        
        // Outbound messages contain a read header consisting of 3 bytes:
        // -----------------------------------------------
        //   Attention Identifier (AID) | Cursor Address
        //            1 byte                  2 bytes
        // -----------------------------------------------
        assertThat(outputBytes).isNotEmpty();
        assertThat(outputBytes[0]).isEqualTo(AttentionIdentification.ENTER.getKeyValue());
        assertThat(outputBytes[1]).isEqualTo(cursorBytes[0]);
        assertThat(outputBytes[2]).isEqualTo(cursorBytes[1]);

        // After the read header, the actual input data to be sent is included
        byte[] outputDataBytes = Arrays.copyOfRange(outputBytes, 3, outputBytes.length);
        String outputDataString = new String(outputDataBytes, codePage);
        assertThat(outputDataString).isEqualTo(inputText);
    }
}
