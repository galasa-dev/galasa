/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SSHPath implements Path {

    private final SSHFileSystem  fileSystem;
    protected final List<String> nameElements = new ArrayList<>();
    protected final boolean      absolute;

    public SSHPath(SSHFileSystem sshFileSystem, String path) {
        this.fileSystem = sshFileSystem;
        if (path == null) {
            throw new NullPointerException();
        }

        // *** Normalise the path name by stripping out double // and any trailing /
        while (path.contains("//")) {
            path = path.replaceAll("\\Q//\\E", "/"); // NOSONAR
        }

        // *** Convert from windows format
        while (path.contains("\\")) {
            path = path.replaceAll("\\Q\\\\E", "/"); // NOSONAR
        }

        this.absolute = path.startsWith("/");
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // *** Break the path into elements
        int firstChar = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                if (firstChar != -1) {
                    this.nameElements.add(path.substring(firstChar, i));
                    firstChar = -1;
                }
            } else {
                if (firstChar == -1) {
                    firstChar = i;
                }
            }
        }

        if (firstChar >= 0) {
            this.nameElements.add(path.substring(firstChar, path.length()));
        }

        // *** Validate the elements
        for (final String part : this.nameElements) {
            if (".".equals(part)) {
                throw new InvalidPathException(path, "Path parts of '.' are not allowed");
            }
            if ("..".equals(part)) {
                throw new InvalidPathException(path, "Path parts of '..' are not allowed");
            }
            if (part.contains("~")) {
                throw new InvalidPathException(path, "Path parts with '~' are not allowed");
            }
            if (part.contains("=")) {
                throw new InvalidPathException(path, "Path parts with '=' are not allowed");
            }
        }

        // *** finally, check it can be converted to an URI
        try {
            toUri();
        } catch (final AssertionError e) {
            throw new AssertionError("Invalid path, would have conversion to URI", e);
        }
    }

    /**
     * Clone part of a pre-exist Path
     *
     * @param fileSystem   - The filesystem the path will be used on
     * @param absolute     - Is th path absolute, ie starts with /
     * @param nameElements - The elements of the path
     * @param start        - The start element to clone
     * @param end          - The end element to clone, with is the last + 1
     */
    protected SSHPath(SSHFileSystem fileSystem, boolean absolute, List<String> nameElements, int start, int end) {
        this.fileSystem = fileSystem;
        this.absolute = absolute;
        for (int i = start; i < end; i++) {
            this.nameElements.add(nameElements.get(i));
        }
    }

    @Override
    public int compareTo(Path other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean endsWith(Path other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean endsWith(String other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path getFileName() {
        if (nameElements.isEmpty()) {
            return new SSHPath(fileSystem, "");
        }
        return new SSHPath(fileSystem, false, nameElements, nameElements.size() - 1, nameElements.size());
    }

    @Override
    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    @Override
    public Path getName(int index) {
        return Paths.get(this.nameElements.get(index));
    }

    @Override
    public int getNameCount() {
        return this.nameElements.size();
    }

    @Override
    public Path getParent() {
        if (this.nameElements.isEmpty()) {
            return null;
        }
        return new SSHPath(this.fileSystem, this.absolute, this.nameElements, 0, this.nameElements.size() - 1);
    }

    @Override
    public Path getRoot() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean isAbsolute() {
        return this.absolute;
    }

    @Override
    public Iterator<Path> iterator() {
        ArrayList<Path> paths = new ArrayList<>(this.nameElements.size());
        
        for(String element : this.nameElements) {
            paths.add(new SSHPath(this.fileSystem, element));
        }
        
        return paths.iterator();
    }

    @Override
    public Path normalize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path relativize(Path other) {
        
        // TODO at the moment we don't support .. so can only
        // relativize with the same parent.
        
        if (this.absolute != other.isAbsolute()) {
            return other; // cannot relativize, so return as other
        }
        
        if (!(other instanceof SSHPath)) {
            return other; // TODO cannot support cross filesystems
        }
        
        SSHPath spother = (SSHPath) other;
        
        int i = 0;
        for(; i < this.nameElements.size(); i++) {
            if (spother.nameElements.size() <= i) {
                return other;
            }
            
            if (!this.nameElements.get(i).equals(spother.nameElements.get(i))) {
                break;
            }
        }
        
        boolean newAbsolute = (spother.absolute && (i == 0));

        
        SSHPath path = new SSHPath(fileSystem, newAbsolute, spother.nameElements, i, spother.nameElements.size());
        return path;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.nio.file.Path)
     */
    @Override
    public Path resolve(Path other) {
        final SSHPath o = checkPath(other);

        if (o.absolute) {
            return o;
        }

        final ArrayList<String> combined = new ArrayList<>(this.nameElements);
        combined.addAll(o.nameElements);

        return new SSHPath(this.fileSystem, this.absolute, combined, 0, combined.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.lang.String)
     */
    @Override
    public Path resolve(String other) {
        return resolve(new SSHPath(this.fileSystem, other));
    }

    /**
     * Check the path is valid
     *
     * @param path - The path to check
     * @return the cast path
     */
    private SSHPath checkPath(Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (!(path instanceof SSHPath)) {
            throw new ProviderMismatchException();
        }
        return (SSHPath) path;
    }

    @Override
    public Path resolveSibling(Path other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path resolveSibling(String other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean startsWith(Path other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean startsWith(String other) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path toAbsolutePath() {
        if (this.absolute) {
            return this;
        }

        return new SSHPath(this.fileSystem, true, this.nameElements, 0, this.nameElements.size());
    }

    @Override
    public File toFile() {
        throw new UnsupportedOperationException("Unable to translate to a java.ioFile");
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public URI toUri() {
        try {
            return new URI(this.fileSystem.provider().getScheme() + ":" + toAbsolutePath().toString());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        if (this.absolute && this.nameElements.isEmpty()) {
            return "/";
        }

        final StringBuilder sb = new StringBuilder();
        boolean prefixSeperator = this.absolute;
        for (final String element : this.nameElements) {
            if (prefixSeperator) {
                sb.append("/");
            }
            sb.append(element);
            prefixSeperator = true;
        }
        return sb.toString();
    }

}
