/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

import java.util.regex.Pattern;

/**
 * Provides utility text scanning routines for tests and Managers to use, intended for use with logs or batch jobs etc.
 * <br>
 * The scanner will remember where the last scan got to, so the next search will continue from that point.
 * <br>
 * You can obtain an implementation of this interface using the {@link LogScanner} annotation.
 * You will need a separate object per log you will be scanning.
 * 
 *  
 *
 */
public interface ILogScanner {
    
    /**
     * Set the scannable that will be used with this scanner and will update
     * 
     * @param scannable The scannable to be associated with this scanner
     * @return this interface for fluent use
     * @throws TextScanException If a scannable has already been set, updateText called or is null, or the first update fails
     */
    ILogScanner setScannable(ITextScannable scannable) throws TextScanException;
    
    /**
     * Update the scannable.
     * 
     * @return this interface for fluent use
     * @throws TextScanException
     */
    ILogScanner updateScannable() throws TextScanException;
    
    
    /**
     * Resets this scanner so it can be reused
     * 
     * @return this interface for fluent use
     */
    ILogScanner reset();
    
    
    /**
     * Sets the checkpoint to the end of the current log
     * 
     * @return this interface for fluent use
     * @throws TextScanException 
     */
    ILogScanner checkpoint() throws TextScanException;
    
    /**
	 * Manually set a checkpoint to the supplied value 
	 * 
	 * @return this interface for fluent use
	 * @throws TextScanException 
	 */
	ILogScanner setCheckpoint(long checkpoint) throws TextScanException;

	/**
     * Resets the checkpoint back to zero
     * 
     * @return this interface for fluent use
     */
    ILogScanner resetCheckpoint();
    
    
    /**
     * Returns the current position of the checkpoint. A value of -1 means the {@link ITextScannable} has not been checkpointed
     * @return the current checkpoint
     */
    long getCheckpoint();
    
    
    /**
	 * Search the log for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
	 * The search will find at least "count" number of searchPatterns in the text.
	 * <br>NOTE: This method will scan from the start of the log,  it will not use the checkpoint
	 * 
	 * @param searchPattern The regex to search for
	 * @param failPattern Failure regex to search for, can be null meaning no fail search
	 * @param count at least how many occurrences of the searchPattern must exist
	 * @return This log scanner for fluent calls
	 * @throws FailTextFoundException If the failurePattern was found
	 * @throws MissingTextException If no occurrences of the searchPattern was found 
	 * @throws IncorrectOccurrencesException If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
	 * @throws TextScanException If any other problem found 
	 */
	ILogScanner scan(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;

	/**
	 * Convenience method for scan(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
	 * 
	 * @param searchString The text to search for
	 * @param failString Failure text to search for, can be null meaning no fail search
	 * @param count at least how many occurrences of the searchText must exist
	 * @return This log scanner for fluent calls
	 * @throws FailTextFoundException If the failText was found
	 * @throws MissingTextException If no occurrences of the searchText was found 
	 * @throws IncorrectOccurrencesException If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
	 * @throws TextScanException If any other problem found 
	 */
	ILogScanner scan(String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;

	/**
     * Search the log for regex patterns, from the last checkpoint.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * <br>NOTE: This method will scan from the start of the log,  it will not use the checkpoint
     * 
     * @param searchPattern The regex to search for
     * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param count At least how many occurrences of the searchPattern must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException If the failurePattern was found
     * @throws MissingTextException If no occurrences of the searchPattern was found 
     * @throws IncorrectOccurrencesException If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws TextScanException If any other problem found 
     */
    ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scanSinceCheckpoint(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + failString + "\E"), count)
     * 
     * @param searchString The text to search for
     * @param failString Failure text to search for, can be null meaning no fail search
     * @param count at least how many occurrences of the searchString must exist
     * @return This log scanner for fluent calls
     * @throws FailTextFoundException If the failString was found
     * @throws MissingTextException If no occurrences of the searchString was found 
     * @throws IncorrectOccurrencesException If insufficient occurrences were found, if there are zero occurrences, then MissingTextException will be thrown
     * @throws TextScanException If any other problem found 
     */
    ILogScanner scanSinceCheckpoint(String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
	 * Search the log for regex patterns.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
	 * The search will find at least "count" number of searchPatterns in the text.
	 * Useful for returning the actual value of the searchPattern
	 * 
	 * @param searchPattern The regex to search for
	 * @param failPattern Failure regex to search for, can be null meaning no fail search
	 * @param occurrance The occurrence to be returned
	 * @return The text of the searchPattern found
	 * @throws MissingTextException The searchPattern was not found at all
	 * @throws IncorrectOccurrencesException If the specified occurrence was not found
	 * @throws TextScanException If any other problem found 
	 */
	String scanForMatch(Pattern searchPattern, Pattern failPattern, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException;

	/**
	 * Convenience method for scanForMatch(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + searchString + "\E"), occurrence)
	 * 
	 * @param searchString The text to search for
	 * @param failString Failure text to search for, can be null meaning no fail search
	 * @param occurrance The occurrence to be returned
	 * @return The text of the searchPattern found
	 * @throws MissingTextException The searchPattern was not found at all
	 * @throws IncorrectOccurrencesException If the specified occurrence was not found
	 * @throws TextScanException If any other problem found 
	 */
	String scanForMatch(String searchString, String failString, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException;

	/**
     * Search the log for regex patterns, from the last checkpoint.  It will search initially search for any occurrence of the failPattern before searching for the searchPattern.
     * The search will find at least "count" number of searchPatterns in the text.
     * Useful for returning the actual value of the searchPattern
     * 
     * @param searchPattern The regex to search for
	 * @param failPattern Failure regex to search for, can be null meaning no fail search
     * @param occurrance The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException The searchPattern was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found 
     */
    String scanForMatchSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
    
    /**
     * Convenience method for scanForMatchSinceCheckpoint(Pattern.Compile("\Q" + searchString + "\E"), Pattern.Compile("\Q" + searchString + "\E"), occurrence)
     * 
     * @param searchString The text to search for
	 * @param failString Failure text to search for, can be null meaning no fail search
     * @param occurrance The occurrence to be returned
     * @return The text of the searchPattern found
     * @throws MissingTextException The searchString was not found at all
     * @throws IncorrectOccurrencesException If the specified occurrence was not found
     * @throws TextScanException If any other problem found 
     */
    String scanForMatchSinceCheckpoint(String searchString, String failString, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException;
        
}
