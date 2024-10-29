/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Create a Result Archive Store specific version of the Path
 *
 *  
 *
 */
public class ResultArchiveStorePath implements Path {

    protected final FileSystem   fileSystem;
    protected final List<String> nameElements = new ArrayList<>();
    protected final boolean      absolute;

    /**
     * Create a path from a string
     *
     * @param fileSystem - The filesystem the path will be used on
     * @param path       - The path to convert
     */
    protected ResultArchiveStorePath(@NotNull FileSystem fileSystem, String path) {
        this.fileSystem = fileSystem;

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
    protected ResultArchiveStorePath(FileSystem fileSystem, boolean absolute, List<String> nameElements, int start,
            int end) {
        this.fileSystem = fileSystem;
        this.absolute = absolute;
        for (int i = start; i < end; i++) {
            this.nameElements.add(nameElements.get(i));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getFileSystem()
     */
    @Override
    public FileSystem getFileSystem() {
        return this.fileSystem;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#isAbsolute()
     */
    @Override
    public boolean isAbsolute() {
        return this.absolute;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getRoot()
     */
    @Override
    public Path getRoot() {
        return newPathObject(true, new ArrayList<>(), 0, 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getFileName()
     */
    @Override
    public Path getFileName() {
        if (this.nameElements.isEmpty()) {
            return null;
        }

        return new ResultArchiveStorePath(this.fileSystem, this.nameElements.get(this.nameElements.size() - 1));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getParent()
     */
    @Override
    public Path getParent() {
        if (this.nameElements.isEmpty()) {
            return null;
        }
        return newPathObject(this.absolute, this.nameElements, 0,
                this.nameElements.size() - 1);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getNameCount()
     */
    @Override
    public int getNameCount() {
        return this.nameElements.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#getName(int)
     */
    @Override
    public Path getName(int index) {
        if ((index < 0) || (index >= this.nameElements.size())) {
            return null;
        }
        return newPathObject(this.nameElements.get(index));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#subpath(int, int)
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        if (endIndex <= beginIndex) {
            return null;
        }
        if ((beginIndex < 0) || (beginIndex >= this.nameElements.size())) {
            return null;
        }
        if (endIndex >= this.nameElements.size()) {
            return null;
        }

        boolean newAbolute = this.absolute;
        if (beginIndex > 0) {
            newAbolute = false;
        }
        return newPathObject(newAbolute, this.nameElements, beginIndex, endIndex);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#startsWith(java.nio.file.Path)
     */
    @Override
    public boolean startsWith(Path other) {
        final ResultArchiveStorePath o = checkPath(other);

        if (this.absolute != o.absolute) {
            return false;
        }

        if (o.nameElements.size() > this.nameElements.size()) {
            return false;
        }

        for (int i = 0; i < o.nameElements.size(); i++) {
            if (!o.nameElements.get(i).equals(this.nameElements.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check the path is valid
     *
     * @param path - The path to check
     * @return the cast path
     */
    private ResultArchiveStorePath checkPath(Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (!(path instanceof ResultArchiveStorePath)) {
            throw new ProviderMismatchException();
        }
        return (ResultArchiveStorePath) path;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#startsWith(java.lang.String)
     */
    @Override
    public boolean startsWith(String other) {
        return startsWith(newPathObject(other));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#endsWith(java.nio.file.Path)
     */
    @Override
    public boolean endsWith(Path other) {
        final ResultArchiveStorePath o = checkPath(other);

        if (o.nameElements.size() > this.nameElements.size()) {
            return false;
        }

        if ((o.nameElements.size() == this.nameElements.size()) && (o.absolute != this.absolute)) {
            return false;
        }

        for (int i = o.nameElements.size() - 1, j = this.nameElements.size() - 1; i >= 0; i--, j--) {
            if (!o.nameElements.get(i).equals(this.nameElements.get(j))) {
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#endsWith(java.lang.String)
     */
    @Override
    public boolean endsWith(String other) {
        return endsWith(newPathObject(other));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#normalize()
     */
    @Override
    public Path normalize() {
        // No Normalisation to do as . and .. are not possible
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.nio.file.Path)
     */
    @Override
    public Path resolve(Path other) {
        final ResultArchiveStorePath o = checkPath(other);

        if (o.absolute) {
            return o;
        }

        final ArrayList<String> combined = new ArrayList<>(this.nameElements);
        combined.addAll(o.nameElements);

        return newPathObject(this.absolute, combined, 0, combined.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolve(java.lang.String)
     */
    @Override
    public Path resolve(String other) {
        return resolve(newPathObject(other));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolveSibling(java.nio.file.Path)
     */
    @Override
    public Path resolveSibling(Path other) {
        final ResultArchiveStorePath o = checkPath(other);

        if (o.absolute || this.nameElements.isEmpty()) {
            return o;
        }
        final ArrayList<String> combined = new ArrayList<>(this.nameElements);
        combined.remove(combined.size() - 1);
        combined.addAll(o.nameElements);

        return newPathObject(this.absolute, combined, 0, combined.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#resolveSibling(java.lang.String)
     */
    @Override
    public Path resolveSibling(String other) {
        return resolveSibling(newPathObject(other));
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#relativize(java.nio.file.Path)
     */
    @Override
    public Path relativize(Path other) {
        final ResultArchiveStorePath o = checkPath(other);

        if (o.absolute && !this.absolute) {
            return o;
        }

        if (this.absolute && !o.absolute) {
            return null;
        }

        if (equals(o)) {
            return newPathObject("");
        }

        if (!o.startsWith(this)) {
            return null;
        }

        return newPathObject(false, o.nameElements, this.nameElements.size(),
                o.nameElements.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toUri()
     */
    @Override
    public URI toUri() {
        try {
            return new URI(this.fileSystem.provider().getScheme() + ":" + toAbsolutePath().toString());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
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

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toAbsolutePath()
     */
    @Override
    public Path toAbsolutePath() {
        if (this.absolute) {
            return this;
        }

        return newPathObject(true, this.nameElements, 0, this.nameElements.size());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toRealPath(java.nio.file.LinkOption[])
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#toFile()
     */
    @Override
    public File toFile() {
        throw new UnsupportedOperationException("Unable to translate to a java.ioFile");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#register(java.nio.file.WatchService,
     * java.nio.file.WatchEvent.Kind[], java.nio.file.WatchEvent.Modifier[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("Watching is not supported with this filesystem");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#register(java.nio.file.WatchService,
     * java.nio.file.WatchEvent.Kind[])
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        throw new UnsupportedOperationException("Watching is not supported with this filesystem");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#iterator()
     */
    @Override
    public Iterator<Path> iterator() {
        final ArrayList<Path> it = new ArrayList<>();
        for (final String element : this.nameElements) {
            it.add(newPathObject(element));
        }
        return it.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.Path#compareTo(java.nio.file.Path)
     */
    @Override
    public int compareTo(Path other) {
        if (!(other instanceof ResultArchiveStorePath)) {
            throw new ProviderMismatchException();
        }
        final ResultArchiveStorePath o = (ResultArchiveStorePath) other;

        if ((this.nameElements.isEmpty()) && (o.nameElements.isEmpty())) {
            return 0;
        }

        if (this.nameElements.isEmpty()) {
            return -1;
        }

        if (o.nameElements.isEmpty()) {
            return 1;
        }

        final int maxSize = Math.max(this.nameElements.size(), o.nameElements.size());
        for (int i = 0; i < maxSize; i++) {
            if (i >= this.nameElements.size()) {
                return -1;
            }
            if (i >= o.nameElements.size()) {
                return 1;
            }

            final int c = this.nameElements.get(i).compareTo(o.nameElements.get(i));

            if (c < 0) {
                return -1;
            }

            if (c > 0) {
                return 1;
            }
        }

        return 0;
    }

    public ResultArchiveStorePath unAbsolute() {
        if (!this.absolute) {
            return this;
        }
        return newPathObject(false, this.nameElements, 0, this.nameElements.size());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return toString().equals(o.toString());
    }
    
    protected ResultArchiveStorePath newPathObject(String newPath) {
        return new ResultArchiveStorePath(this.fileSystem, newPath);
    }
    
    protected ResultArchiveStorePath newPathObject(boolean absolute, List<String> nameElements, int start, int end) {
        return new ResultArchiveStorePath(this.fileSystem, absolute, nameElements, start, end);
    }


}