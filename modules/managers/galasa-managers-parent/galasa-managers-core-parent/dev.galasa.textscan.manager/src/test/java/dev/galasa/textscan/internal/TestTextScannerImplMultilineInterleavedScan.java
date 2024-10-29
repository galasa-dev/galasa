/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.textscan.TextScanManagerException;

public class TestTextScannerImplMultilineInterleavedScan {
    private static final String TEXT_STRING = "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One Two\n" +
            								 "One Three Two\n" +
            								 "One Three\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One Four\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n" +
            								 "One\n";
    private static final Pattern REGEX_OK = Pattern.compile("Two\nOne Three", Pattern.MULTILINE);
    private static final Pattern REGEX_FAIL = Pattern.compile("Four", Pattern.MULTILINE);
    
    @Test
    public void testRawRegexWorks() {
        System.out.println(TEXT_STRING);
        Matcher matcher = REGEX_OK.matcher(TEXT_STRING);
        int count = 0;
        while(matcher.find()) {
            System.out.println(TEXT_STRING.substring(matcher.start(), matcher.end()));
            count++;
        }
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    public void testStringFindsOnlyTwoOccurances() throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
        TextScannerImpl ts = new TextScannerImpl();
        ts.scan(TEXT_STRING, REGEX_OK, null, 2);
        try {
            ts.scan(TEXT_STRING, REGEX_OK, null, 3);
            fail("Should not have found 3 occurances");
        } catch(IncorrectOccurrencesException e) {}
    }
    
    @Test
    public void testStringFailAfterOccurances() throws IncorrectOccurrencesException, MissingTextException, TextScanException {
        TextScannerImpl ts = new TextScannerImpl();
        try {
            ts.scan(TEXT_STRING, REGEX_OK, REGEX_FAIL, 2);
            fail("Should have issued FailTextFoundException");
        } catch(FailTextFoundException e) {}
    }
    
    @Test
    public void testInputStreamFindsOnlyTwoOccurances() throws TextScanManagerException {
        ByteArrayInputStream bais = new ByteArrayInputStream(TEXT_STRING.getBytes(StandardCharsets.UTF_8));
        TextScannerImpl ts = new TextScannerImpl();
        ts.scan(bais, REGEX_OK, null, 2);
        try {
        	bais = new ByteArrayInputStream(TEXT_STRING.getBytes(StandardCharsets.UTF_8));
            ts.scan(bais, REGEX_OK, null, 3);
            fail("Should not have found 3 occurances");
        } catch(IncorrectOccurrencesException e) {}
    }
    
    @Test
    public void testInputStreamFailAfterOccurances() throws TextScanManagerException {
        ByteArrayInputStream bais = new ByteArrayInputStream(TEXT_STRING.getBytes(StandardCharsets.UTF_8));
        TextScannerImpl ts = new TextScannerImpl();
        try {
            ts.scan(bais, REGEX_OK, REGEX_FAIL, 2);
            fail("Should have issued FailTextFoundException");
        } catch(FailTextFoundException e) {}
    }
}