/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Create a Result Archive Store specific File System
 *
 *  
 *
 */
public class ResultArchiveStoreFileSystem extends FileSystem {

    private final ResultArchiveStoreFileSystemProvider fileSystemProvider;
    private boolean                                    readOnly = false;

    /**
     * Create a RAS File System
     *
     * @param fileSystemProvider - The provider to use with it, local or etcd3 etc
     *
     */
    public ResultArchiveStoreFileSystem(ResultArchiveStoreFileSystemProvider fileSystemProvider) {
        this.fileSystemProvider = fileSystemProvider;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#provider()
     */
    @Override
    public ResultArchiveStoreFileSystemProvider provider() {
        return this.fileSystemProvider;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#close()
     */
    @Override
    public void close() throws IOException {
        // Nothing to close
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#isOpen()
     */
    @Override
    public boolean isOpen() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getSeparator()
     */
    @Override
    public String getSeparator() {
        return "/";
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getRootDirectories()
     */
    @Override
    public Iterable<Path> getRootDirectories() {
        final ArrayList<Path> roots = new ArrayList<>();
        roots.add(newPathObject("/"));
        return roots;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getFileStores()
     */
    @Override
    public Iterable<FileStore> getFileStores() {
        final ArrayList<FileStore> fileStores = new ArrayList<>();
        fileStores.add(this.fileSystemProvider.getActualFileStore());
        return fileStores;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#supportedFileAttributeViews()
     */
    @Override
    public Set<String> supportedFileAttributeViews() {
        final HashSet<String> set = new HashSet<>();
        set.add("basic");
        set.add("ras");
        return set;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getPath(java.lang.String, java.lang.String[])
     */
    @Override
    public Path getPath(String first, String... more) {
        final StringBuilder sb = new StringBuilder();
        if (first != null) {
            sb.append(first);
        }
        for (final String m : more) {
            if (m != null) {
                if (sb.length() > 0) {
                    sb.append("/");
                }
                sb.append(m);
            }
        }

        return newPathObject(sb.toString());
    }
    
    protected ResultArchiveStorePath newPathObject(String path) {
        return new ResultArchiveStorePath(this, path);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getPathMatcher(java.lang.String)
     */
    @Override
    public PathMatcher getPathMatcher(String syntaxAndInput) {
        final int pos = syntaxAndInput.indexOf(':');
        if ((pos <= 0) || (pos >= (syntaxAndInput.length() - 1))) {
            throw new IllegalArgumentException();
        }
        final String syntax = syntaxAndInput.substring(0, pos);
        final String input = syntaxAndInput.substring(pos + 1);
        String expr;
        if ("glob".equals(syntax)) {
            expr = createRegexFromGlob(input);
        } else {
            if ("regex".equals(syntax)) {
                expr = input;
            } else {
                throw new UnsupportedOperationException("Syntax '" + syntax + "' not recognized");
            }
        }
        // return matcher
        final Pattern pattern = Pattern.compile(expr);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }

            @Override
            public String toString() {
                return pattern.toString();
            }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#getUserPrincipalLookupService()
     */
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.nio.file.FileSystem#newWatchService()
     */
    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("WatchService is unavailable on RAS File Systems");
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Convert a glob into a regex
     *
     * @param glob - the glob to convert
     * @return - the resulting regex
     */
    public static String createRegexFromGlob(String glob) {
        final StringBuilder sb = new StringBuilder();

        sb.append("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append('.');
                    break;
                case '.':
                    sb.append("\\.");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('$');
        return sb.toString();
    }

}