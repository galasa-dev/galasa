/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import dev.galasa.framework.IFileSystem;

import java.nio.file.DirectoryStream.Filter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;


/**
 * - A directory is a file system.
 * - Each directory holds a map of nodes in that directory.
 * - Each node could be a file, or another directory.
 * - Each file system knows where it is mounted.
 */
public class MockFileSystem extends FileSystem implements IFileSystem {

    private static class Node {
        // Path path;
        boolean isFolder ;
        byte[] contents ;
    }

    // A map of parts of the path, and the node it relates to.
    private Map<String,Node> files = new HashMap<>();

    public boolean isFolder(Path folderPath) {
        boolean isFolder = false ;
        Node node = files.get(folderPath.toString());
        if (node != null) {
            isFolder = node.isFolder;
        }
        return isFolder;
    }

    public byte[] getContentsAsBytes(Path filePath) {
        Node node = files.get(filePath.toString());
        byte[] results = null; 
        if (node != null) {
            results = node.contents;
        }
        return results;
    }

    public String getContentsAsString(Path filePath) {
        byte[] bytes = getContentsAsBytes(filePath);
        String results = null;
        if (bytes != null ) {
            results = new String(bytes);
        }
        return results;
    }

    public void setFileContents(Path filePath, String contents) {
        Node node = files.get(filePath.toString());
        if (node != null) {
            node.contents = contents.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public void createDirectories(Path folderPath) throws IOException {
        if (folderPath!=null) {
            if(!exists(folderPath)) {
                // Recursively make sure the parent folder exists.
                Path parent = folderPath.getParent();
                createDirectories(parent);
                // Now create this folder.
                createNode(folderPath, true);
            }
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        if (exists(path)) {
            throw new IOException("File "+path.toString()+" already exists!");
        }
        createNode(path, false);
    }

    private void createNode(Path path, boolean isFolder) {
        Node node = new Node();
        node.isFolder = isFolder ;
        node.contents = new byte[0];
        // node.path = path ;
        files.put(path.toString(),node);
    }

    @Override
    public boolean exists(Path pathToFolderOrFile) {
        boolean isExists ;
        Node node = this.files.get(pathToFolderOrFile.toString());
        if (node == null) {
            isExists = false ;
        } else {
            isExists = true ;
        }
        return isExists;
    }

    @Override
    public boolean isRegularFile(Path filePath) {
        return !files.get(filePath.toString()).isFolder;
    }

    @Override
    public boolean isDirectory(Path filePath) {
        return files.get(filePath.toString()).isFolder;
    }

    @Override
    public Stream<Path> walk(Path folderPath) {
        return files.keySet().stream()
            .filter(path -> path.startsWith(folderPath.toString()))
            .map(path -> new MockPath(path.toString(), this));
    }

    @Override
    public FileSystemProvider provider() {
        return new MockFileSystemProvider(this);
    }

    @Override
    public long size(Path folderPath) throws IOException {
        return 0;
    }

    @Override
    public Path getPath(String first, String... more) {
        return new MockPath(first + String.join("/", more), this);
    }

    public String probeContentType(Path path) throws IOException {
        String contentType = "application/octet-stream";
        if (path.toString().endsWith(".properties") 
            || path.toString().endsWith(".txt")) {
          contentType =  "text/plain";
        } else if (path.toString().endsWith(".gz")) {
            contentType = "application/x-gzip";
        } else if (path.toString().endsWith(".json")) {
            contentType = "application/json";
        }
        return contentType;
    }

    public List<Path> getListOfFiles(String directory, Filter<? super Path> filter) throws IOException {
        List<Path> resultPaths = new ArrayList<>();
    
        for (String pathStr : files.keySet()) {
            // Check if this path is to a direct child of the given directory
            if (pathStr.startsWith(directory + "/") && !(pathStr.substring(directory.length() + 1).contains("/"))) {
                MockPath mockPath = new MockPath(pathStr, this);

                if (filter.accept(mockPath)) {
                    resultPaths.add(mockPath);
                }
            }
        }
        return resultPaths;
    }

    public List<Path> getListOfAllFiles() throws IOException {
        List<Path> resultPaths = new ArrayList<>();
        for (String pathStr : files.keySet()) {
            Node node = files.get(pathStr);
            if (!node.isFolder) {
                MockPath mockPath = new MockPath(pathStr, this);
                resultPaths.add(mockPath);
            }
        }
        return resultPaths;
    }

	@Override
	public InputStream newInputStream(Path folderPath) throws IOException {
        if (exists(folderPath)) {
            return new ByteArrayInputStream(files.get(folderPath.toString()).contents);
        }
        return null;
	}

    @Override
    public Path createFile(Path path, FileAttribute<?>... attrs) throws IOException {
        createFile(path);
        return path;
    }

    @Override
    public void write(Path path, byte[] bytes) throws IOException {
        Node node = this.files.get(path.toString());
        if (node != null) {
            node.contents = bytes;
        } else {
            createNode(path, false);
            write(path,bytes);
        }
    }


    @Override
    public List<String> readLines(URI uri) throws IOException {
        String filePathStr = uri.getPath();
        Path filePath = getPath(filePathStr);

        if (!exists(filePath)) {
            throw new FileNotFoundException("File "+filePathStr+" was not found");
        }
        String contents = getContentsAsString(filePath);

        ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
        InputStreamReader sourceReader = new InputStreamReader(source);
        List<String> lines = IOUtils.readLines(sourceReader);
        return lines ;
    }


    @Override
    public String readString(Path path) throws IOException {
        if (!exists(path)) {
            throw new FileNotFoundException("File "+path+" was not found");
        }
        return getContentsAsString(path);
    }

    // -------------- Un-implemented methods follow ------------------

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Unimplemented method 'isReadOnly'");
    }

    @Override
    public String getSeparator() {
        throw new UnsupportedOperationException("Unimplemented method 'getSeparator'");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        throw new UnsupportedOperationException("Unimplemented method 'getRootDirectories'");
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException("Unimplemented method 'getFileStores'");
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException("Unimplemented method 'supportedFileAttributeViews'");
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Unimplemented method 'getPathMatcher'");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Unimplemented method 'getUserPrincipalLookupService'");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'newWatchService'");
    }
}