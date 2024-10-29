/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ICredentialsUsernameToken;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.SSHAuthFailException;
import dev.galasa.ipnetwork.SSHException;

/**
 * SSH client for Galasa
 * 
 *  
 * 
 */
public class SSHClient implements ICommandShell {

    private final Log           logger        = LogFactory.getLog(SSHClient.class);

    private KeepAliveThread     keepAliveThread;

    private final long          defaultTimeout;

    private final String        hostname;
    private final int           port;
    private final String        userid;
    private final String        password;

    private JSch                sshClient;
    private Session             session;
    private Channel             channel       = null;

    private long                lastCommandTimestamp;

    private boolean             logShellResults;
    
    private boolean             removeAnsiEscapeCodes = false;

    private final static String specialPrompt = "[GalasaPrompt]";

    // Default value: Linux command
    private String changePromptCommand = "PS1=" + specialPrompt;

    public SSHClient(String hostname, int port, ICredentials credentials, long defaultTimeoutInMillis) throws SSHException {

        this.hostname = hostname;
        this.port = port;
        this.defaultTimeout = defaultTimeoutInMillis;

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

    /**
     * Issue a command using SSH. Equivalent to  {@link #issueCommand(String, boolean, long)}
     * 
     * @param command - command to issue
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    @Override
    public String issueCommand(String command) throws SSHException {

        return issueCommand(command, false, defaultTimeout);
    }

    /**
     * Issue a command using SSH. Equivalent to {@link #issueCommand(String, boolean,
     * long)}
     * 
     * @param command - command to issue
     * @param timeoutInMillis - time (in milliseconds) to wait with no new output appearing
     *                before timing out
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    @Override
    public String issueCommand(String command, long timeoutInMillis) throws SSHException {
        return issueCommand(command, false, timeoutInMillis);
    }

    /**
     * Issue a command using SSH. Equivalent to
     * {@link #issueCommand(String, boolean, long)}
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    @Override
    public String issueCommand(String command, boolean newShell) throws SSHException {
        return issueCommand(command, newShell, defaultTimeout);
    }

    /**
     * Issue a command using SSH
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @param timeoutInMillis  - time (in milliseconds) to wait with no new output appearing
     *                 before timing out
     * @return the output of the command (stdout and stderr)
     * @throws SSHException
     */
    @Override
    public synchronized String issueCommand(String command, boolean newShell, long timeoutInMillis) throws SSHException {

        // Connect if we are not already connected
        connect();
        try {
            synchronized (sshClient) {
                // SessionChannelClient session = null;
                // if (session == null || session.)
                try {

                    logger.trace("Issuing '" + command + "'");
                    lastCommandTimestamp = System.currentTimeMillis();

                    // Issue the desired command and retrieve the response to a
                    // string
                    String response = retrieveOutput(command, timeoutInMillis);

                    if (logShellResults) {
                        logger.trace("Received '" + response);
                    }
                    lastCommandTimestamp = System.currentTimeMillis();

                    return response;
                } catch (SSHException e) {
                    throw e;
                } catch (IOException e) {
                    throw new SSHException("Error whilst issuing command to ssh '" + command + "'", e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SSHException("Interrupted while trying to retrieve output", e);
                } catch (ExecutionException e) {
                    throw new SSHException("Execution error while trying to retrieve output", e);
                }
            }
        } finally {
            // disconnect();
        }
    }

    /**
     * Define the right command used to change the shell prompt
     */
    @Override
    public void setChangePromptCommand(String command) {
        changePromptCommand = command + specialPrompt;
    }

    /**
     * Issue a command using SSH shell. Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)}
     * 
     * @param command - command to issue
     * @return the output of the command
     * @throws SSHException
     */
    @Override
    public String issueCommandToShell(String command) throws SSHException {

        return issueCommandToShell(command, false, defaultTimeout);
    }

    /**
     * Issue a command using SSH shell. Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)}
     * 
     * @param command - command to issue
     * @param timeoutInMillis - time (in milliseconds) to wait with no new output appearing
     *                before timing out
     * @return the output of the command
     * @throws SSHException
     */
    @Override
    public String issueCommandToShell(String command, long timeoutInMillis) throws SSHException {
        return issueCommandToShell(command, false, timeoutInMillis);
    }

    /**
     * Issue a command using SSH shell. Equivalent to
     * {@link #issueCommandToShell(String, boolean, long)}
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @return the output of the command
     * @throws SSHException
     */
    @Override
    public String issueCommandToShell(String command, boolean newShell) throws SSHException {
        return issueCommandToShell(command, newShell, defaultTimeout);
    }

    /**
     * Issue a command using SSH shell
     * 
     * @param command  - command to issue
     * @param newShell - if true will start a new
     * @param timeoutInMillis  - time (in milliseconds) to wait with no new output appearing
     *                 before timing out
     * @return the output of the command
     * @throws SSHException
     */
    @Override
    public synchronized String issueCommandToShell(String command, boolean newShell, long timeoutInMillis) throws SSHException {

        connect();

        try {

            if (channel == null || channel.isClosed() || newShell) {
                if (channel != null && !channel.isClosed()) {
                    logger.trace("Closing old shell session");
                    channel.disconnect();
                }
                logger.trace("Opening new shell session to ssh");
                channel = session.openChannel("shell");
                ((ChannelShell) channel).setPty(true);
                ((ChannelShell) channel).setPtyType("ansi", 2048, 24, 0, 0);
                channel.connect();
                Thread.sleep(5000); // NOSONAR - Sleep is sufficent
            }

            lastCommandTimestamp = System.currentTimeMillis();
            // Set a special prompt so we can easily identify responses to our commands
            logger.trace("Setting special prompt '" + specialPrompt + "'");
            retrieveOutputFromShell(channel, changePromptCommand, timeoutInMillis);
            Thread.sleep(500); // NOSONAR - Sleep is sufficent

            // Issue the desired command and retrieve the response to a string
            lastCommandTimestamp = System.currentTimeMillis();
            String response = retrieveOutputFromShell(channel, command, timeoutInMillis);
            lastCommandTimestamp = System.currentTimeMillis();
            
            return response;

        } catch (IOException e) {
            throw new SSHException("Error whilst issuing command to ssh '" + command + "'", e);
        } catch (JSchException e) {
            throw new SSHException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SSHException("Interrupted while trying to retrieve output", e);
        } catch (ExecutionException e) {
            throw new SSHException("Execution error while trying to retrieve output", e);
        } finally {
            // disconnect();
        }
    }

    @Override
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

                this.keepAliveThread = new KeepAliveThread(session);
                this.keepAliveThread.start();

            } catch (Exception e) {
                if ("Auth fail".equals(e.getMessage())) {
                    throw new SSHAuthFailException(e);
                }

                if (retry > 0) {
                    logger.trace("Exception caught during SSH connection, will retry.", e);
                    if (session != null && session.isConnected()) {
                        session.disconnect();
                        session = null;
                    }
                    Thread.sleep(5000); // NOSONAR - Sleep is sufficent
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

    @Override
    public void restartShell() throws SSHException {
    }

    /**
     * Disconnect the client
     * 
     * @throws IOException
     */
    @Override
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

    /**
     * Retrieve all output from the shell, returning only that which is found
     * between the command issued and the next occurrence of the special prompt we
     * defined in {@link #issueCommand(String)}
     *
     * @param command
     * @param timeoutInMillis
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws SSHException
     */
    private String retrieveOutput(String command, long timeoutInMillis)
            throws IOException, InterruptedException, ExecutionException, SSHException {

        StringBuilder sb = new StringBuilder();
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setPty(true);
            channel.setPtyType("ansi", 2048, 24, 0, 0);
            channel.setInputStream(null);
            channel.setErrStream(null);
            channel.setCommand(command);
            InputStream is = channel.getInputStream();
            InputStream err = channel.getErrStream();
            channel.connect();

            long whenTimeout = Calendar.getInstance().getTimeInMillis() + timeoutInMillis;

            byte[] tmp = new byte[1024];
            while (true) {
                if (whenTimeout <= Calendar.getInstance().getTimeInMillis()) {
                    throw new SSHException("Read of command timed out, response so far:-\n" + sb.toString());
                }
                while (is.available() > 0) {
                    int i = is.read(tmp);

                    if (i < 0) {
                        break;
                    }
                    String data = new String(tmp, 0, i);
                    sb.append(data);
                }
                while (err.available() > 0) {
                    int i = err.read(tmp);

                    if (i < 0) {
                        break;
                    }
                    String data = new String(tmp, 0, i);
                    sb.append(data);
                }
                if (channel.isClosed()) {
                    if (is.available() > 0)
                        continue;
                    if (err.available() > 0)
                        continue;
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (Exception ee) {
                }
            }
        } catch (SSHException e) {
            throw e;
        } catch (Exception e) {
            throw new SSHException("Error reading exec output", e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        
        if (this.removeAnsiEscapeCodes) {
            return new String(removeAnsiEscapeCodes(sb.toString().getBytes()));
        }

        return sb.toString();

    }

    private byte[] removeAnsiEscapeCodes(byte[] bytes) throws IOException {
        return AnsiEscapeSequences.stripAnsiEscapeSequences(bytes);
    }

    /**
     * Retrieve all output from the shell, returning only that which is found
     * between the command issued and the next occurrence of the special prompt we
     * defined in {@link #issueCommandToShell(String)}
     *
     * @param channel
     * @param command
     * @param timeoutInMillis
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws SSHException
     */
    private String retrieveOutputFromShell(Channel channel, String command, long timeoutInMillis)
            throws IOException, InterruptedException, ExecutionException, SSHException {

        // Get the input stream from the current session
        final InputStream in = channel.getInputStream();
        OutputStream os = channel.getOutputStream();
        in.skip(in.available()); // NOSONAR - Dont care what is on the buffer to start with

        // Remove any unwanted trailing end-of-line characters
        command = command.trim();
        String patternCommand = command.replaceAll(".*[\\r\\n]", "");

        // Construct the pattern which will match the output we are actually interested
        // in
        // i.e. everything between our command and the next prompt
        Pattern responsePattern = Pattern
                .compile("\\Q" + patternCommand + "\\E[\\r\\n]*(.*)\\Q" + specialPrompt + "\\E", Pattern.DOTALL);

        // Submit the command
        logger.trace("Submitting command to host '" + hostname + "':\n'" + command + "'");
        os.write((command + " \r\n").getBytes());
        os.flush();
        Thread.sleep(500);

        // Create a string builder to build the response, and a buffer in to which to
        // read
        // from the input stream
        StringBuilder responseBuilder = new StringBuilder();
        final byte buffer[] = new byte[5000];

        // Create an executor and a callable which will allow us to read continuously
        // from
        // the input stream with a timeoutInMillis
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Integer> reader = new Callable<Integer>() {

            /**
             * Return the number of bytes ready to be read from the buffer
             */
            @Override
            public Integer call() throws Exception {
                return in.read(buffer);
            }
        };

        // Retrieve the output
        while (true) {

            // Submit our callable and wait for it to tell us how many bytes to read
            Future<Integer> future = executor.submit(reader);
            int read = 0;
            try {
                read = future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                executor.shutdown();
                throw new SSHException("Timed out waiting for response from ssh. Response so far: " + responseBuilder);
            }

            // Append the new output to our response
            responseBuilder.append(new String(buffer, 0, read));

            // If the output matches <command>...<prompt> then we have found the complete
            // response
            Matcher responseMatcher = responsePattern.matcher(responseBuilder.toString());
            if (responseMatcher.find()) {
                executor.shutdown();
                String response = responseMatcher.group(1);
                logger.trace("Retrieved response from host '" + hostname + "':\n'" + response + "'");
                return response;
            }
        }
    }

    private class KeepAliveThread extends Thread {

        private final Session monitorSession;

        // Some commands we run download the isolated build zip which takes ages...
        // Timeout increased from 60secs to 120secs to allow the download to complete.
        private long          idleTimeout = 120000;

        public KeepAliveThread(Session session) {
            this.monitorSession = session;
            this.setDaemon(true);
            this.setName("GalasaSSHClient timeout thread");

            // if (configurationProperties != null) {
            // String idleTimeoutString =
            // configurationProperties.getProperty("core.ssh.idle.timeout", "60000");
            // try {
            // idleTimeout = Long.parseLong(idleTimeoutString);
            // } catch(Exception e) {
            // logger.error("Invalid ssh idle timeout '" + idleTimeoutString + "'",e);
            // }
            //
            // }
        }

        @Override
        public void run() {

            lastCommandTimestamp = System.currentTimeMillis();

            while (this.monitorSession.isConnected()) {

                synchronized (SSHClient.this) {

                    long timeout = System.currentTimeMillis() - idleTimeout;
                    if (timeout >= lastCommandTimestamp) {
                        logger.debug("SSH Client unused after " + idleTimeout + " milliseconds, freeing session");
                        this.monitorSession.disconnect();
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    return;
                }
            }
        }

    }

    @Override
    public void reportResultStrings(boolean report) {
        this.logShellResults = report;
    }

    @Override
    public void setRemoveAnsiEscapeCodes(boolean remoteAnsiEscapeCodes) {
        this.removeAnsiEscapeCodes = remoteAnsiEscapeCodes;
    }

}
