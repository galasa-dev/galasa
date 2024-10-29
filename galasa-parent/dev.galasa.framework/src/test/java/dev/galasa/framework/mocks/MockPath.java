/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class MockPath implements Path {

    private final String path;

    protected MockFileSystem fileSystem;

    public MockPath(String path, MockFileSystem mockFileSystem) {
        this.path = path;
        this.fileSystem= mockFileSystem;
    }

    @Override
    public Path getParent() {
        int lastSeparator = path.lastIndexOf("/");
        if (lastSeparator == -1 || lastSeparator == 0) {
            // This path either has no parent or is the root path
            return null;
        } else {
            // This path has a parent, so return it
            String parentPath = path.substring(0, lastSeparator);
            return new MockPath(parentPath, this.fileSystem);
        }
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public Path getFileName() {
        String[] dirStructure = path.split("/");
        String result = dirStructure[dirStructure.length - 1];
        if (result.contains(".")) {
            return new MockPath(result, fileSystem);
        }
        return null;
    }

    @Override
    public Path resolve(Path other) {
        return new MockPath(this.path + "/" + other.toString(), fileSystem );
    }

    

    @Override
    public boolean isAbsolute() {
        throw new UnsupportedOperationException("Unimplemented method 'isAbsolute'");
    }

    @Override
    public Path getRoot() {
        throw new UnsupportedOperationException("Unimplemented method 'getRoot'");
    }


    @Override
    public int getNameCount() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Path getName(int index) {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException("Unimplemented method 'subpath'");
    }

    @Override
    public boolean startsWith(Path other) {
        throw new UnsupportedOperationException("Unimplemented method 'startsWith'");
    }

    @Override
    public boolean endsWith(Path other) {
        throw new UnsupportedOperationException("Unimplemented method 'endsWith'");
    }

    @Override
    public Path normalize() {
        throw new UnsupportedOperationException("Unimplemented method 'normalize'");
    }

    @Override
    public Path relativize(Path other) {
        throw new UnsupportedOperationException("Unimplemented method 'relativize'");
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("Unimplemented method 'toUri'");
    }

    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException("Unimplemented method 'toAbsolutePath'");
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'toRealPath'");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    @Override
    public int compareTo(Path other) {
        throw new UnsupportedOperationException("Unimplemented method 'compareTo'");
    }
    
}