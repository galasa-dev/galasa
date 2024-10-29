/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan;

import java.io.InputStream;

/**
 * This interface provides a access to a text resource that can be repeatedly updated and scanned for text.
 * Examples with be a running batch job, docker container log or a linux server log.
 * 
 * It is the intention that other Managers will provide IScannable objects for use within the Text Scan Manager
 *  
 *
 */
public interface ITextScannable {
    
    /**
     * Is the scannable object fetched by inputstream, used when the text file can be so large that it may 
     * exceed the JVM heap.
     * 
     * @return true if is an inputstream
     */
	public boolean isScannableInputStream();
    
    /**
     * Used for normal logs that fit easily in a standard String
     * 
     * @return true if it is a simple String object
     */
	public boolean isScannableString();
    
    
    /**
     * A name so the this scannable can be identified in exceptions or warning messages
     * 
     * @return a string to uniquely identify the scannable
     */
	public String getScannableName();
    
    /**
     * Update/Refresh the scannable text,  likely to be a no-op if this is an inputstream scannable. 
     * 
     * @return this scannable for fluent use
     * @throws TextScanException If the update fails
     */
	public ITextScannable updateScannable() throws TextScanException;
    
    /**
     * Fetch the InputStream for the scannable
     * 
     * @return The latest inputstream
     * @throws TextScanException If there is an error fetching the inputstream or it is not an inputstream
     */
	public InputStream getScannableInputStream() throws TextScanException;
    
    /**
     * Fetch the latest scannable text
     * 
     * @return The latest scannable text
     * @throws TextScanException if there is an error retrieving the text
     */
	public String getScannableString() throws TextScanException;
}
