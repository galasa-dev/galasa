/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

import java.io.InputStream;
import java.util.regex.Pattern;



/**
 * Provides utility text scanning routines for tests and Managers to use, intended for use with logs or batch jobs etc.
 * <br>
 * The implementation of this interface will not record the text being searched, so the implementation
 * can be reused against different resources.
 * <br>
 * If you require the ability to remember where the scan reached in a "log", use the {@link ILogScanner}.
 * <br>
 * You can obtain an implementation of this interface using the {@link TextScanner} annotation.
 * 
 *  
 *
 */
public interface ITextScanner {
    
    /**
     * Search a String for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * 
     * @param text The text to be searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failurePattern was found
     * @throws MissingTextException If no occurrences of the searchPattern were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found 
     * @throws TextScanException If any other problem found 
     */
    ITextScanner scan(String text, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scan(text, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param text The text to be searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchLiteral must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failureLiteral was found
     * @throws MissingTextException If no occurrences of the searchLiteral were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found
     * @throws TextScanException If any other problem found
     */
    ITextScanner scan(String text, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Search a IScannable for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * 
     * @param scannable The scannable to be searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failurePattern was found
     * @throws MissingTextException If no occurrences of the searchPattern were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found
     * @throws TextScanException If any other problem found
     */
    ITextScanner scan(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scan(scannable, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param scannable The scannable to be searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchLiteral must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failureLiteral was found
     * @throws MissingTextException If no occurrences of the searchLiteral were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found
     * @throws TextScanException If any other problem found
     */
    ITextScanner scan(ITextScannable scannable, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Search an InputStream for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>
     * NOTE: unlike the scannable/string scans, this method will scan the text on a line by line basis, using a BufferedReader, to prevent the JVM Heap from being exceeded.
     * therefore you will not be able to use multiline patterns
     * 
     * @param inputStream The inputStream  to be searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchPattern must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failurePattern was found
     * @throws MissingTextException If no occurrences of the searchPattern were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found
     * @throws TextScanException If any other problem found
     */
    ITextScanner scan(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scan(inputStream, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param inputStream The inputStream to be searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchText must exist
     * @return This text scanner for fluent calls
     * @throws FailTextFoundException If the failureLiteral was found
     * @throws MissingTextException If no occurrences of the searchLiteral were found
     * @throws IncorrectOccurrencesException If incorrect number of occurrences were found
     * @throws TextScanException If any other problem found
     */
    ITextScanner scan(InputStream inputStream, String searchLiteral, String failLiteral, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Search a String for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>Useful for returning the actual value of the searchPattern or failPattern
     * 
     * @param text the text being searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchPattern or failPattern found
     * @throws MissingTextException The searchPattern was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(String text, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scanForMatch(text, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param text the text being searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchLiteral or failLiteral found
     * @throws MissingTextException The searchLiteral was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(String text, String searchLiteral, String failLiteral, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Search a IScannable for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>Useful for returning the actual value of the searchPattern or failPattern.
     * 
     * @param scannable the scannable being searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchPattern or failPattern found
     * @throws MissingTextException The searchPattern was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scanForMatch(scannable, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param scannable the text being searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchLiteral or failLiteral found
     * @throws MissingTextException The searchLiteral was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(ITextScannable scannable, String searchLiteral, String failLiteral, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Search an InputStream for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>
     * NOTE: unlike the scannable/string scans, this method will scan the text on a line by line basis, using a BufferedReader, to prevent the JVM Heap from being exceeded.
     * therefore you will not be able to use multiline patterns.
     * <br>Useful for returning the actual value of the searchPattern or failPattern.
     * 
     * @param inputStream the inputStream being searched
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchPattern or failPattern found
     * @throws MissingTextException The searchPattern was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scanForMatch(inputStream, Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param inputStream the text being searched
     * @param searchLiteral The exact text to search for
     * @param failLiteral The exact failure text to search for, can be null meaning no fail search
     * @param occurrence The occurrence to be returned
     * @return The text of the searchLiteral or failLiteral found
     * @throws MissingTextException The searchLiteral was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found
     */
    String scanForMatch(InputStream inputStream, String searchLiteral, String failLiteral, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
}
