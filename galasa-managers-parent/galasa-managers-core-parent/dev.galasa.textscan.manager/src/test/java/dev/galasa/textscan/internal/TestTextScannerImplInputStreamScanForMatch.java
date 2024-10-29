/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;


public class TestTextScannerImplInputStreamScanForMatch {

	private static final String TEXT_STRING = "This junit test tests scanForMatch methods in TextScanner implementation that uses input streams";
	InputStream textInputStream = new ByteArrayInputStream(TEXT_STRING.getBytes());
	ITextScanner scanner;
	
	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
	}

	
	// scanForMatch() with InputStream and search Pattern	
	@Test
	public void testScanForMachForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("a");
		int count = 1;
		assertEquals("a", scanner.scanForMatch(textInputStream, searchPattern, null, count));
	}
	
	@Test
	public void testScanForMachForInputStreamInputWithFailPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("z");
		Pattern failPattern = Pattern.compile("a");
		int count = 1;
		assertEquals("a", scanner.scanForMatch(textInputStream, searchPattern, failPattern, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("\\d");
		int count = 1;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 10;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 0;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	// scanForMatch() InputStream and search String 	
	@Test
	public void testScanForMachForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "test";
		int count = 1;
		assertEquals("test", scanner.scanForMatch(textInputStream, searchString, null, count));
	}
 	
	@Test
	public void testScanForMachForInputStreamInputWithFailStringFound() throws TextScanManagerException {
		String searchString = "missing";
		String failString = "test";
		int count = 1;
		assertEquals(failString, scanner.scanForMatch(textInputStream, searchString, failString, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "dragon";
		int count = 1;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		String searchString = "test";
		int count = 5;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanForMachInputStreamWithInvalidOccurancesString() throws TextScanManagerException {
		String searchString = "test";
		int count = 0;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
}
