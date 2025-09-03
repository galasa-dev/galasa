/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ResultArchiveStoreContentType;

public class FileSystem implements IFileSystem {

    private static final Log logger = LogFactory.getLog(FileSystem.class);
    public FileSystem() {
    }

    @Override
    public void createDirectories(Path folderPath) throws IOException {
        Files.createDirectories(folderPath);
    }

    @Override
    public void createFile(Path filePath) throws IOException {
        Files.createFile(filePath);
    }

    @Override
    public boolean exists(Path pathToFolderOrFile ) {
        return pathToFolderOrFile.toFile().exists();
    }

    @Override
    public boolean isRegularFile(Path filePath) {
        return Files.isRegularFile(filePath);
    }

    @Override
    public boolean isDirectory(Path filePath) {
        return Files.isDirectory(filePath);
    }

    @Override
    public Stream<Path> walk(Path folderPath) throws IOException {
        return Files.walk(folderPath);
    }

    @Override
    public long size(Path folderPath) throws IOException {
        return Files.size(folderPath);
    }

    @Override
    public InputStream newInputStream(Path folderPath) throws IOException {
        return Files.newInputStream(folderPath);
    }

    public String probeContentType(Path path) throws IOException {
        logger.info("Probing the contentType of "+path.toString());
        String contentType = getFileAttributeValue(path, SupportedFileAttributeName.CONTENT_TYPE, ResultArchiveStoreContentType.BINARY.value());
        return contentType;
    }

    private String getFileAttributeValue(Path artifactPath, SupportedFileAttributeName supportedAttributeName, String defaultValue ) {
        String value = defaultValue;
        String attributeName = supportedAttributeName.getValue();
        try {
            Map<String,Object> attributes = Files.readAttributes(artifactPath, attributeName);
            if (attributes==null) {
                logger.info("getFileAttributeValue: Failed to get attribute "+attributeName+" from file at "+artifactPath+" defaulting to "+value);
            } else {
                Object obj = attributes.get(attributeName);
                if( obj == null ) {
                    logger.info("getFileAttributeValue: File "+artifactPath+" has attribute "+attributeName+" with a null value. defaulting to "+value);
                } else {
                    value = obj.toString();
                    logger.info("getFileAttributeValue: File "+artifactPath+" has attribute "+attributeName+" with a string value of:"+value);
                }
            }
        } catch(Exception ex) {
            logger.info("getFileAttributeValue: Failed to get attribute "+attributeName+" from artifact "+artifactPath+" defaulting to "+value,ex);
        }
        return value ;
    }

    @Override
    public Path createFile(Path path, FileAttribute<?>... attrs) throws IOException {
        return Files.createFile(path, attrs);
    }

    @Override
    public void write(Path path, byte[] bytes) throws IOException {
        Files.write(path,bytes);
    }

    @Override
    public List<String> readLines(URI uri) throws IOException {
        List<String> lines ;
        try {
            File fileToRead = new File(uri);
            lines = IOUtils.readLines(new FileReader(fileToRead));
        } catch (IOException e) {
            throw e ;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        return lines ;
    }

    @Override
    public String readString(Path path) throws IOException {
        return Files.readString(path);
    }
}
