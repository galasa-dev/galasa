/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dev.galasa.textscan.TextScanException;

public class TestAbstractTextScannable {

	AbstractTextScannable abstractTextScannable = new AbstractTextScannable();
	
	@Test
	public void testMethods() throws TextScanException {
		assertFalse(abstractTextScannable.isScannableInputStream());
		assertTrue(abstractTextScannable.isScannableString());
		assertNull(abstractTextScannable.getScannableInputStream());
		assertEquals("unknown", abstractTextScannable.getScannableName());
		assertNull(abstractTextScannable.getScannableInputStream());
		assertEquals("", abstractTextScannable.getScannableString());
		assertTrue(abstractTextScannable.updateScannable() instanceof AbstractTextScannable);
	}

}
