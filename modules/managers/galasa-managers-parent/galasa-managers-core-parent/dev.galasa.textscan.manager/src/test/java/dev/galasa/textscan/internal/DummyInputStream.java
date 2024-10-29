/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import java.io.IOException;
import java.io.InputStream;

class DummyInputStream extends InputStream {
	@Override
	public int read() throws IOException {
		throw new IOException("EXCEPTION");
	}
}