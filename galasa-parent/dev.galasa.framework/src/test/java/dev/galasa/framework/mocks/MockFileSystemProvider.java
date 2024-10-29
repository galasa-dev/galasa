/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
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
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockFileSystemProvider extends FileSystemProvider {


    public static class MockDirectoryStream implements DirectoryStream<Path> {

        private MockFileSystem mockFS;
        private Filter<? super Path> filter;
        private String directory;

        public MockDirectoryStream(MockFileSystem mockFS, Filter<? super Path> filter, String directory) {
            this.mockFS = mockFS;
            this.filter = filter;
            this.directory = directory;
        }

        @Override
        public void close() throws IOException {
            // Do nothing
        }

        @Override
        public Iterator<Path> iterator() {
            List<Path> paths;
            try {
                paths = mockFS.getListOfFiles(directory, filter);
            } catch( IOException ex ) {
                // Not expecting the unit tests to have a problem filtering the file list.
                // so just blow up the unit test.
                throw new RuntimeException(ex);
            }

            return paths.iterator();
        }
        
    }

    private MockFileSystem mockFS;

    public MockFileSystemProvider(MockFileSystem mockFS){
        this.mockFS = mockFS ;
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return new MockDirectoryStream(mockFS, filter, dir.toString());
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        if (!mockFS.exists(path)) {
            throw new IOException("File not found! "+path.toString());
        }

        byte[] contents = mockFS.getContentsAsBytes(path);
        return new SeekableByteChannel() {
            private int position = 0;

            @Override
            public int read(ByteBuffer dst) throws IOException {
                int bytesRemaining = contents.length - position;
                int bytesToRead = Math.min(dst.remaining(), bytesRemaining);

                if (bytesToRead <= 0) {
                    return -1;
                }

                dst.put(contents, position, bytesToRead);
                position += bytesToRead;
                return bytesToRead;
            }

            @Override
            public boolean isOpen() {
                throw new UnsupportedOperationException("Unimplemented method 'isOpen'");
            }

            @Override
            public void close() throws IOException {
            }

            @Override
            public int write(ByteBuffer src) throws IOException {
                throw new UnsupportedOperationException("Unimplemented method 'write'");
            }

            @Override
            public long position() throws IOException {
                return position;
            }

            @Override
            public SeekableByteChannel position(long newPosition) throws IOException {
                throw new UnsupportedOperationException("Unimplemented method 'position'");
            }

            @Override
            public long size() throws IOException {
                throw new UnsupportedOperationException("Unimplemented method 'size'");
            }

            @Override
            public SeekableByteChannel truncate(long size) throws IOException {
                throw new UnsupportedOperationException("Unimplemented method 'truncate'");
            }
            
        };
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("Unimplemented method 'getScheme'");
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'newFileSystem'");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        throw new UnsupportedOperationException("Unimplemented method 'getFileSystem'");
    }

    @Override
    public Path getPath(URI uri) {
        throw new UnsupportedOperationException("Unimplemented method 'getPath'");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'createDirectory'");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'copy'");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'isSameFile'");
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'isHidden'");
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'getFileStore'");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'checkAccess'");
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException("Unimplemented method 'getFileAttributeView'");
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'readAttributes'");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'readAttributes'");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Unimplemented method 'setAttribute'");
    }
    
}