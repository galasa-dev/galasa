/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.SetContentType;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.extensions.common.couchdb.CouchdbException;

public class CouchdbRasFileSystemProvider extends ResultArchiveStoreFileSystemProvider {

    private static final String                                RAS_CONTENT_TYPE = "ras:contentType";
    private static final String                                BASIC_SIZE       = "size";
    private static final String                                POSIX_SIZE       = "posix:size";

    private final HashMap<Path, ResultArchiveStoreContentType> contentTypes     = new HashMap<>();

    private final HashSet<CouchdbArtifactPath>                 paths            = new HashSet<>();

    private final CouchdbRasStore                              couchdbRasStore;

    private final LogFactory logFactory;

    protected CouchdbRasFileSystemProvider(FileStore fileSystemStore, CouchdbRasStore couchdbRasStore, LogFactory logFactory) {
        super(fileSystemStore, null);
        this.logFactory = logFactory;
        fileSystem = new CouchdbFileSystem(this);
        this.couchdbRasStore = couchdbRasStore;
        paths.add(new CouchdbArtifactPath(fileSystem, "/"));
    }

    protected void addPath(CouchdbArtifactPath path) {
        path = path.toAbsolutePath();
        if (paths.contains(path)) {
            return;
        }
        paths.add(path);

        CouchdbArtifactPath parentPath = (CouchdbArtifactPath) path.getParent();
        while (parentPath != null && !paths.contains(parentPath)) {
            if (paths.contains(parentPath)) {
                return;
            }
            paths.add(parentPath);
            parentPath = parentPath.getParent();
        }

    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {

        boolean write = options.contains(StandardOpenOption.WRITE);

        if (write) {
            Path absolute = path.toAbsolutePath();
            ResultArchiveStoreContentType contentType = null;
            for (OpenOption option : options) {
                if (option instanceof SetContentType) {
                    contentType = ((SetContentType) option).getContentType();
                }
            }

            if (contentType != null) {
                for (FileAttribute<?> attr : attrs) {
                    if (attr instanceof ResultArchiveStoreContentType) {
                        contentType = (ResultArchiveStoreContentType) attr;
                        contentTypes.put(absolute, contentType);
                    }
                }
            }
            if (contentType == null) {
                contentType = contentTypes.get(absolute);
            }

            HashSet<OpenOption> passThroughOptions = new HashSet<>();
            passThroughOptions.add(StandardOpenOption.WRITE);
            passThroughOptions.add(StandardOpenOption.TRUNCATE_EXISTING);

            return new CouchdbRasWriteByteChannel(this, this.couchdbRasStore, absolute, contentType, passThroughOptions,
                    attrs, this.logFactory );
        } else {
            CouchdbArtifactPath cdbPath = (CouchdbArtifactPath) path;

            for (CouchdbArtifactPath artifactPath : paths) {
                if (artifactPath.toString().equals(path.toString())) {
                    cdbPath = artifactPath;
                }
            }

            Path cachePath = Files.createTempFile("galasa_couchdb", "temp");
            try {
                couchdbRasStore.retrieveArtifact(cdbPath, cachePath);
            } catch (CouchdbException e) {
                throw new IOException("Unable to retrieve artifact", e);
            }
            return new CouchdbRasReadByteChannel(cachePath);
        }
    }

    public Path getRoot() {
        return new CouchdbArtifactPath(this.getActualFileSystem(), "/");
    }

    @Override
    public Path getPath(URI uri) {
        return new CouchdbArtifactPath(this.getActualFileSystem(), uri.toString());
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return new CouchdbDirectoryStream(dir, filter, paths);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        if (path instanceof CouchdbArtifactPath
                && (type == CoucbDbBasicAttributes.class || type == BasicFileAttributes.class)) {
            CouchdbArtifactPath caPath = (CouchdbArtifactPath) path;
            return (A) caPath.readAttributes();
        }
        return null;
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        final HashMap<String, Object> returnAttrs = new HashMap<>();

        if (!(path instanceof CouchdbArtifactPath)) {
            return returnAttrs;
        }

        CouchdbArtifactPath caPath = (CouchdbArtifactPath) path;

        final ArrayList<String> attrs = new ArrayList<>(Arrays.asList(attributes.replaceAll(" ", "").split(",")));

        // *** We need to add our attributes for * or ras:* or ras:contentType, and also
        // the basic/posix file attributes
        final Iterator<String> it = attrs.iterator();
        while (it.hasNext()) {
            final String attr = it.next();
            if ("*".equals(attr)) {
                returnAttrs.put(RAS_CONTENT_TYPE, caPath.getContentType());
                returnAttrs.put(BASIC_SIZE, caPath.getLength());
                returnAttrs.put(POSIX_SIZE, caPath.getLength());
            } else if ("size".equals(attr)) {
                returnAttrs.put(BASIC_SIZE, caPath.getLength());
            } else {
                final int colon = attr.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                if ("ras".equals(attr.substring(0, colon))) {
                    it.remove();

                    final String attrName = attr.substring(colon + 1);

                    if ("*".equals(attrName)) {
                        returnAttrs.put(RAS_CONTENT_TYPE, caPath.getContentType());
                    } else if ("contentType".equals(attrName)) {
                        returnAttrs.put(RAS_CONTENT_TYPE, caPath.getContentType());
                    } else {
                        throw new UnsupportedOperationException("Attribute ras:" + attrName + " is not available");
                    }
                } else if ("posix".equals(attr.substring(0, colon))) {
                    it.remove();

                    final String attrName = attr.substring(colon + 1);

                    if ("*".equals(attrName)) {
                        returnAttrs.put(POSIX_SIZE, caPath.getLength());
                    } else if ("size".equals(attrName)) {
                        returnAttrs.put(POSIX_SIZE, caPath.getLength());
                    }
                }
            }
        }

        return returnAttrs;
    }
    
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (modes.length == 0) { // Check the file exists
            for(CouchdbArtifactPath p : this.paths) {
                if (p.compareTo(path) == 0) {
                    return;
                }
            }
            throw new IOException("Path " + path.toString() + " is missing");
        }
        
        for (final AccessMode mode : modes) {
            switch (mode) {
                case EXECUTE:
                    throw new UnsupportedOperationException("Path '" + path.toString() + " is not executable");
                case READ:
                case WRITE:
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

}
