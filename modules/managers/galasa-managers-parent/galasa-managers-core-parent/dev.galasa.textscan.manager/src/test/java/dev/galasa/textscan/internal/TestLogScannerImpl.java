/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({IOUtils.class})
public class TestLogScannerImpl {
//	private static final String SCANNABLE_NAME = "SCANNABLE";
//
//	private static final String SCANNABLE_MUST_NOT_BE_NULL = "Scannable must not be null";
//	
//	private static final String QUOTE = "'";
//
//	private static final String TEXT_STRING = "zero one two three four five six seven eight nine";
//	
//	private InputStream textInputStream;
//
//	private static final String SEARCH_STRING = "four";
//
//	private static final String FAIL_STRING = "five";
//
//	private static final String ABSENT_STRING = "absent";
//	
//	private static final Pattern SEARCH_PATTERN = Pattern.compile(SEARCH_STRING);
//	
//	private static final Pattern FAIL_PATTERN = Pattern.compile(FAIL_STRING);
//	
//	private static final Pattern ABSENT_PATTERN = Pattern.compile(ABSENT_STRING);
//
//	private LogScannerImpl logScanner;
//
//	private LogScannerImpl logScannerSpy;
//	
//	@Mock
//	private ITextScannable textScannableMock;
//	
//	@Before
//	public void before() throws TextScanException {
//		logScanner = new LogScannerImpl();
//		logScannerSpy = spy(logScanner);
//		doNothing().when(logScannerSpy).checkScannableNoNull();
//		doNothing().when(logScannerSpy).checkIsCheckpointed();
//		textInputStream = new ByteArrayInputStream(TEXT_STRING.getBytes());
//	}
//	
//	@Test
//	public void testSetScannable() throws TextScanException {
//		assertSame(logScannerSpy, logScannerSpy.setScannable(textScannableMock));
//	}
//	
//	@Test
//	public void testUpdateScannable() throws TextScanException {
//		assertSame(logScannerSpy.setScannable(textScannableMock), logScannerSpy.updateScannable());
//	}
//	
//	@Test
//	public void testUpdateScannableException() throws TextScanException {
//		TextScanException expectedException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScanner.updateScannable();
//        });
//        assertEquals("exception should contain expected cause", SCANNABLE_MUST_NOT_BE_NULL, expectedException.getMessage());
//	}
//	
//	@Test
//	public void testReset() throws TextScanException {
//		((LogScannerImpl) logScannerSpy.setScannable(textScannableMock)).checkScannableNoNull();
//		logScanner.reset();
//		TextScanException expectedException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScanner.checkScannableNoNull();
//        });
//        assertEquals("exception should contain expected cause", SCANNABLE_MUST_NOT_BE_NULL, expectedException.getMessage());
//	}
//	
//	@Test
//	public void testCheckpoint() throws TextScanException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		assertEquals((long) TEXT_STRING.length(), logScannerSpy.getCheckpoint());
//
//		reset(textScannableMock);
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		assertEquals((long) TEXT_STRING.length(), logScannerSpy.getCheckpoint());
//		
//		PowerMockito.mockStatic(IOUtils.class);
//		when(IOUtils.toByteArray((InputStream) any())).thenThrow(new IOException());
//		TextScanException expectedException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.checkpoint();
//        });
//        assertEquals("exception should contain expected cause", "Unable to checkpoint scannable '" + SCANNABLE_NAME + QUOTE, expectedException.getMessage());
//		
//        reset(textScannableMock);
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		PowerMockito.mockStatic(IOUtils.class);
//		when(IOUtils.toByteArray((InputStream) any())).thenThrow(new IOException());
//		expectedException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.checkpoint();
//        });
//        assertEquals("exception should contain expected cause", "Unable to checkpoint scannable '" + SCANNABLE_NAME + QUOTE + ", unknown scannable type", expectedException.getMessage());
//	}
//	
//	@Test
//	public void testResetCheckpoint() throws TextScanException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		assertEquals((long) TEXT_STRING.length(), logScannerSpy.getCheckpoint());
//
//		logScannerSpy.resetCheckpoint();
//		assertEquals(-1, logScannerSpy.getCheckpoint());		
//	}
//	
//	// scanSinceCheckpoint() ----------------------------------------------------------------------------------------
//	
//	@Test
//	public void testScanSinceCheckpointSearchPatternTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + SEARCH_PATTERN + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // FailTextFoundException
//        when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING + "\n" + TEXT_STRING);
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, FAIL_PATTERN, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail Pattern '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Expecting 2 instances of Pattern '" + SEARCH_PATTERN + "' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // text found 
//		assertTrue(logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSinceCheckpointSearchPatternTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + SEARCH_PATTERN + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // FailTextFoundException
//		textInputStream.reset();
//        textInputStream = new ByteArrayInputStream((TEXT_STRING + "\n" + TEXT_STRING).getBytes());
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, FAIL_PATTERN, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail Pattern '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Expecting 2 instances of Pattern '" + SEARCH_PATTERN + "' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertTrue(logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSinceCheckpointSearchPatternTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	@Test
//	public void testScanSinceCheckpointSearchStringTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + SEARCH_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + SEARCH_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // FailTextFoundException
//        when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING + "\n" + TEXT_STRING);
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, FAIL_STRING, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail String '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//        assertEquals("Fail Pattern '\\Q" + FAIL_STRING + "\\E' found", expectedFailTextFoundException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Expecting 2 instances of Pattern '\\Q" + SEARCH_STRING + "\\E' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // text found 
//		assertTrue(logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSinceCheckpointSearchStringTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + SEARCH_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + SEARCH_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // FailTextFoundException
//		textInputStream.reset();
//        textInputStream = new ByteArrayInputStream((TEXT_STRING + "\n" + TEXT_STRING).getBytes());
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, FAIL_STRING, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail String '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//        assertEquals("Fail Pattern '\\Q" + FAIL_STRING + "\\E' found", expectedFailTextFoundException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Expecting 2 instances of Pattern '\\Q" + SEARCH_STRING + "\\E' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertTrue(logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSinceCheckpointSearchStringTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	// scanForMatchSinceCheckpoint() ----------------------------------------------------------------------------------------
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchPatternTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + SEARCH_PATTERN + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING + "\n" + TEXT_STRING);
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '" + SEARCH_PATTERN + "'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // text found 
//        assertEquals(SEARCH_STRING, logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchPatternTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + SEARCH_PATTERN + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        textInputStream = new ByteArrayInputStream((TEXT_STRING + "\n" + TEXT_STRING).getBytes());
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '" + SEARCH_PATTERN + "'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchPatternTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchStringTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + SEARCH_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + SEARCH_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING + "\n" + TEXT_STRING);
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '\\Q" + SEARCH_STRING + "\\E'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // text found 
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchStringTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + SEARCH_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + SEARCH_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        textInputStream = new ByteArrayInputStream((TEXT_STRING + "\n" + TEXT_STRING).getBytes());
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '\\Q" + SEARCH_STRING + "\\E'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSinceCheckpointSearchStringTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanForMatchSinceCheckpoint(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	// scan() ----------------------------------------------------------------------------------------
//	
//	@Test
//	public void testScanSearchPatternTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scan(ABSENT_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // FailTextFoundException
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scan(SEARCH_PATTERN, FAIL_PATTERN, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail Pattern '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scan(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Expecting 2 instances of Pattern '" + SEARCH_PATTERN + "' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // text found 
//		assertTrue(logScannerSpy.scan(SEARCH_PATTERN, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSearchPatternTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scan(ABSENT_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // FailTextFoundException
//		textInputStream.reset();
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scan(SEARCH_PATTERN, FAIL_PATTERN, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail Pattern '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scan(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Expecting 2 instances of Pattern '" + SEARCH_PATTERN + "' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertTrue(logScannerSpy.scan(SEARCH_PATTERN, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSearchPatternTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scan(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	@Test
//	public void testScanSearchStringTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scan(ABSENT_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + ABSENT_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // FailTextFoundException
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scan(SEARCH_STRING, FAIL_STRING, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail String '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//        assertEquals("Fail Pattern '\\Q" + FAIL_STRING + "\\E' found", expectedFailTextFoundException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scan(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Expecting 2 instances of Pattern '\\Q" + SEARCH_STRING + "\\E' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // text found 
//		assertTrue(logScannerSpy.scan(SEARCH_STRING, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSearchStringTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scan(ABSENT_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + ABSENT_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // FailTextFoundException
//		textInputStream.reset();
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		FailTextFoundException expectedFailTextFoundException = assertThrows("expected exception should be thrown", FailTextFoundException.class, ()->{
//			logScannerSpy.scan(SEARCH_STRING, FAIL_STRING, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedFailTextFoundException.getMessage());
//        assertEquals("Fail String '" + FAIL_STRING + "' found", expectedFailTextFoundException.getCause().getMessage());
//        assertEquals("Fail Pattern '\\Q" + FAIL_STRING + "\\E' found", expectedFailTextFoundException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scan(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Expecting 2 instances of Pattern '\\Q" + SEARCH_STRING + "\\E' but found 1 occurrence(s)", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertTrue(logScannerSpy.scan(SEARCH_STRING, null, 1) instanceof LogScannerImpl);
//	}
//	
//	@Test
//	public void testScanSearchStringTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scan(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	// scanForMatch() ----------------------------------------------------------------------------------------
//	
//	@Test
//	public void testScanForMatchSearchPatternTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatch(ABSENT_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '" + SEARCH_PATTERN + "'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // text found 
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatch(SEARCH_PATTERN, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSearchPatternTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatch(ABSENT_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search Pattern '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_PATTERN, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '" + SEARCH_PATTERN + "'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatch(SEARCH_PATTERN, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSearchPatternTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_PATTERN, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	@Test
//	public void testScanForMatchSearchStringTextString() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScannerSpy.setScannable(textScannableMock);
//		
//		// MissingTextException
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatch(ABSENT_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + ABSENT_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '\\Q" + SEARCH_STRING + "\\E'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // text found 
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatch(SEARCH_STRING, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSearchStringTextInputStream() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//		logScannerSpy.checkpoint();
//		
//		// MissingTextException
//		textInputStream.reset();
//		MissingTextException expectedMissingTextException = assertThrows("expected exception should be thrown", MissingTextException.class, ()->{
//			logScannerSpy.scanForMatch(ABSENT_STRING, null, 1);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedMissingTextException.getMessage());
//        assertEquals("Search String '" + ABSENT_STRING + "' not found", expectedMissingTextException.getCause().getMessage());
//        assertEquals("Search Pattern '\\Q" + ABSENT_STRING + "\\E' not found", expectedMissingTextException.getCause().getCause().getMessage());
//
//        // IncorrectOccurrencesException
//		textInputStream.reset();
//        IncorrectOccurrencesException expectedIncorrectOccurrencesException = assertThrows("expected exception should be thrown", IncorrectOccurrencesException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_STRING, null, 2);
//        });
//        assertEquals("Problem scanning '" + SCANNABLE_NAME + QUOTE, expectedIncorrectOccurrencesException.getMessage());
//        assertEquals("Wrong number of occurrences of String '" + SEARCH_STRING + "' found", expectedIncorrectOccurrencesException.getCause().getMessage());
//        assertEquals("Unable to find occurrence 2 of Pattern '\\Q" + SEARCH_STRING + "\\E'. Occurrences found: 1", expectedIncorrectOccurrencesException.getCause().getCause().getMessage());
//        
//        // Text found 
//		textInputStream.reset();
//		assertEquals(SEARCH_STRING, logScannerSpy.scanForMatch(SEARCH_STRING, null, 1));
//	}
//	
//	@Test
//	public void testScanForMatchSearchStringTextUnknown() throws TextScanException, FailTextFoundException, MissingTextException, IncorrectOccurrencesException, IOException {
//		logScannerSpy.setScannable(textScannableMock);
//		doReturn(logScannerSpy).when(logScannerSpy).checkpoint();
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.scanForMatch(SEARCH_STRING, null, 1);
//        });
//        assertEquals("Problem scanning 'null'", expectedTextScanException.getMessage());
//        assertEquals("Unknown scannable type", expectedTextScanException.getCause().getMessage());
//	}
//	
//	@Test
//	public void testCheckIsCheckpointed() throws TextScanException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableString()).thenReturn(true);
//		when(textScannableMock.getScannableString()).thenReturn(TEXT_STRING);
//		logScanner.setScannable(textScannableMock);
//
//		logScanner.checkpoint();
//		logScanner.checkIsCheckpointed();
//		
//		logScanner.resetCheckpoint();		
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScanner.checkIsCheckpointed();
//        });
//        assertEquals("Scannable has not been checkpointed", expectedTextScanException.getMessage());
//	}
//	
//	@Test
//	public void testSkipToCheckpoint() throws TextScanException, IOException {
//		when(textScannableMock.getScannableName()).thenReturn(SCANNABLE_NAME);
//		when(textScannableMock.isScannableInputStream()).thenReturn(true);
//		when(textScannableMock.getScannableInputStream()).thenReturn(textInputStream);
//		logScannerSpy.setScannable(textScannableMock);
//
//		logScannerSpy.setCheckpoint(-1);		
//		logScannerSpy.skipToCheckpoint();
//
//		textInputStream.reset();
//		logScannerSpy.setCheckpoint(11);		
//		logScannerSpy.skipToCheckpoint();
//
//		textInputStream.reset();
//		logScannerSpy.setCheckpoint(99);	
//		TextScanException expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.skipToCheckpoint();			
//        });
//        assertEquals("Unable to skip to checkpoint of scannable '" + SCANNABLE_NAME + "'", expectedTextScanException.getMessage());
//        assertTrue(expectedTextScanException.getCause() instanceof IOException);
//        assertEquals("Failed to skip 99 bytes. Actual bytes skipped 49", expectedTextScanException.getCause().getMessage());
//
//		when(textScannableMock.getScannableInputStream()).thenReturn(new DummyInputStream());
//		logScannerSpy.setScannable(textScannableMock);
//		expectedTextScanException = assertThrows("expected exception should be thrown", TextScanException.class, ()->{
//			logScannerSpy.skipToCheckpoint();			
//        });
//        assertEquals("Unable to skip to checkpoint of scannable '" + SCANNABLE_NAME + "'", expectedTextScanException.getMessage());
//        assertTrue(expectedTextScanException.getCause() instanceof IOException);
//        assertEquals("EXCEPTION", expectedTextScanException.getCause().getMessage());
//	}
}
