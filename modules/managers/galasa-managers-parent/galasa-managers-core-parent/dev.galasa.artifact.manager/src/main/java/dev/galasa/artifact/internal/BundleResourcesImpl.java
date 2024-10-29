/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.artifact.internal;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import dev.galasa.artifact.IBundleResources;
import dev.galasa.artifact.ISkeletonProcessor;
import dev.galasa.artifact.SkeletonProcessorException;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.artifact.ISkeletonProcessor.SkeletonType;
import dev.galasa.framework.spi.IFramework;

public class BundleResourcesImpl implements IBundleResources {

    private static final String      FILE_SEPARATOR = "/";

    private final Bundle             bundle;

    private final ISkeletonProcessor velocitySkeletonProcessor;
    private final ISkeletonProcessor ppSkeletonProcessor;

    private static final Log         logger         = LogFactory.getLog(BundleResourcesImpl.class);

    public BundleResourcesImpl(Class<?> owningClass, IFramework framework) {
        this.bundle = FrameworkUtil.getBundle(owningClass);

        this.velocitySkeletonProcessor = new VelocitySkeletonProcessor(framework);
        this.ppSkeletonProcessor = new PlusPlusSkeletonProcessor(framework);
    }

    public Map<String, InputStream> retrieveDirectoryContents(String directory) throws TestBundleResourceException {

        HashMap<String, InputStream> directoryContents = new HashMap<>();

        List<String> contentPaths = listDirectory(bundle, directory, null);

        for (String path : contentPaths) {
            directoryContents.put(path, retrieveFile(path));
        }

        return directoryContents;
    }

    public InputStream retrieveFile(String filename) throws TestBundleResourceException {

        filename = normalisePath(filename);

        logger.debug("Searching for artifact: " + filename + " in bundle " + bundle.getSymbolicName());

        InputStream is = null;

        URL fileURL = bundle.getEntry(filename);

        if (fileURL != null) {
            String urlString = fileURL.toString();
            if (!urlString.contains(filename)) {
                throw new TestBundleResourceException(
                        "The found artifact '" + fileURL.getPath() + "' does not match the case of '" + filename + "'");
            }
            try {
                is = fileURL.openStream();
            } catch (IOException e) {
                logger.error("IO Error accessing file: " + filename, e);
                throw new TestBundleResourceException(e);
            }

            return is;
        }

        throw new TestBundleResourceException(
                "No such artifact: " + filename + " in bundle " + bundle.getSymbolicName());
    }

    /**
     * This is now horrendously complicated, bear with me
     */
    public InputStream retrieveJar(String symbolicName, String version, String directory)
            throws TestBundleResourceException {

        // Ensure our directory name begins with a file separator so that we always
        // search relative to the bundle root
        if ((!"".equals(directory)) && (!directory.startsWith(FILE_SEPARATOR))) {
            directory = FILE_SEPARATOR + directory;
        }

        // If we have a version then hopefully the jar is neatly named
        // <symbolicName>_<version>.jar or <symbolicName>-<version>.jar 
        // and we can do a simple file locate
        InputStream jis = null;
        if (!"".equals(version)) {
            String expectedJarName = directory + symbolicName + "_" + version + ".jar";
            String expectedJarNameHyphen = directory + symbolicName + "-" + version + ".jar";

            try {
                jis = retrieveFile(expectedJarName);
                return jis;
            } catch (TestBundleResourceException e) {
                logger.debug("Failed to find jar under expected name with underscore separator. Will check for hyphen separator");
            }
            try {
                jis = retrieveFile(expectedJarNameHyphen);
                return jis;
            } catch (TestBundleResourceException e) {
                logger.debug("Failed to find jar under expected name with hyphen separator. Will inspect manifests to locate it.", e);
            }
        }

        // If that didn't work we call listJars to find all the jars in the directory
        // and bundle given
        List<String> foundJars = listDirectory(bundle, directory, "jar");
        
        logger.info("Found Jars:" + foundJars);

        // Assuming we have some jars to inspect, lets inspect them
        if (!foundJars.isEmpty()) {
            String bestJar = null;
            String bestVersion = "0.0.0";
            for (String jar : foundJars) {

                if (!jar.startsWith(FILE_SEPARATOR)) {
                    jar = FILE_SEPARATOR + jar;
                }

                // Put the jar entry into a JarInputStream
                JarInputStream jaris = null;
                try {
                    jaris = new JarInputStream(bundle.getEntry(jar).openStream());
                } catch (IOException e) {
                    throw new TestBundleResourceException("Unable to open a stream into " + jar, e);
                }
                
                // Grab the attributes from the manifest
                Attributes attributes = jaris.getManifest().getMainAttributes();
                
                try {
                    jaris.close();
                } catch (IOException e) {
                    logger.warn("Unable to close jar input stream", e);
                }
                
                // Convert JarInputStream to InputStream and store contents as string 
                InputStream is = retrieveFile(jar);
                
				String jarContents = null;
				try {
					jarContents = streamAsString(is);
				} catch (IOException e) {
                    throw new TestBundleResourceException("Unable to store InputStream as string", e);
				}
							
				Boolean matchingNames = false;
				String bundleSymbolicName = attributes.getValue("Bundle-SymbolicName");
				
				//Checks if the symbolic name is listed in the manifest 
				if (bundleSymbolicName != null) {
					if (bundleSymbolicName.equals(symbolicName)) {
					matchingNames = true;
					}
				}
                // We don't know exactly what type of jar this is, so we don't know the names of
                // the exact contents,
                // but if the contents contain the passed symbolic name it's a fair bet it's the
                // right name
                if (jarContents.contains(symbolicName) || matchingNames == true){

                    // If we were passed a version let's inspect all the attributes to see if one of
                    // them looks like a version
                    boolean hasVersion = false;

                    for (Entry<Object, Object> entry : attributes.entrySet()) {

                        // If something looks like a version, check it matches the passed version
                        if (entry.getKey().toString().toLowerCase().matches("(bundle-)?version")) {

                            String foundVersion = (String) entry.getValue();

                            // If we weren't passed a version then we'll assume this jar is the correct one
                            if (isVersionInRange(foundVersion, version)) {
                                logger.debug(jar + " has the correct symbolic name and version.");
                                if (compareVersions(foundVersion, bestVersion) >= 0) {
                                    bestJar = jar;
                                    bestVersion = foundVersion;
                                }
                            }

                            hasVersion = true;
                            break;
                        }
                    }

                    // If we found no attribute which looked like a version we'll just assume that
                    // this is the right one.
                    if (!hasVersion) {
                        logger.debug(jar + " has the correct symbolic name, but no version was found to inspect.");
                        if (bestJar == null) {
                            bestJar = jar;
                        }
                    }
                }
            }

            if (bestJar != null) {
                logger.debug("Selected '" + bestJar + "'");
                return retrieveFile(bestJar);
            }
        }

        throw new TestBundleResourceException(
                "No jar found with symbolicName=" + symbolicName + " and version=" + version);
    }

    /**
     * return 1 if version1 > version2 return -1 if version1 < version2 return 0 if
     * version1 == version2
     * 
     * @param version1
     * @param version2
     * @return
     */
    private int compareVersions(String version1, String version2) {

        // Split versions up by "."
        String[] version1Array = version1.split("\\.");
        String[] version2Array = version2.split("\\.");

        // Work through the arrays from left to right comparing the values
        int maxLength = Math.max(version1Array.length, version2Array.length);
        for (int i = 0; i < maxLength; i++) {
            long n1 = getLongtAtIndex(version1Array, i);
            long n2 = getLongtAtIndex(version2Array, i);

            if (n2 > n1) {
                return -1;
            }

            if (n2 < n1) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Get the long from a given index in a string array assume zero if we are
     * out of bounds
     */
  
    private long getLongtAtIndex(String[] arr, int index) {

    	if (arr.length <= index) {
            return 0;
        }
        
        return Long.parseLong(arr[index]);
    }
    

    /**
     * return true if a given version lies within a given range
     * 
     * @param version
     * @param range
     * @return
     */
    private boolean isVersionInRange(String version, String range) {

        if (range.isEmpty()) {
            return true;
        }

        if (range.matches("[\\d\\.]+")) {
            return version.equals(range);
        }

        /*
         * Regex with 4 groups: ^([\\[\\(]) - beginning of string: '[' or '('
         * ([\\d+\\.]+) - some quantity of numbers and '.'s. should be the lower bound
         * (,[\\d+\\.]+)? - as above, prefixed with ',' and optional. should be the
         * upper bound if there is one ([\\)\\]])$ - reverse of the first group: ']' or
         * ')'
         */
        Pattern p = Pattern.compile("^([\\[\\(])([\\d+\\.]+)(,[\\d+\\.]+)?([\\)\\]])$", Pattern.MULTILINE);
        Matcher m = p.matcher(range);

        if (m.find()) {
            boolean exclusiveLower = "(".equals(m.group(1));
            boolean exclusiveUpper = ")".equals(m.group(4));

            String lower = m.group(2);
            String upper = m.group(3).replaceFirst(",", "");

            int dLower = compareVersions(lower, version);
            if (dLower > 0 || (exclusiveLower && dLower == 0)) {
                return false;
            }

            if (upper != null) {

                int dUpper = compareVersions(version, upper);

                if (dUpper > 0 || (exclusiveUpper && dUpper == 0)) {
                    return false;
                }
            }

            return true;
        }

        logger.error("Unparsable versionRange: '" + range + "'");

        return false;
    }

    @Override
    public InputStream zipDirectoryContents(String resourcesDirectory, Map<String, Object> parameters, String encoding,
            boolean gzip) throws TestBundleResourceException {
        try {

            resourcesDirectory = resourcesDirectory.replaceFirst("\\/*" + RESOURCES_DIRECTORY + "\\/*", "");

            // Create a byte array output stream to write to
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipos;

            // If gzip compression is requested output via gzip and buffer, otherwise go
            // straight to buffer
            if (gzip) {
                zipos = new ZipOutputStream(new GZIPOutputStream(new BufferedOutputStream(baos)));
            } else {
                zipos = new ZipOutputStream(new BufferedOutputStream(baos));
            }

            List<String> contents = listDirectory(bundle, resourcesDirectory, null);

            // If there is no encoding required then this is a binary copy
            // we just want to write the bytes straight into the tarball
            // so we don't risk corrupting anything that is actually binary
            if (encoding == null) {

                // Create our tarball
                for (String path : contents) {

                    byte[] b = IOUtils.toByteArray(retrieveSkeletonFile(path, parameters));

                    // TarEntry needs to be instantiated with a File - use a dummy and set the size
                    // manually to avoid IOExceptions
                    String zipEntryName = path
                            .replaceFirst("\\/*" + RESOURCES_DIRECTORY + "\\/*" + resourcesDirectory + "\\/*", "");
                    ZipEntry zipEntry = new ZipEntry(zipEntryName);
                    zipEntry.setSize(b.length);

                    // Start the new entry
                    zipos.putNextEntry(zipEntry);

                    // Write the entry's content
                    zipos.write(b);

                    zipos.flush();
                }

                // If there is encoding required then we assume that everything
                // we are given can be safely converted to a char[] without risk
                // of corruption - if anyone wanted their actual binary re-encoded
                // they are going to get nonsense anyway...
            } else {
                OutputStreamWriter osw = new OutputStreamWriter(zipos, encoding);

                // Create our tarball
                for (String path : contents) {

                    char[] c = IOUtils.toCharArray(retrieveSkeletonFile(path, parameters));

                    // TarEntry needs to be instantiated with a File - use a dummy and set the size
                    // manually to avoid IOExceptions
                    String zipEntryName = path
                            .replaceFirst("\\/*" + RESOURCES_DIRECTORY + "\\/*" + resourcesDirectory + "\\/*", "");
                    ZipEntry zipEntry = new ZipEntry(zipEntryName);
                    zipEntry.setSize(c.length);

                    // Start the new entry
                    zipos.putNextEntry(zipEntry);

                    // Write the entry's content through the writer to convert
                    osw.write(c);

                    osw.flush();
                    zipos.flush();
                }

                osw.close();
            }

            zipos.close();

            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new TestBundleResourceException("Error attempting to create zip", e);
        }
    }

    private List<String> listDirectory(Bundle bundle, String directory, String fileExtension) {
    	
    	if(fileExtension != null && !fileExtension.startsWith(".")){
    		fileExtension = "." + fileExtension.toLowerCase();
    	}

        List<String> directoryContents = new ArrayList<>();

        directory = normalisePath(directory);

        Enumeration<String> entryPaths = bundle.getEntryPaths(directory);

        if (entryPaths != null) {

            while (entryPaths.hasMoreElements()) {
                String entryPath = entryPaths.nextElement();

                if (entryPath.endsWith(FILE_SEPARATOR)) {

                    List<String> childDirectoryContents = listDirectory(bundle, entryPath, fileExtension);

                    if (!childDirectoryContents.isEmpty()) {
                        directoryContents.addAll(childDirectoryContents);
                    }

                    continue;
                }

                if (fileExtension != null) {
                    if (entryPath.toLowerCase().endsWith(fileExtension)) {
                        directoryContents.add(entryPath);
                    }
                } 
                else {
                    directoryContents.add(entryPath);
                }
            }
        }

        return directoryContents;
    }

    private String normalisePath(String rawPath) {

        rawPath = rawPath.replaceAll("[\\/\\\\]", FILE_SEPARATOR);

        if (rawPath.startsWith("." + FILE_SEPARATOR))
            rawPath = rawPath.replaceFirst("\\.", "");

        if (!rawPath.startsWith(FILE_SEPARATOR))
            rawPath = FILE_SEPARATOR + rawPath;

        rawPath = rawPath.replaceAll("\\/+", "/");
        rawPath = rawPath.replaceFirst(FILE_SEPARATOR, "");

        return rawPath;
    }

    @Override
    public InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters)
            throws TestBundleResourceException {
        return retrieveSkeletonFile(path, parameters, SkeletonType.PLUSPLUS);
    }

    @Override
    public InputStream retrieveSkeletonFile(String path, Map<String, Object> parameters, int skeletonType)
            throws TestBundleResourceException {

        InputStream skeletonis = retrieveFile(path);

        if (parameters == null || parameters.isEmpty()) {
            return skeletonis;
        }

        InputStream processedis;
        try {
            processedis = selectProcessor(skeletonType).processSkeleton(skeletonis, parameters);
        } catch (Exception e) {
            throw new TestBundleResourceException("Error whilst attempting to process skeleton from " + path, e);
        }

        return processedis;
    }

    @Override
    public Map<String, InputStream> retrieveSkeletonDirectoryContents(String directory, Map<String, Object> parameters,
            int skeletonType) throws TestBundleResourceException {

        Map<String, InputStream> skeletons = retrieveDirectoryContents(directory);
        if (parameters == null || parameters.isEmpty()) {
            return skeletons;
        }
        HashMap<String, InputStream> processedSkeletons = new HashMap<>();

        for (Entry<String, InputStream> entry : skeletons.entrySet()) {
            InputStream is;
            try {
                is = selectProcessor(skeletonType).processSkeleton(entry.getValue(), parameters);
            } catch (Exception e) {
                throw new TestBundleResourceException(
                        "Error whilst attempting to process skeleton from " + entry.getKey(), e);
            }
            processedSkeletons.put(entry.getKey(), is);
        }

        return processedSkeletons;
    }

    @Override
    public List<String> streamAsList(InputStream inputStream) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        List<String> lines = new ArrayList<>();

        while ((line = br.readLine()) != null) {
            lines.add(line);
        }

        inputStream.close();

        return lines;
    }

    @Override
    public String streamAsString(InputStream inputStream) throws IOException {

        char [] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();
        Reader input = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        for(int read; (read = input.read(buffer,0,buffer.length)) > 0; ){
            sb.append(buffer,0,read);
        }

        inputStream.close();
        return sb.toString();
    }

    private ISkeletonProcessor selectProcessor(int skeletonType) throws SkeletonProcessorException {

        switch (skeletonType) {

            case SkeletonType.PLUSPLUS:
                return ppSkeletonProcessor;
            case SkeletonType.VELOCITY:
                return velocitySkeletonProcessor;
            default:
                throw new SkeletonProcessorException("SkeletonType '" + skeletonType + "' is not a valid type");
        }
    }

    @Override
    public String retrieveFileAsString(String path) throws TestBundleResourceException, IOException {
        return this.streamAsString(this.retrieveFile(path));
    }

    @Override
    public String retrieveSkeletonFileAsString(String path, Map<String, Object> parameters)
            throws TestBundleResourceException, IOException {
        return this.streamAsString(this.retrieveSkeletonFile(path, parameters));
    }

}
