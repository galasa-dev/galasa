/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.Test;

import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.mocks.MockNetwork;

public class TerminalTest {

    @Test
    public void testCanCreateTerminal() throws Exception {

        String terminalId = "abcdef";
        String host = "myHost";
        int port = 1234;
        boolean isSSL = false;
        boolean isVerifyServer = false;
        TerminalSize primarySize = new TerminalSize(80,24);
        TerminalSize alternateSize = new TerminalSize(80,24);
        ITextScannerManagerSpi textScan = null ;
        Charset codePage = Charset.availableCharsets().get("1024");

        ByteArrayOutputStream networkOut = new ByteArrayOutputStream();

        MockNetwork network = new MockNetwork(networkOut) {
            public boolean connectClient() throws NetworkException {
                return true;
            }
        };

        new Terminal(terminalId, host, port, isSSL, isVerifyServer, primarySize, alternateSize,
        textScan, codePage, network);

    }
    
}
