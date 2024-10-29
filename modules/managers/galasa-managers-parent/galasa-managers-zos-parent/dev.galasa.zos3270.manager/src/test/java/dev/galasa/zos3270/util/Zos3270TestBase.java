/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.util;

import java.nio.charset.Charset;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.internal.LogScannerImpl;
import dev.galasa.textscan.internal.TextScannerImpl;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;

public class Zos3270TestBase {

	protected static Charset ebcdic = Charset.forName("Cp037");

	protected Terminal CreateTestTerminal() throws TerminalInterruptedException {
		TerminalSize terminalSize = new TerminalSize(10, 2);
		TerminalSize alternateTerminalSize = new TerminalSize(0, 0);
		ITextScanner textScanner = new TextScannerImpl();
		ILogScanner logScanner = new LogScannerImpl();

		ITextScannerManagerSpi mockTextScannerManager = new ITextScannerManagerSpi() {

			@Override
			public ITextScanner getTextScanner() throws TextScanManagerException {
				return textScanner;
			}

			@Override
			public ILogScanner getLogScanner() throws TextScanManagerException {
				return logScanner;
			}
		};
		return new Terminal("test", "", 0, false, terminalSize, alternateTerminalSize, mockTextScannerManager, ebcdic);
	}

    protected Screen CreateTestScreen() throws TerminalInterruptedException {
        return CreateTestScreen(80, 24, null);
    }

    protected static Screen CreateTestScreen(int columns, int rows, Network network) throws TerminalInterruptedException {
		TerminalSize terminalSize = new TerminalSize(columns, rows);
		TerminalSize alternateTerminalSize = new TerminalSize(0, 0);

        return new Screen(terminalSize, alternateTerminalSize, network, ebcdic);
    }
}
