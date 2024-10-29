/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.TextScanException;

public class DummyScannableInputStream implements ITextScannable {

	@Override
	public boolean isScannableInputStream() {
		return true;
	}

	@Override
	public boolean isScannableString() {
		return false;
	}

	@Override
	public String getScannableName() {
		return "InputStreamDummy";
	}

	@Override
	public ITextScannable updateScannable() throws TextScanException {
		return this;
	}

	@Override
	public InputStream getScannableInputStream() throws TextScanException {
		String string = "This class is a dummy object with InputStream for junit "
        		+ "tests for scanner implementations methods that uses scannable";
		InputStream stream = new ByteArrayInputStream(string.getBytes());
		return stream;
	}

	@Override
	public String getScannableString() throws TextScanException {
		return "DummyScannableInputStream";
	}

}
