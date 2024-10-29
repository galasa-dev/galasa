/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class TestTextScannerImplInputStreamLargeFile {

	private static final File FILE = new File("src/test/resources/bigfile.txt");
	private InputStream textInputStream;
	private ITextScanner scanner;

	@Before
	public void beforeClass() throws FileNotFoundException {
		scanner = new TextScannerImpl();
		textInputStream = new FileInputStream(FILE);
	}

	// scan() with InputStream text and search Pattern
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("a huge dragon landed on the house and started breathing fire");
		Pattern failPattern = Pattern.compile("Line 13\nLine 14");
		int count = 1;
		scanner.scan(textInputStream, searchPattern, failPattern, count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("100");
		Pattern failPattern = null;;
		int count = 3;
		assertTrue(scanner.scan(textInputStream, searchPattern, failPattern, count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("the dragon fired 5 times and then decided to take a nap");
		Pattern failPattern = null;
		int count = 1;
		scanner.scan(textInputStream, searchPattern, failPattern, count);
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("100");
		Pattern failPattern = null;
		int count = 10;
		scanner.scan(textInputStream, searchPattern, failPattern, count);
	}

	// scan() with InputStream text and search String
	@Test(expected = FailTextFoundException.class)
	public void testScanForInputStreamInputWithFailString() throws TextScanManagerException {
		String searchString = "after a nap dragon became very very hungry";
		String failString = "100";
		int count = 1;
		scanner.scan(textInputStream, searchString, failString, count);
	}
	@Test
	public void testScanForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "Line 16 Eye";
		String failString = null;;
		int count = 3;
		assertTrue(scanner.scan(textInputStream, searchString, failString, count) instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class) 
	public void testScanForInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "He decided that he wants some nice pork, so he flew towards pig village";
		String failString = null;
		int count = 1;
		scanner.scan(textInputStream, searchString, failString, count);
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		String searchString = "Line 38 Bee";
		String failString = null;
		int count = 10;
		scanner.scan(textInputStream, searchString, failString, count);
	}

	// scanForMatch() with InputStream text and search Pattern
	@Test
	public void testScanForMatchForInputStreamInputWithSearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("100");
		int count = 3;
		assertEquals("100", scanner.scanForMatch(textInputStream, searchPattern, null, count));
	}
	
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("He carefully landed into the willage and silently lifted the roof");
		int count = 1;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("77");
		int count = 10;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	@Test(expected = TextScanException.class)
	public void testScanInputStreamIOException() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		int count = 1;
		scanner.scanForMatch(new DummyInputStream(), searchPattern, null, count);
	}


	// scanForMatch() with InputStream text and search String
	@Test
	public void testScanForMatchForInputStreamInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "Line 76 M";
		int count = 3;
		assertEquals(searchString, scanner.scanForMatch(textInputStream, searchString, null, count));
	}
	
	@Test(expected = MissingTextException.class) 
	public void testScanForForMatchInputStreamInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "In the house he found five sleeping pigs in blankets and he ate them all.";
		int count = 1;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputWithIncorrectOccurancesString() throws TextScanManagerException {
		String searchString = "10";
		int count = 10;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	
	// scanForMatch() with InputStream text and search String - incorrect Occurrences
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputCheckDubleAccountingString() throws TextScanManagerException {
		String searchString = "Q";
		int count = 2;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputCheckDubleAccountingString2() throws TextScanManagerException {
		String searchString = "Line 7\nLine 8\nLine 7";
		int count = 3;
		scanner.scanForMatch(textInputStream, searchString, null, count);
	}
	
	// scanForMatch() with InputStream text and search Pattern - incorrect Occurrences
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputCheckDoubleAccountingPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("Q");
		int count = 2;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForForMatchInputStreamInputCheckDoubleAccountingPattern2() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("Line 7\nLine 8\nLine 7");
		int count = 3;
		scanner.scanForMatch(textInputStream, searchPattern, null, count);
	}
}
