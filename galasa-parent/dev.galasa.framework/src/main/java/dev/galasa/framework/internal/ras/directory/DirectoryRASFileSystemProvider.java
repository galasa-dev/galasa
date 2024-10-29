/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystem;
import dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider;
import dev.galasa.framework.spi.ras.ResultArchiveStorePath;
import dev.galasa.ResultArchiveStoreContentType;
import dev.galasa.ResultArchiveStoreFileAttributeView;
import dev.galasa.SetContentType;

/**
 * The Directory RAS Provider for stored artifacts, that does most of the work
 * on the local filesystem
 *
 *  
 *
 */
public class DirectoryRASFileSystemProvider extends ResultArchiveStoreFileSystemProvider {

    private static final String RAS_CONTENT_TYPE      = "ras:contentType";

    private final Path          artifactDirectory;
    private final Path          artifactPropertesFile;
    private final Properties    contentTypeProperties = new Properties();

    /**
     * Create the Directory RAS provider for stored artifacts
     *
     * @param runDirectory - The real directory to store the stored artifacts
     * @throws IOException - if there is an error during setup
     */
    protected DirectoryRASFileSystemProvider(Path runDirectory) throws IOException {
        super(runDirectory.getFileSystem().provider().getFileStore(runDirectory));

        this.artifactDirectory = runDirectory.resolve("artifacts");
        this.artifactPropertesFile = runDirectory.resolve("artifacts.properties");

        // *** Load the content type properties file, contains the content type of all
        // the files if set
        if (Files.exists(this.artifactPropertesFile)) {
            try (InputStream is = Files.newInputStream(this.artifactPropertesFile)) {
                this.contentTypeProperties.load(is);
            } catch (final Exception e) {
                throw new IOException("Unable to read the artifacts contenttypes", e);
            }
        }
    }

    /**
     * Set the content type of the virtual path
     *
     * @param path        - the stored artifact to set the content type for
     * @param contentType - the content type
     * @throws IOException - if unable to save the properties file
     */
    private void setContentType(Path path, ResultArchiveStoreContentType contentType) throws IOException {
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }

        this.contentTypeProperties.setProperty(path.toString(), contentType.value());

        // *** Make hardcopy
        try (OutputStream os = Files.newOutputStream(this.artifactPropertesFile)) {
            this.contentTypeProperties.store(os, null);
        } catch (final Exception e) {
            throw new IOException("Unable to write the artifacts contenttypes", e);
        }
    }

    /**
     * Retrieve the content type for stored artifact virtual path
     *
     * @param path - stored artifact virtual path
     * @return - the content path
     */
    private ResultArchiveStoreContentType getContentType(Path path) {
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        ResultArchiveStoreContentType contentType = null;
        final String sContentType = this.contentTypeProperties.getProperty(path.toString());
        if (sContentType == null) {
            contentType = ResultArchiveStoreContentType.TEXT;
        } else {
            contentType = new ResultArchiveStoreContentType(sContentType);
        }
        return contentType;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * createFileSystem()
     */
    @Override
    protected ResultArchiveStoreFileSystem createFileSystem() {
        return new DirectoryRASFileSystem(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#getScheme()
     */
    @Override
    public String getScheme() {
        return "rasdir";
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * newByteChannel (java.nio.file.Path, java.util.Set,
     * java.nio.file.attribute.FileAttribute[])
     */
    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {
        Path realPath = getRealPath(path);
        if (!Files.exists(realPath.getParent())) {
            Files.createDirectories(realPath.getParent());
        }

        // *** Remove any SetContentType open options
        HashSet<OpenOption> newOptions = new HashSet<>();
        for (OpenOption option : options) {
            if (option instanceof SetContentType) {
                setContentType(path, ((SetContentType) option).getContentType());
            } else {
                newOptions.add(option);
            }
        }

        // *** Get a nice byte channel
        final SeekableByteChannel byteChannel = Files.newByteChannel(realPath, newOptions); // NOSONAR

        // *** If we have a RAS attribute, contenttype, set it
        for (final FileAttribute<?> attr : attrs) {
            if (attr instanceof ResultArchiveStoreContentType) {
                setContentType(path, (ResultArchiveStoreContentType) attr);
            }
        }

        return byteChannel;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * checkAccess( java.nio.file.Path, java.nio.file.AccessMode[])
     */
    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        boolean write = false;
        for (final AccessMode mode : modes) {
            switch (mode) {
                case EXECUTE:
                    throw new IOException("Path '" + path.toString() + " is not available execute");
                case READ:
                    break;
                case WRITE:
                    write = true;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        if (write) {
            if (this.fileSystem.isReadOnly()) {
                throw new AccessDeniedException(path.toAbsolutePath().toString());
            }
            return;
        }

        final Path realPath = getRealPath(path);
        if (!Files.exists(realPath)) {
            throw new NoSuchFileException(path.toAbsolutePath().toString());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * createDirectory(java.nio.file.Path, java.nio.file.attribute.FileAttribute[])
     */
    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        final Path realPath = getRealPath(dir);

        if (Files.exists(dir)) {
            throw new FileAlreadyExistsException(dir.toAbsolutePath().toString());
        }

        Files.createDirectory(realPath);
    }

    /**
     * Convert the Stored Artifact virtual path to a real filesystem path
     *
     * @param path - the virtual path
     * @return - the real path
     */
    private Path getRealPath(Path path) {
        if (!(path instanceof ResultArchiveStorePath)) {
            throw new ProviderMismatchException();
        }

        ResultArchiveStorePath rasPath = (ResultArchiveStorePath) path;

        if (rasPath.isAbsolute()) {
            rasPath = rasPath.unAbsolute();
        }

        return this.artifactDirectory.resolve(rasPath.toString());
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * getFileAttributeView(java.nio.file.Path, java.lang.Class,
     * java.nio.file.LinkOption[])
     */
    @SuppressWarnings("unchecked") // NOSONAR
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        final Path realPath = getRealPath(path);

        if (type == BasicFileAttributeView.class) {
            return realPath.getFileSystem().provider().getFileAttributeView(realPath, type, options);
        }
        if (type == ResultArchiveStoreFileAttributeView.class) {
            return (V) new ResultArchiveStoreFileAttributeView(getContentType(path));
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * readAttributes (java.nio.file.Path, java.lang.Class,
     * java.nio.file.LinkOption[])
     */
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        final Path realPath = getRealPath(path);

        if (type == BasicFileAttributes.class) {
            return realPath.getFileSystem().provider().readAttributes(realPath, type, options);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * readAttributes (java.nio.file.Path, java.lang.String,
     * java.nio.file.LinkOption[])
     */
    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        final HashMap<String, Object> returnAttrs = new HashMap<>();

        final ResultArchiveStoreContentType contentType = getContentType(path);

        final ArrayList<String> attrs = new ArrayList<>(Arrays.asList(attributes.replaceAll(" ", "").split(","))); // NOSONAR

        // *** We need to add our attributes for * or ras:* or ras:contentType, then
        // pass off to the basic file attributes
        final Iterator<String> it = attrs.iterator();
        while (it.hasNext()) {
            final String attr = it.next();
            if ("*".equals(attr)) {
                returnAttrs.put(RAS_CONTENT_TYPE, contentType.value());
            } else {
                final int colon = attr.indexOf(':');
                if ((colon == 3) && "ras".equals(attr.substring(0, colon))) {
                    it.remove();

                    final String attrName = attr.substring(colon + 1);

                    if ("*".equals(attrName)) {
                        returnAttrs.put(RAS_CONTENT_TYPE, contentType.value());
                    } else if ("contentType".equals(attrName)) {
                        returnAttrs.put(RAS_CONTENT_TYPE, contentType.value());
                    } else {
                        throw new UnsupportedOperationException("Attribute ras:" + attrName + " is not available");
                    }
                }
            }
        }

        // *** If we still have attributes, rebuild the string so we can pass them on
        if (!attrs.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final String attr : attrs) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(attr);
            }

            // *** Pass them on to the real path
            final Path realPath = getRealPath(path);
            returnAttrs.putAll(realPath.getFileSystem().provider().readAttributes(realPath, sb.toString(), options));
        }

        return returnAttrs;
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * setAttribute( java.nio.file.Path, java.lang.String, java.lang.Object,
     * java.nio.file.LinkOption[])
     */
    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Setting attributes in unsupported in Result Archive Store");
    }

    /*
     * (non-Javadoc)
     *
     * @see dev.galasa.framework.spi.ras.ResultArchiveStoreFileSystemProvider#
     * newDirectoryStream(java.nio.file.Path, java.nio.file.DirectoryStream.Filter)
     */
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return new DirectoryRASDirectoryStream(this.fileSystem, this.artifactDirectory, getRealPath(dir), filter);
    }

}