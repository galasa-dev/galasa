/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import dev.galasa.common.SSLTLSContextNameSelector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.spi.NetworkException;

public class Network {

    private final Log           logger          = LogFactory.getLog(getClass());

    private final String        host;
    private final int           port;
    private final boolean       ssl;
    private final String        terminalId;
    private boolean             switchedSSL     = false;
    private boolean             doStartTls      = true;    

    private Socket              socket;
    private OutputStream        outputStream;
    private InputStream         inputStream;

    private KeepAlive           keepAlive;
    private Instant             lastSend        = Instant.now();

    private Exception           errorException;

    private boolean             basicTelnet = false;

    private SSLTLSContextNameSelector nameSelector = new SSLTLSContextNameSelector();

    public Network(String host, int port, String terminalId) {
        this(host, port, false, terminalId);
    }

    public Network(String host, int port, boolean ssl, String terminalId) {
        this.host       = host;
        this.port       = port;
        this.ssl        = ssl;
        this.terminalId = terminalId;
    }

    public boolean connectClient() throws NetworkException {
        logger.trace("connectClient() entered");
        if (socket != null) {
            if (socket.isConnected()) {
                logger.trace("connectClient() exiting, socket is already connected to the server");
                return true;
            }

            close();
        }

        Socket newSocket = null;
        try {
            newSocket = createSocket();
            newSocket.setTcpNoDelay(true);
            newSocket.setKeepAlive(true);

            this.socket = newSocket;
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            newSocket = null;

            this.keepAlive = new KeepAlive();
            this.keepAlive.start();

            logger.trace("connectClient() exiting, client connected OK");
            return true;
        } catch (Exception e) {
            throw new NetworkException("Unable to connect to Telnet server", e);
        } finally {
            if (newSocket != null) {
                try {
                    logger.trace("Closing newSocket");
                    newSocket.close();
                    logger.trace("newSocket closed OK");
                } catch (IOException e) {
                    logger.error("Failed to close the socket", e);
                }
            }
            logger.trace("connectClient() exiting");
        }
    }
    
    public void setDoStartTls(boolean doStartTls) {
        this.doStartTls = doStartTls;
    }
    
    public boolean isDoStartTls() {
        return this.doStartTls;
    }

    public boolean isConnected() {
        return (this.socket != null);
    }

    public Socket createSocket() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        logger.trace("createSocket() entered");
        Socket newSocket = null;
        if (!ssl) {
            logger.trace("Creating non-SSL socket");
            newSocket = new Socket(this.host, this.port);
        } else {
            String contextName = nameSelector.getSelectedSSLContextName();
            logger.trace("Initializing SSL context: " + String.valueOf(contextName));

            SSLContext sslContext = SSLContext.getInstance(contextName);
            sslContext.init(null, new TrustManager[] { new TrustAllCerts() }, new java.security.SecureRandom());

            logger.trace("Initialized SSL context OK");

            logger.trace("Creating socket with remote host: " + String.valueOf(this.getHostPort()));
            newSocket = sslContext.getSocketFactory().createSocket(this.host, this.port);
            logger.trace("Created socket OK");
            
            logger.trace("Starting SSL handshake");
            ((SSLSocket) newSocket).startHandshake();
            logger.trace("SSL handshake OK");
        }
        newSocket.setTcpNoDelay(true);
        newSocket.setKeepAlive(true);

        logger.trace("createSocket() exiting");
        return newSocket;
    }

    public void close() {
        logger.trace("close() entered");
        if (socket != null) {
            try {
                logger.trace("Closing socket...");
                socket.close();
                logger.trace("Socket closed OK");
            } catch (IOException e) {
                logger.error("Failed to close the socket", e);
            }
            socket = null;
            inputStream = null;
            outputStream = null;
            
            logger.trace("Interrupting keep-alive thread");

            this.keepAlive.shutdown = true;
            this.keepAlive.interrupt();

            logger.trace("Keep-alive thread interrupted OK");
        }
        logger.trace("close() exiting");
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }


    public Socket startTls() throws NetworkException {
        logger.trace("startTls() entered");
        try {
            String contextName = nameSelector.getSelectedSSLContextName();
            logger.trace("Initializing SSL context: " + String.valueOf(contextName));

            SSLContext sslContext = SSLContext.getInstance(contextName);
            sslContext.init(null, new TrustManager[] { new TrustAllCerts() }, new java.security.SecureRandom());

            logger.trace("Initialized SSL context OK");

            logger.trace("Creating socket with remote host: " + String.valueOf(this.getHostPort()));
            Socket tlsSocket = sslContext.getSocketFactory().createSocket(socket, this.host, this.port, false);
            logger.trace("Created socket OK");

            logger.trace("Starting SSL handshake");
            ((SSLSocket) tlsSocket).startHandshake();
            logger.trace("SSL handshake OK");

            tlsSocket.setTcpNoDelay(true);
            tlsSocket.setKeepAlive(true);

            this.socket = tlsSocket;
            this.inputStream = tlsSocket.getInputStream(); 
            this.outputStream = tlsSocket.getOutputStream();

            logger.trace("startTls() exiting");
            return tlsSocket;
        } catch(Exception e) {
            throw new NetworkException("Problem negotiating TLS on plain socket", e);
        }
    }

    public void sendDatastream(byte[] outboundDatastream) throws NetworkException {
        if (this.errorException != null) {
            throw new NetworkException("Terminal network connection has gone into error state",this.errorException);
        }

        sendDatastream(outputStream, outboundDatastream);
    }

    public void sendDatastream(OutputStream outputStream, byte[] outboundDatastream) throws NetworkException {
        if (outputStream == null) {
            throw new NetworkException("Attempt to send data to a disconnected terminal " + this.terminalId);
        }
        
        synchronized(outputStream) {
            try {
                byte[] header = new byte[] { 0, 0, 0, 0, 0 };
                byte[] trailer = new byte[] { (byte) 0xff, (byte) 0xef };

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (!this.basicTelnet) {
                    baos.write(header);
                }
                baos.write(outboundDatastream);
                baos.write(trailer);
                outputStream.write(baos.toByteArray());
                outputStream.flush();

                this.lastSend = Instant.now();
            } catch (IOException e) {
                throw new NetworkException("Unable to write outbound datastream", e);
            }
        }
    }

    public void sendIac(byte[] outboundIac) throws NetworkException {
        synchronized(outputStream) {
            try {
                outputStream.write(outboundIac);
                outputStream.flush();

                this.lastSend = Instant.now();
            } catch (IOException e) {
                throw new NetworkException("Unable to write outbound iac", e);
            }
        }
    }

    public boolean isTls() {
        return this.ssl;
    }

    private void sendKeepAlive() {
        logger.trace("sendKeepAlive() entered");
        if (this.outputStream == null) {
            logger.trace("sendKeepAlive() exiting, output stream is null");
            return;
        }

        if (this.lastSend.plus(10, ChronoUnit.MINUTES).isAfter(Instant.now())) {
            logger.trace("sendKeepAlive() exiting, a keep-alive has been sent recently");
            return;
        }

        synchronized(this.outputStream) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(NetworkThread.IAC);
                baos.write(NetworkThread.DO);
                baos.write(NetworkThread.TIMING_MARK);
                outputStream.write(baos.toByteArray());
                outputStream.flush();
                this.lastSend = Instant.now();
                logger.trace("Keep-alive sent OK");
            } catch(Exception e) {
                logger.error("Failed to write DO TIMING MARK",e);
            }
        }
        logger.trace("sendKeepAlive() exiting");
    }

    public String getHostPort() {
        return this.host + ":" + Integer.toString(this.port);
    }

    private class TrustAllCerts implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // TODO Add functionality for the Certificate management
            logger.trace("checkClientTrusted() entered");
            logger.trace("authType is: " + String.valueOf(authType));

            if (chain != null) {
                logger.trace(Integer.toString(chain.length) + " certificates in chain received");
            } else {
                logger.trace("No certificates to check");
            }
            logger.trace("checkClientTrusted() exiting");

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // TODO Add functionality for the Certificate management
            logger.trace("checkServerTrusted() entered");
            logger.trace("authType is: " + String.valueOf(authType));

            if (chain != null) {
                logger.trace(Integer.toString(chain.length) + " certificates in chain received");
            } else {
                logger.trace("No certificates to check");
            }
            logger.trace("checkServerTrusted() exiting");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            logger.trace("getAcceptedIssuers() entered, returning empty array");
            return new X509Certificate[0];
        }

    }

    private class KeepAlive extends Thread {

        private boolean shutdown = false;

        public KeepAlive() {
            logger.trace("Starting keep-alive thread");
            setName("3270 keep alive");
        }

        @Override
        public void run() {
            logger.trace("Started keep-alive thread");
            while(!shutdown) {
                sendKeepAlive();

                try {
                    Thread.sleep(5000);
                } catch(Exception e) {
                    logger.trace("Keep-alive encountered an exception, interrupting thread...", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void setBasicTelnet(boolean basicTelnet) {
        this.basicTelnet = basicTelnet;
    }

    public void switchedSSL(boolean switchedSSL) {
        this.switchedSSL = switchedSSL;
    }
    
    public boolean isSwitchedSSL() {
        return this.switchedSSL;
    }

}
