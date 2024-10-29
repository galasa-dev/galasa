/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal.ssh.filesystem;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.ipnetwork.SSHException;

public class SSHFileSystem extends FileSystem {

    private final Log                   logger = LogFactory.getLog(SSHFileSystemProvider.class);

    private final String                hostname;
    private final int                   port;
    private final String                userid;
    private final String                password;

    private JSch                        sshClient;
    private Session                     session;

    private final SSHFileStore          fileStore;
    private final SSHFileSystemProvider fileSystemProvider;

    public SSHFileSystem(String hostname, int port, ICredentials credentials) throws SSHException {

        this.hostname = hostname;
        this.port = port;

        this.fileStore = new SSHFileStore("sshfilestore-" + hostname);
        this.fileSystemProvider = new SSHFileSystemProvider(this);

        this.sshClient = new JSch();
        this.session = null;

        try {
            if (credentials instanceof ICredentialsUsernamePassword) {
                ICredentialsUsernamePassword creds = (ICredentialsUsernamePassword) credentials;
                this.userid = creds.getUsername();
                this.password = creds.getPassword();
            } else if (credentials instanceof ICredentialsUsernameToken) {
                ICredentialsUsernameToken creds = (ICredentialsUsernameToken) credentials;
                this.userid = creds.getUsername();
                this.password = null;
                this.sshClient.addIdentity(this.userid, creds.getToken(), null, null);
            } else {
                throw new SSHException("Unsupported credentials type - " + credentials.getClass().getName());
            }
        } catch (SSHException e) {
            throw e;
        } catch (JSchException e) {
            throw new SSHException("Problem adding credentials to SSH", e);
        }

    }

    public void connect() throws SSHException {
        connect(5);
    }

    /**
     * Connect to the target system
     * 
     * @throws SSHException
     */
    private synchronized void connect(int retry) throws SSHException {
        // Do nothing if already connected
        if (session != null && session.isConnected()) {
            return;
        }

        try {

            try {
                session = sshClient.getSession(this.userid, hostname, port);
                session.setIdentityRepository(sshClient.getIdentityRepository());
                if (this.password != null) {
                    session.setPassword(this.password);
                }
                session.setConfig("StrictHostKeyChecking", "no");

                session.connect();

                // Slight delay to allow the connection to stabilise
                try {
                    Thread.sleep(200); // NOSONAR - Sleep is sufficent
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SSHException("Interrupted trying to authenticate using SSH", e);
                }

                logger.trace("SSH Client connected to '" + hostname + ":" + port);

            } catch (Exception e) {
                if (retry > 0) {
                    logger.trace("Exception caught during SSH connection, will retry.", e);
                    if (session != null && session.isConnected()) {
                        session.disconnect();
                        session = null;
                    }
                    Thread.sleep(5000); // NOSONAR - Sleep is sufficient
                    connect(retry - 1);
                } else {
                    throw e;
                }
            }
        } catch (SSHException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SSHException("Interrupted while trying to retrieve output", e);
        } catch (Exception e) {
            throw new SSHException("Unrecognised exception in connection", e);
        }

        return;
    }

    protected ChannelSftp getFileChannel() throws SSHException {
        connect();

        try {
            Channel channel = session.openChannel("sftp");
            channel.connect();

            return (ChannelSftp) channel;
        } catch (Exception e) {
            throw new SSHException("Unable to open a sftp channel to the server", e);
        }
    }

    /**
     * Disconnect the client
     * 
     * @throws IOException
     */
    public synchronized void disconnect() throws SSHException {
        if (session == null) {
            return;
        }
        synchronized (sshClient) {

            if (!session.isConnected()) {
                session = null;
                return;
            }

            session.disconnect();
            logger.trace("SSH Client disconnected");

            session = null;
        }
        return;
    }

    @Override
    public void close() throws IOException {
        try {
            this.disconnect();
        } catch (SSHException e) {
            throw new IOException("Problem disconnecting the SSH FileSystem");
        }
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        final ArrayList<FileStore> fileStores = new ArrayList<>();
        fileStores.add(this.fileStore);
        return fileStores;
    }

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

        return new SSHPath(this, sb.toString());
    }

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

    @Override
    public Iterable<Path> getRootDirectories() {
        final ArrayList<Path> roots = new ArrayList<>();
        roots.add(new SSHPath(this, "/"));
        return roots;
    }

    @Override
    public String getSeparator() {
        return "/";
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Watch Service is not available with this FileSystem");
    }

    @Override
    public FileSystemProvider provider() {
        return this.fileSystemProvider;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("need to write");
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
