/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import java.io.OutputStream;
import java.util.regex.Pattern;

import dev.galasa.zosfile.IZosUNIXFile;

public interface IZosLibertyServerLog {
    
    /**
     * Returns the name of the specified log file
     * @return the file name
     * @throws ZosLibertyServerException 
     */
    public String getName() throws ZosLibertyServerException;

    /**
     * Returns the {@link IZosUNIXFile} associated with this {@link IZosLibertyServerLog}
     * @return Liberty server log file
     * @throws ZosLibertyServerException
     */
    public IZosUNIXFile getZosUNIXFile() throws ZosLibertyServerException;
    
    /**
     * Returns the contents of a specified log
     * @return contents of log
     * @throws ZosLibertyServerException 
     */
    public OutputStream retrieve() throws ZosLibertyServerException;
    
    /**
     * Delete the {@link IZosUNIXFile} if it exists
     * @throws ZosLibertyServerException 
     */
    public void delete() throws ZosLibertyServerException;

    /**
     * Save the log to the Results Archive Store
     * @param rasPath
     * @throws ZosLibertyServerException
     */
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException;

    /**
     * Checkpoint this {@link IZosLibertyServerLog}
     * @return checkpoint
     * @throws ZosLibertyServerException 
     */
    public long checkpoint() throws ZosLibertyServerException;
    
    /**
     * Return the current checkpoint on this {@link IZosLibertyServerLog}
     * @return checkpoint 
     */
    public long getCheckpoint();

    /**
     * Returns the contents of log since the last checkpoint
     * @return contents of log
     * @throws ZosLibertyServerException 
     */
    public OutputStream retrieveSinceCheckpoint() throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search text
     * @param searchText the text to search
     * @return true if text found
     * @throws ZosLibertyServerException 
     */
    public String searchForText(String searchText) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search or fail String
     * @param searchText the text to search
     * @return true if text found
     * @throws ZosLibertyServerException 
     */
    public String searchForText(String searchText, String failText) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search text String since the last checkpoint
     * @param searchText the text to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForTextSinceCheckpoint(String searchText) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search or fail String since the last checkpoint
     * @param searchText the text to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForTextSinceCheckpoint(String searchText, String failText) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search Pattern
     * @param searchPattern the Pattern to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForPattern(Pattern searchPattern) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search or fail Pattern
     * @param searchPattern the Pattern to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForPattern(Pattern searchPattern, Pattern failPattern) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search Pattern since the last checkpoint
     * @param searchPattern the Pattern to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForPatternSinceCheckpoint(Pattern searchPattern) throws ZosLibertyServerException;

    /**
     * Searches contents of log for specified search or fail Pattern since the last checkpoint
     * @param searchPattern the Pattern to search
     * @return the string found or null
     * @throws ZosLibertyServerException 
     */
    public String searchForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern) throws ZosLibertyServerException;

    /**
     * Wait for a search search String to appear in specified log. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchText is found;</li>
     * <li>the failText is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchText the text to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForText(String searchText, long timeout) throws ZosLibertyServerException;

    /**
     * Wait for a search String or fail text to appear in specified log. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchText is found;</li>
     * <li>the failText is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchText the text to search
     * @param failText the failure text to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForText(String searchText, String failText, long timeout) throws ZosLibertyServerException;
    
    /**
     * Wait for a search String to appear in specified log since the last checkpoint. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchText is found;</li>
     * <li>the failText is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchText the text to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForTextSinceCheckpoint(String searchText, long timeout) throws ZosLibertyServerException;
    
    /**
     * Wait for a search or fail String  to appear in specified log since the last checkpoint. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchText is found;</li>
     * <li>the failText is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchText the text to search
     * @param failText the failure text to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForTextSinceCheckpoint(String searchText, String failText, long timeout) throws ZosLibertyServerException;

    /**
     * Wait for a search Pattern to appear in specified log. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchPattern is found;</li>
     * <li>the failPattern is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchPattern the Pattern to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForPattern(Pattern searchPattern, long timeout) throws ZosLibertyServerException;

    /**
     * Wait for a search or fail Pattern or fail Pattern to appear in specified log. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchPattern is found;</li>
     * <li>the failPattern is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchPattern the Pattern to search
     * @param failPattern the failure pattern to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForPattern(Pattern searchPattern, Pattern failPattern, long timeout) throws ZosLibertyServerException;
    
    /**
     * Wait for a search Pattern to appear in specified log since the last checkpoint. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchPattern is found;</li>
     * <li>the failPattern is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchPattern the Pattern to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, long timeout) throws ZosLibertyServerException;
    
    /**
     * Wait for a search or fail Pattern or fail Pattern to appear in specified log since the last checkpoint. Will check every 3 seconds until one of:
     * <ul>
     * <li>the searchPattern is found;</li>
     * <li>the failPattern is found;</li>
     * <li>the specified timeout is reached.</li>
     * </ul>
     * @param searchPattern the Pattern to search
     * @param failPattern the failure pattern to search
     * @param timeout timeout value in seconds
     * @return the string found or null
     * @throws ZosLibertyServerException
     */
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern, long timeout) throws ZosLibertyServerException;
}
