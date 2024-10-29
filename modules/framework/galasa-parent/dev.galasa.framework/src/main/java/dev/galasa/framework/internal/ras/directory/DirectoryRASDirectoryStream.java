/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Used with the Directory RAS to produce directory listings
 *
 *  
 *
 */
public class DirectoryRASDirectoryStream implements DirectoryStream<Path> {

    private final ArrayList<Path> directoryList = new ArrayList<>();
    private boolean               closed        = false;

    /**
     * Create a Directory stream.
     *
     * @param fileSystem   - the RAS filesystem
     * @param artifactRoot - The real directory for the artifacts - to relativize
     *                     the virtual paths
     * @param realPath     - the real filesystem path
     * @param filter       - any filters to apply
     * @throws IOException - if there is a problem reading the filesystem
     */
    protected DirectoryRASDirectoryStream(FileSystem fileSystem, Path artifactRoot, Path realPath,
            Filter<? super Path> filter) throws IOException {

        // *** Load the entire list, which is against the stream idea, but we shouldn't
        // *** be using that much heap
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(realPath, filter)) {
            for (final Path child : ds) {
                final Path newPath = fileSystem.getPath(artifactRoot.relativize(child).toString()).toAbsolutePath();
                this.directoryList.add(newPath);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.DirectoryStream#iterator()
     */
    @Override
    public Iterator<Path> iterator() {
        if (this.closed) {
            throw new ClosedDirectoryStreamException();
        }

        return this.directoryList.iterator();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        this.closed = true;
    }

}