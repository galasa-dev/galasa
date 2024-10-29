/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

/**
 * Dummy RAS provider to be extended by other RAS services
 *
 *  
 *
 */
public class ResultArchiveStoreFileSystemProvider extends FileSystemProvider {

    public static final String NOT_AVAILABLE_MESSAGE = "Not available in a Dummy Result Archive Store";

    private final FileStore    fileStore;
    protected FileSystem       fileSystem;

    protected ResultArchiveStoreFileSystemProvider(FileStore fileSystemStore) {
        this.fileStore = fileSystemStore;
        this.fileSystem = createFileSystem();
    }

    protected ResultArchiveStoreFileSystemProvider(FileStore fileSystemStore, FileSystem fileSystem) {
        this.fileStore = fileSystemStore;
        this.fileSystem = fileSystem;
    }

    protected ResultArchiveStoreFileSystem createFileSystem() {
        return new ResultArchiveStoreFileSystem(this);
    }

    /**
     * @return The Filestore backing this provider
     */
    public FileStore getActualFileStore() {
        return this.fileStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#getScheme()
     */
    @Override
    public String getScheme() {
        return "rasdummy";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#newFileSystem(java.net.URI,
     * java.util.Map)
     */
    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return this.fileSystem;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#getFileSystem(java.net.URI)
     */
    @Override
    public FileSystem getFileSystem(URI uri) {
        return this.fileSystem;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#getPath(java.net.URI)
     */
    @Override
    public Path getPath(URI uri) {
        return getActualFileSystem().newPathObject(uri.getPath());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#newByteChannel(java.nio.file.Path,
     * java.util.Set, java.nio.file.attribute.FileAttribute[])
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        return new ResultArchiveStoreByteChannel(); // Dummy Channel
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.nio.file.spi.FileSystemProvider#newDirectoryStream(java.nio.file.Path,
     * java.nio.file.DirectoryStream.Filter)
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException(NOT_AVAILABLE_MESSAGE);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#createDirectory(java.nio.file.Path,
     * java.nio.file.attribute.FileAttribute[])
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        // *** Dummy so ignoring
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#delete(java.nio.file.Path)
     */
    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Not allowed to delete artifacts from the Result Archive Store");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#copy(java.nio.file.Path,
     * java.nio.file.Path, java.nio.file.CopyOption[])
     */
    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException(NOT_AVAILABLE_MESSAGE);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#move(java.nio.file.Path,
     * java.nio.file.Path, java.nio.file.CopyOption[])
     */
    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException(NOT_AVAILABLE_MESSAGE);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#isSameFile(java.nio.file.Path,
     * java.nio.file.Path)
     */
    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return (path.compareTo(path2) == 0);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#isHidden(java.nio.file.Path)
     */
    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#getFileStore(java.nio.file.Path)
     */
    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return this.fileStore;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#checkAccess(java.nio.file.Path,
     * java.nio.file.AccessMode[])
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (modes.length == 0) { // Check the file exists
            throw new IOException("File does not exist in Dummy RAS");
        }
        
        for (final AccessMode mode : modes) {
            switch (mode) {
                case EXECUTE:
                    throw new UnsupportedOperationException("Path '" + path.toString() + " is not executable");
                case READ:
                    throw new UnsupportedOperationException(
                            "Path '" + path.toString() + " is not available read in dummy RAS");
                case WRITE:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * java.nio.file.spi.FileSystemProvider#getFileAttributeView(java.nio.file.Path,
     * java.lang.Class, java.nio.file.LinkOption[])
     */
    @SuppressWarnings("unchecked") // NOSONAR
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        if (type == BasicFileAttributeView.class) {
            return (V) new ResultArchiveStoreBasicAttributesView();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#readAttributes(java.nio.file.Path,
     * java.lang.Class, java.nio.file.LinkOption[])
     */
    @SuppressWarnings("unchecked") // NOSONAR
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        return (A) new ResultArchiveStoreBasicAttributes();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#readAttributes(java.nio.file.Path,
     * java.lang.String, java.nio.file.LinkOption[])
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.spi.FileSystemProvider#setAttribute(java.nio.file.Path,
     * java.lang.String, java.lang.Object, java.nio.file.LinkOption[])
     */
    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not available in a Result Archive Store");
    }

    public ResultArchiveStoreFileSystem getActualFileSystem() {
        return (ResultArchiveStoreFileSystem) this.fileSystem;
    }

}