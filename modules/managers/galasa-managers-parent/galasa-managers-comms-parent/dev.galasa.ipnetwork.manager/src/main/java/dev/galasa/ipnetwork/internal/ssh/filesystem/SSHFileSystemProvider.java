/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import dev.galasa.ipnetwork.SSHException;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * SSH FileStore for Galasa
 * 
 *  
 * 
 */
public class SSHFileSystemProvider extends FileSystemProvider {

    private final SSHFileSystem fileSystem;

    public SSHFileSystemProvider(SSHFileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (modes == null || modes.length == 0) {
            modes = new AccessMode[] { AccessMode.READ };
        }

        for (AccessMode mode : modes) {
            switch (mode) {
                case READ:
                    ChannelSftp channel = null;
                    try {
                        channel = fileSystem.getFileChannel();
                        channel.lstat(path.toAbsolutePath().toString());
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            throw new NoSuchFileException(path.toAbsolutePath().toString());
                        }
                        throw new IOException("Unable to check for read via SFTP", e);
                    } catch (Exception e) {
                        throw new IOException("Unable to check for read via SFTP", e);
                    } finally {
                        if (channel != null) {
                            channel.disconnect();
                        }
                    }
                    break;
                case WRITE:
                    break;
                case EXECUTE:
                    throw new IOException("Unable to execute files via SFTP");
                default:
                    throw new IOException("Unrecognised accessmode " + mode);
            }
        }

    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = fileSystem.getFileChannel();
            channel.mkdir(dir.toAbsolutePath().toString());
        } catch (Exception e) {
            throw new IOException("Unable to create directory via SFTP", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = fileSystem.getFileChannel();

            SftpATTRS attrs = channel.lstat(path.toAbsolutePath().toString());
            if (attrs.isReg()) {
                channel.rm(path.toAbsolutePath().toString());
            } else {
                @SuppressWarnings("unchecked")
                Vector<LsEntry> v = channel.ls(path.toAbsolutePath().toString());

                Iterator<LsEntry> i = v.iterator();
                while (i.hasNext()) {
                    LsEntry e = i.next();
                    if (e.getFilename().equals(".") || e.getFilename().equals("..")) {
                        i.remove();
                    }
                }

                if (!v.isEmpty()) {
                    throw new DirectoryNotEmptyException(path.toAbsolutePath().toString());
                }

                channel.rmdir(path.toAbsolutePath().toString());
            }

        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                throw new NoSuchFileException(path.toAbsolutePath().toString());
            }
            throw new IOException("Unable to delete via SFTP for path " + path, e);
        } catch (Exception e) {
            throw new IOException("Unable to delete via SFTP for path " + path, e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public Path getPath(URI uri) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public String getScheme() {
        return "galasassh";
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        ChannelSftp channel = null;
        try {
            channel = fileSystem.getFileChannel();
            String filecontent = new String(Files.readAllBytes(source), Charset.defaultCharset());
            InputStream sourceStream = new ByteArrayInputStream(filecontent.getBytes());
            channel.put(sourceStream, target.toAbsolutePath().toString());
            channel.rm(source.toString());
        } catch (Exception e) {
            throw new IOException("Unable to move file via SFTP", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
            throws IOException {

        try {
            return new SSHByteChannel(path, options, this.fileSystem);
        } catch (SSHException e) {
            throw new IOException("Unable to create byte channel for " + path, e);
        }
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
        return new SSHDirectoryStream(dir, fileSystem, filter);
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        return this.fileSystem;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
            throws IOException {
        return (A) new SSHBasicAttributes(fileSystem);
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        // TODO Auto-generated method stub

    }

    public SSHFileSystem getActualFileSystem() {
        return this.fileSystem;
    }

}
