/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.ManagerException;
import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;


public class TestTextScannerImplScannableScanForMatch {
	
	private ITextScanner scanner;
	private ITextScannable scannableString;
	private ITextScannable scannableStream;
	private ITextScannable scannableEmpty;

	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
		scannableString = new DummyScannableString();
		scannableStream = new DummyScannableInputStream();
		scannableEmpty = new DummyScannableEmpty();
	}

	// scanForMatch with scannable String and search Pattern
	@Test
	public void testScanForMachForScannableStringInputWithSearchPatternFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[c]");
		scanner = new TextScannerImpl();
		int count = 1;
		assertEquals("c", scanner.scanForMatch(scannableString, searchPattern, null, count));
	}
	
	@Test
	public void testScanForMachForScannableStringInputWithFailPatternFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[z]");
		Pattern failPattern = Pattern.compile("[c]");
		scanner = new TextScannerImpl();
		int count = 1;
		assertEquals("c", scanner.scanForMatch(scannableString, searchPattern, failPattern, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableStringInputWithSearchPatternNotFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("\\d");
		int count = 1;
		scanner.scanForMatch(scannableString, searchPattern, null, count);
	
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForScannableStringInputWithIncorrectOccurancesPattern() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 10;
		scanner.scanForMatch(scannableString, searchPattern, null, count);
	}
	
	@Test (expected = TextScanException.class)
	public void testScanScannableInvalidOccurancesPattern() throws IncorrectOccurrencesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[a]");
		int count = 0;
		scanner.scanForMatch(scannableString, searchPattern, null, count);
	}

	// scanForMatch() with scannable InputStream and search Pattern
	@Test 
	public void testScanForMachForScannableInputStreamInputWithSearchPatternFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[a]");
		int count = 1;
		assertEquals("a", scanner.scanForMatch(scannableStream, searchPattern, null, count));
	
	}
	
	@Test(expected = MissingTextException.class) 
	public void testScanForMachForScannableInputStreamInputWithSearchPatternNotFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("\\d");
		int count = 1;
		scanner.scanForMatch(scannableStream, searchPattern, null, count);
	
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForScannableInputStreamInputWithIncorrectOccurancesPattern() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[i]");
		int count = 10;
		scanner.scanForMatch(scannableStream, searchPattern, null, count);
	}

	// scanForMatch with scannable String and search String
	@Test
	public void testScanForMachForScannableStringInputWithSearchStringFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 1;
		assertEquals(searchString, scanner.scanForMatch(scannableString, searchString, null, count));
	
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableStringInputWithSearchStringNotFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "lemon";
		int count = 1;
		scanner.scanForMatch(scannableString, searchString, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForScannableInputStringWithIncorrectOccurancesString() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 10;
		scanner.scanForMatch(scannableString, searchString, null, count);
	}
	
	@Test (expected = TextScanException.class)
	public void testScanScannableInvalidOccurancesString() throws IncorrectOccurrencesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 0;
		scanner.scanForMatch(scannableString, searchString, null, count);
	}

	// scanForMatch with scannable InputStream and search String
	@Test
	public void testScanForMachForScannableInputStreamInputWithSearchStringFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 1;
		assertEquals("dummy", scanner.scanForMatch(scannableStream, searchString, null, count));
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForMachForScannableInputStreamInputWithSearchStringNotFound() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "lemon";
		int count = 1;
		scanner.scanForMatch(scannableStream, searchString, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForMachForScannableInputStreamInputWithIncorrectOccurancesString() throws IncorrectOccurrencesException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 5;
		scanner.scanForMatch(scannableStream, searchString, null, count);
	}
	
	// scanForMatch with invalid scannable
	@Test (expected = TextScanManagerException.class)
	public void testScanForMatchScannableEmptyString() throws IncorrectOccurrencesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		String searchString = "dummy";
		int count = 1;
		scanner.scanForMatch(scannableEmpty, searchString, null, count);
	}
	
	@Test (expected = TextScanManagerException.class)
	public void testScanForMatchScannableEmptyPattern() throws IncorrectOccurrencesException, FailTextFoundException, MissingTextException, ManagerException, IOException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		int count = 1;
		scanner.scanForMatch(scannableEmpty, searchPattern, null, count);
	}

}
