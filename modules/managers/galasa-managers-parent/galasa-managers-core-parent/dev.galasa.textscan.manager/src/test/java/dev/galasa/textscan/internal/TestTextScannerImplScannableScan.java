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
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;

public class TestTextScannerImplScannableScan {

	
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
	
	// scan() with scannable String and search Pattern
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableStringInputWithFailPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = Pattern.compile("[abc]");
		int count = 1;
		scanner.scan(scannableString, searchPattern, failPattern, count);
	}
	@Test
	public void testScanForScannableStringInputWithSearchPatternFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = null;
		int count = 1;
		assertTrue(scanner.scan(scannableString, searchPattern, failPattern, count) instanceof ITextScanner);
	
	}
	@Test(expected = MissingTextException.class)
	public void testScanForScannableStringInputWithSearchPatternNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		Pattern searchPattern = Pattern.compile("5");
		Pattern failPattern = null;
		int count = 1;
		scanner.scan(scannableString,searchPattern,failPattern,count);
	
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[c]");
		Pattern failPattern = null;
		int count = 10;
		scanner.scan(scannableString, searchPattern, failPattern, count);
	
	}
	
	// scan() with scannable InputStream and search Pattern
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableInputStreamInputWithFailPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = Pattern.compile("[abc]");
		int count = 1;
		scanner.scan(scannableStream, searchPattern, failPattern, count);
	
	}
	
	@Test
	public void testScanForScannableInputStreamInputWithSearchPatternFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("c");
		Pattern failPattern = null;
		int count = 1;
		assertTrue(scanner.scan(scannableStream, searchPattern, failPattern, count)instanceof ITextScanner);
	
	}

	@Test(expected = MissingTextException.class)
	public void testScanForScannableInputStreamInputWithSearchPatternNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("5");
		Pattern failPattern = null;
		int count = 1;
		scanner.scan(scannableStream, searchPattern, failPattern, count);
	
	}
	
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForScannableInputStreamInputWithIncorrectOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("c");
		Pattern failPattern = null;
		int count = 10;
		scanner.scan(scannableStream, searchPattern, failPattern, count);
	
	}
	
	
	// scan() with scannable String and search String
	@Test(expected = FailTextFoundException.class)
	public void testScanScannableStringInputWithFailString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "dummy";
		String failString = "junit";
		int count = 1;
		scanner.scan(scannableString, searchString, failString, count);
	}
	@Test 
	public void testScanScannableStringInputWithSearchStringFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "class";
		String failString = null;
		int count = 1;
		assertTrue(scanner.scan(scannableString, searchString, failString, count) instanceof ITextScanner);
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForScannableStringInputWithIncorrectOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "class";
		String failString = null;
		int count = 5;
		scanner.scan(scannableString, searchString, failString, count);
	}
	@Test(expected = MissingTextException.class)
	public void testScanForScannableStringInputWithSearchStringNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "a dog";
		String failString = null;
		int count = 1;
		scanner.scan(scannableString, searchString, failString, count);
	
	}
	
	// scan() with scannable InputStream and search String
	@Test(expected = FailTextFoundException.class)
	public void testScanForScannableInputStreamInputWithFailString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "dummy";
		String failString = "junit";
		int count = 1;
		scanner.scan(scannableStream, searchString, failString, count);
	}
	@Test
	public void testScanForScannableInputStreamInputWithSearchStringFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		String searchString = "dummy";
		String failString = null;
		int count = 1;
		assertTrue(scanner.scan(scannableStream, searchString, failString, count)instanceof ITextScanner);
	}
	@Test(expected = MissingTextException.class)
	public void testScanScannableInputStreamInputWithSearchStringNotFound() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "lemon";
		String failString = "apple";
		int count = 1;
		scanner.scan(scannableStream, searchString, failString, count);
	
	}
	@Test(expected = IncorrectOccurrencesException.class)
	public void testScanForScannableInputStreamInputWithIncorrectOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		String searchString = "dummy";
		String failString = "apple";
		int count = 5;
		scanner.scan(scannableStream, searchString, failString, count);
	
	}
	
	// scan() with scannable String and Pattern - invalid count
	@Test (expected =  TextScanException.class)
	public void testScanScannableInvalidOccurancesString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException{
		String searchString = "dummy";
		String failString = null;
		int count = 0;
		scanner.scan(scannableString, searchString, failString, count);
	}
	@Test (expected = TextScanException.class)
	public void testScanScannableInvalidOccurancesPattern() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern = null;
		int count = 0;
		scanner.scan(scannableString, searchPattern, failPattern, count);
	}
	
	// scan() with scannable String and Pattern - invalid scannable
	@Test (expected = TextScanManagerException.class)
	public void testScanScannableEmptyString() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException,TextScanManagerException {
		String searchString = "dummy";
		String failString = "test";
		int count = 1;
		scanner.scan(scannableEmpty, searchString, failString, count);
	}
	@Test (expected = TextScanManagerException.class)
	public void testScanScannableEmptyPattern() throws TextScanManagerException {
		Pattern searchPattern = Pattern.compile("[^abc]");
		Pattern failPattern =  Pattern.compile("dragon");
		int count = 1;
		scanner.scan(scannableEmpty, searchPattern, failPattern, count);
	}
}
