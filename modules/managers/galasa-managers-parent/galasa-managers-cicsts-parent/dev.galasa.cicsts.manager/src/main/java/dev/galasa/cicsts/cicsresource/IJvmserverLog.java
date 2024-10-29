/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cicsresource;

import java.io.OutputStream;
import java.util.regex.Pattern;

import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosfile.IZosUNIXFile;

public interface IJvmserverLog {
	
	/**
	 * Returns the name of the specified log file or DDNAME
	 * @return the file or DDNAME name
	 * @throws CicsJvmserverResourceException 
	 */
	public String getName() throws CicsJvmserverResourceException;
	
	/**
	 * Is this JVM server log a {@link IZosUNIXFile}
	 * @return
	 */
	public boolean isZosUNIXFile();
	
	/**
	 * Is this JVM server log a {@link IZosBatchJobOutputSpoolFile}
	 * @return
	 */
	public boolean isZosBatchJobSpoolFile();
	
	/**
	 * Returns the {@link IZosUNIXFile} associated with this {@link IJvmserverLog}
	 * @return JVM server log file
	 * @throws CicsJvmserverResourceException
	 */
	public IZosUNIXFile getZosUNIXFile() throws CicsJvmserverResourceException;

	/**
	 * Returns the {@link IZosBatchJobOutputSpoolFile} associated with this {@link IJvmserverLog}
	 * @return JVM server log spool file
	 * @throws CicsJvmserverResourceException
	 */
	public IZosBatchJobOutputSpoolFile getZosBatchJobOutputSpoolFile() throws CicsJvmserverResourceException;
	
	/**
	 * Returns the contents of a specified log
	 * @return contents of log
	 * @throws CicsJvmserverResourceException 
	 */
	public OutputStream retrieve() throws CicsJvmserverResourceException;
	
	/**
	 * Delete the {@link IZosUNIXFile} if it exists. 
	 * 
	 * @throws CicsJvmserverResourceException the log is a {@link IZosBatchJobOutputSpoolFile}
	 */
	public void delete() throws CicsJvmserverResourceException;

	/**
	 * Save the log to the Results Archive Store
	 * @param rasPath
	 * @throws CicsJvmserverResourceException
	 */
	public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException;

	/**
	 * Checkpoint this {@link IJvmserverLog}
	 * @return checkpoint
	 * @throws CicsJvmserverResourceException 
	 */
	public long checkpoint() throws CicsJvmserverResourceException;
	
	/**
	 * Return the current checkpoint on this {@link IJvmserverLog}
	 * @return checkpoint 
	 */
	public long getCheckpoint();

	/**
	 * Returns the contents of log since the last checkpoint
	 * @return contents of log
	 * @throws CicsJvmserverResourceException 
	 */
	public OutputStream retrieveSinceCheckpoint() throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search text
	 * 
	 * @param searchText the text to search
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForText(String searchText) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search or fail String
	 * 
	 * @param searchText the text to search
	 * @return true if text found
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForText(String searchText, String failText) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search text String since the last checkpoint
	 * @param searchText the text to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForTextSinceCheckpoint(String searchText) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search or fail String since the last checkpoint
	 * 
	 * @param searchText the text to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForTextSinceCheckpoint(String searchText, String failText) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search Pattern
	 * 
	 * @param searchPattern the Pattern to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForPattern(Pattern searchPattern) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search or fail Pattern
	 * 
	 * @param searchPattern the Pattern to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForPattern(Pattern searchPattern, Pattern failPattern) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search Pattern since the last checkpoint
	 * @param searchPattern the Pattern to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForPatternSinceCheckpoint(Pattern searchPattern) throws CicsJvmserverResourceException;

	/**
	 * Searches contents of log for specified search or fail Pattern since the last checkpoint
	 * @param searchPattern the Pattern to search
	 * @param failPattern the failure pattern to search
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException 
	 */
	public String searchForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search String to appear in specified log. 
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found</li>
	 * <li>the failText is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchText the text to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForText(String searchText, long timeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search String or fail text to appear in specified log.
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found</li>
	 * <li>the failText is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchText the text to search
	 * @param failText the failure text to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForText(String searchText, String failText, long timeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search String to appear in specified log since the last checkpoint. 
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found</li>
	 * <li>the failText is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * @param searchText the text to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForTextSinceCheckpoint(String searchText, long timeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search or fail String  to appear in specified log since the last checkpoint. 
	 *
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchText is found</li>
	 * <li>the failText is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchText the text to search
	 * @param failText the failure text to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForTextSinceCheckpoint(String searchText, String failText, long timeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search Pattern to appear in specified log. Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found;</li>
	 * <li>the failPattern is found;</li>
	 * <li>the specified timeout is reached.</li>
	 * </ul>
	 * @param searchPattern the Pattern to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPattern(Pattern searchPattern, long timeout) throws CicsJvmserverResourceException;

	/**
	 * Wait for a search or fail Pattern or fail Pattern to appear in specified log. 
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found</li>
	 * <li>the failPattern is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchPattern the Pattern to search
	 * @param failPattern the failure pattern to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPattern(Pattern searchPattern, Pattern failPattern, long timeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search Pattern to appear in specified log since the last checkpoint.
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found</li>
	 * <li>the failPattern is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchPattern the Pattern to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPatternSinceCheckpoint(Pattern searchPattern, long timeout) throws CicsJvmserverResourceException;
	
	/**
	 * Wait for a search or fail Pattern or fail Pattern to appear in specified log since the last checkpoint. 
	 * 
	 * Will check every 3 seconds until one of:
	 * <ul>
	 * <li>the searchPattern is found</li>
	 * <li>the failPattern is found</li>
	 * <li>the specified timeout is reached</li>
	 * </ul>
	 * 
	 * @param searchPattern the Pattern to search
	 * @param failPattern the failure pattern to search
	 * @param timeout timeout in seconds
	 * @return the string found or null
	 * @throws CicsJvmserverResourceException
	 */
	public String waitForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern, long timeout) throws CicsJvmserverResourceException;
}
