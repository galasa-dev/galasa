/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IBundleResources {

    public static final String RESOURCES_DIRECTORY = "resources";

    /**
     * Retrieve a single file as an InputStream
     *
     * @param path
     * @return
     * @throws TestBundleResourceException
     */
    InputStream retrieveFile(String path) throws TestBundleResourceException;
    
    /**
     * Retrieve the contents of a file as a String
     * 
     * @param path The path to the file to which will be read
     * @return The contents of the file
     * @throws TestBundleResourceException
     * @throws IOException
     */
    String retrieveFileAsString(String path) throws TestBundleResourceException, IOException;

    /**
     * Retrieve a single file as an InputStream, if the file is a skeleton then any
     * required substitutions will be performed before the stream is returned.
     * 
     * @param path
     * @param parameters
     * @param skeletonType
     * @return
     * @throws TestBundleResourceException
     */
    InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters, int skeletonType)
            throws TestBundleResourceException;


    /**
     * Retrieve a single file as an InputStream
     * 
     * If the file is a skeleton then any
     * required substitutions will be performed before the stream is returned. 
     * 
     * Uses the default {@link ISkeletonProcessor}
     * 
     * @param path The path to the file
     * @param parameters
     * @return A stream so the caller can read the contents of the file
     * @throws TestBundleResourceException
     */
    InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters) throws TestBundleResourceException;


    /**
     * Retrieve a single file as a String
     * 
     * If the file is a skeleton then any required substitutions will 
     * be performed before the string is returned. 
     * 
     * Uses the default {@link ISkeletonProcessor}
     * 
     * @param path The path to the file to be read
     * @param parameters 
     * @return The contents of the file
     * @throws TestBundleResourceException
     */
    String retrieveSkeletonFileAsString(String path, Map<String, Object> parameters) throws TestBundleResourceException, IOException;


    /**
     * For a directory, retrieve a map of file paths within it, and InputStreams for
     * those files.
     * 
     * @param directory
     * @return
     * @throws TestBundleResourceException
     */
    Map<String, InputStream> retrieveDirectoryContents(String directory) throws TestBundleResourceException;

    /**
     * For a directory, retrieve a map of file paths within it, and InputStreams for
     * those files, if the file is a skeleton then any required substitutions will
     * be performed before the stream is returned.
     * 
     * @param directory
     * @return
     * @throws TestBundleResourceException
     */
    Map<String, InputStream> retrieveSkeletonDirectoryContents(String directory, Map<String, Object> parameters,
            int skeletonType) throws TestBundleResourceException;

    /**
     * Retrieve a jar file as an input stream
     * 
     * @param symbolicName
     * @param version
     * @param directory
     * @return
     * @throws TestBundleResourceException
     */
    InputStream retrieveJar(String symbolicName, String version, String directory) throws TestBundleResourceException;

    /**
     * Convert an input stream into a list of strings
     * 
     * @param file
     * @return
     * @throws IOException
     */
    List<String> streamAsList(InputStream file) throws IOException;

    /**
     * Convert an input stream into a string
     * 
     * @param file
     * @return
     * @throws IOException
     */
    String streamAsString(InputStream file) throws IOException;

    InputStream zipDirectoryContents(String resourcesDirectory, Map<String, Object> parameters, String encoding,
            boolean gzip) throws TestBundleResourceException;

}
