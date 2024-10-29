/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;


public class TestTextScannerImplInputStreamScan {

	private static final String TEXT_STRING = "This junit test tests scan methods in TextScanner implementation that uses input streams";
	private InputStream textInputstream;
	private ITextScanner scanner;

	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
		textInputstream = new ByteArrayInputStream(TEXT_STRING.getBytes());
	}

	// scan() InputStream with search Pattern 
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithfailPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		Pattern failPattern = Pattern.compile("[a-zA-Z]");
		int count = 1;
		scanner.scan(textInputstream, searchPattern, failPattern, count);
	}
	
	@Test
	public void testScanForInputStreamInputWithsearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[a]");
		Pattern failPattern = null;
		int count = 1;
		assertTrue(scanner.scan(textInputstream, searchPattern, failPattern, count) instanceof ITextScanner);
	}
	
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithsearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("\\d");
		Pattern failPattern = null;
		int count = 1;
		scanner.scan(textInputstream, searchPattern, failPattern, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		Pattern failPattern = null;
		int count = 10;
		scanner.scan(textInputstream, searchPattern, failPattern, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanInputStreamWithInvalidOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		Pattern failPattern = null;
		int count = 0;
		scanner.scan(textInputstream, searchPattern, failPattern, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanInputStreamIOException() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		Pattern failPattern = null;
		int count = 1;
		scanner.scan(new DummyInputStream(), searchPattern, failPattern, count);
	}

	// scan() InputStream with search String 
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailString() throws TextScanManagerException {
		String searchString = "test";
		String failString = "scan";
		int count = 1;
		scanner.scan(textInputstream, searchString, failString, count);
	}
	
	@Test
	public void testScanForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "test";
		String failString = null;
		int count = 1;
		assertTrue(scanner.scan(searchString, searchString, failString, count)instanceof ITextScanner);
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "dragon";
		String failString = null;
		int count = 1;
		scanner.scan(textInputstream, searchString, failString, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesrString() throws TextScanManagerException {
		String searchString = "test";
		String failString = null;
		int count = 10;
		scanner.scan(searchString, searchString, failString, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanInputStreamwithInvalidOccurancesString() throws TextScanManagerException {
		String searchString = "test";
		String failString = null;
		int count = 0;
		scanner.scan(searchString, searchString, failString, count);
	}
}
