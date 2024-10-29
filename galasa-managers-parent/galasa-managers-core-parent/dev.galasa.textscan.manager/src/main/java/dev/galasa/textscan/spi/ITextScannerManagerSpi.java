/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.spi;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.TextScanManagerException;

public interface ITextScannerManagerSpi {
	
	public ITextScanner getTextScanner() throws TextScanManagerException;

	public ILogScanner getLogScanner() throws TextScanManagerException;
}
