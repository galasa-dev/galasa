/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;

public class TestTextScannerImplStringScan {
	
	private static final String TEST_STRING = "This is a textscanner test";
	private ITextScanner scanner;

	@Before
	public void beforeClass() {
		scanner = new TextScannerImpl();
	}
	
	// scan() with String text and search Pattern
	public void testScanForStringInputWithFailPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = Pattern.compile("[abc]");
		int count = 1;
		scanner.scan(TEST_STRING, searchPattern, failPattern, count);
	}
	
	@Test
	public void testScanForStringInputWithSearchPatternFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = null;
		int count = 1;
		assertTrue(scanner.scan(TEST_STRING, searchPattern, failPattern, count) instanceof ITextScanner);
	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchPatternNotFound() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[5]");
		Pattern failPattern = Pattern.compile("[123]");
		scanner = new TextScannerImpl();
		int count = 1;
		scanner.scan(TEST_STRING, searchPattern, failPattern, count);
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForStringInputWithIncorrectOccurancesPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[abc]");
		Pattern failPattern = null;
		int count = 5;
		scanner.scan(TEST_STRING, searchPattern, failPattern, count) ;
	}
	
	@Test (expected = TextScanException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumber() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[abc]");
		Pattern failPattern = null;
		int count = 0;
		scanner.scan(TEST_STRING, searchPattern, failPattern, count) ;
	}
	
	// scan() with String text and search String
	@Test(expected = FailTextFoundException.class)
	public void testScanForStringInputWithFailString() throws TextScanManagerException {
		String searchString = "test";
		String failString = "is";
		int count = 1;
		scanner.scan(TEST_STRING, searchString, failString, count) ;
	}
	
	@Test
	public void testScanForStringInputWithSearchStringFound() throws TextScanManagerException {
		String searchString = "test";
		String failString = null;
		int count = 1;
		assertTrue(scanner.scan(TEST_STRING, searchString, failString, count) instanceof ITextScanner);

	}
	
	@Test(expected = MissingTextException.class)
	public void testScanForStringInputWithSearchStringNotFound() throws TextScanManagerException {
		String searchString = "Dragon";
		String failString = null;
		int count = 1;
		scanner.scan(TEST_STRING, searchString ,failString, count) ;
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForStringInputWithIncorrectOccurancesString() throws TextScanManagerException {
		String testString = "Five flying Dragons with five little dragons";
		String searchString = "dragons";
		String failString = null;
		int count = 3;
		scanner.scan(testString, searchString, failString, count) ;
	}
	
	@Test (expected = TextScanException.class)
	public void testScanForStringInputWithIncorrectOccurancesNumberString() throws TextScanManagerException {
		String searchString = "test";
		String failString = null;
		int count = 0;
		scanner.scan(TEST_STRING, searchString, failString, count) ;
	}
}
