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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.spi.NetworkException;

public class NetworkServer /* extends Thread */ {

    public static final Charset ascii7 = Charset.forName("us-ascii");

    private final Log           logger = LogFactory.getLog(getClass());

    private final Socket        socket;
    private final OutputStream  outputStream;
    private final InputStream   inputStream;
    private String              deviceName;

    public NetworkServer(Socket socket) throws NetworkException {
        try {
            this.socket = socket;
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);

            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();

            negotiate(this.inputStream, this.outputStream);
        } catch (IOException e) {
            throw new NetworkException("Unable to initialise the server", e);
        }

    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("Problem closing socket", e);
            }
        }
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void negotiate(InputStream inputStream, OutputStream outputStream) throws NetworkException {
        try {
            // *** Send the DO TN3270E initialisation message
            byte[] doTN3270 = new byte[] { NetworkThread.IAC, NetworkThread.DO, NetworkThread.TN3270E };
            outputStream.write(doTN3270);
            outputStream.flush();

            // *** Let see what the client says, we are only going to support TN3270E
            // clients
            expect(inputStream, NetworkThread.IAC, NetworkThread.WILL, NetworkThread.TN3270E);

            byte[] sendDeviceType = new byte[] { NetworkThread.IAC, NetworkThread.SB, NetworkThread.TN3270E, NetworkThread.SEND,
                    NetworkThread.DEVICE_TYPE, NetworkThread.IAC, NetworkThread.SE };
            outputStream.write(sendDeviceType);
            outputStream.flush();

            String deviceType = "";
            boolean gotCorrectDeviceType = false;
            while (!gotCorrectDeviceType) {
                // *** Get the requested device type, only accepting IBM-3278-2 terminals for
                // the moment
                ByteBuffer buffer = readSbSeMessage(inputStream);
                expect(buffer, NetworkThread.DEVICE_TYPE, NetworkThread.REQUEST);
                deviceType = "";
                deviceName = "";
                boolean readingType = true;
                while (buffer.hasRemaining()) {
                    byte[] b = new byte[1];
                    buffer.get(b);
                    if (b[0] == NetworkThread.CONNECT || b[0] == NetworkThread.ASSOCIATE) {
                        readingType = false;
                    } else {
                        String bs = new String(b, ascii7);
                        if (readingType) {
                            deviceType += bs;
                        } else {
                            deviceName += bs;
                        }
                    }
                }

                if ("IBM-3278-2".equals(deviceType) || "IBM-3278-2-E".equals(deviceType)
                        || "IBM-3278-3".equals(deviceType) || "IBM-3278-3-E".equals(deviceType)
                        || "IBM-3278-4".equals(deviceType) || "IBM-3278-4-E".equals(deviceType)
                        || "IBM-3278-5".equals(deviceType) || "IBM-3278-5-E".equals(deviceType)
                        || "IBM-3279-2".equals(deviceType) || "IBM-3279-2-E".equals(deviceType)
                        || "IBM-3279-3".equals(deviceType) 
                        || "IBM-DYNAMIC".equals(deviceType)) {
                    break;
                }

                // *** Reject Device Type
                byte[] rejectDeviceType = new byte[] { NetworkThread.IAC, NetworkThread.SB, NetworkThread.TN3270E, NetworkThread.DEVICE_TYPE,
                        NetworkThread.REJECT, NetworkThread.REASON, NetworkThread.INV_DEVICE_TYPE, NetworkThread.IAC, NetworkThread.SE };
                outputStream.write(rejectDeviceType);
                outputStream.flush();
            }

            if (deviceName.isEmpty()) {
                deviceName = "TERM0001";
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(NetworkThread.IAC);
            baos.write(NetworkThread.SB);
            baos.write(NetworkThread.TN3270E);
            baos.write(NetworkThread.DEVICE_TYPE);
            baos.write(NetworkThread.IS);
            baos.write(deviceType.getBytes(ascii7));
            baos.write(NetworkThread.CONNECT);
            baos.write(deviceName.getBytes(ascii7));
            baos.write(NetworkThread.IAC);
            baos.write(NetworkThread.SE);
            outputStream.write(baos.toByteArray());
            outputStream.flush();

            // *** Negotiate functions, we are going to accept any
            ByteBuffer buffer = readSbSeMessage(inputStream);
            expect(buffer, NetworkThread.FUNCTIONS, NetworkThread.REQUEST);

            byte[] noFunctions = new byte[] { NetworkThread.IAC, NetworkThread.SB, NetworkThread.TN3270E, NetworkThread.FUNCTIONS, NetworkThread.IS,
                    NetworkThread.IAC, NetworkThread.SE };
            outputStream.write(noFunctions);
            outputStream.flush();
        } catch (IOException e) {
            throw new NetworkException("IOException during terminal negotiation", e);
        }
    }

    public void sendDatastream(byte[] outboundDatastream) throws NetworkException {
        sendDatastream(outputStream, outboundDatastream);
    }

    public void sendDatastream(OutputStream outputStream, byte[] outboundDatastream) throws NetworkException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] header = new byte[] { 0, 0, 0, 0, 0 };
            byte[] trailer = new byte[] { (byte) 0xff, (byte) 0xef };

            baos.write(header);
            baos.write(outboundDatastream);
            baos.write(trailer);

            outputStream.write(baos.toByteArray());
            outputStream.flush();
        } catch (IOException e) {
            throw new NetworkException("Unable to write outbound datastream", e);
        }

    }

    public static void expect(InputStream inputStream, byte... expected) throws IOException, NetworkException {
        byte[] received = new byte[expected.length];

        int length = inputStream.read(received);

        if (length != expected.length) {
            throw new NetworkException("Expected " + expected.length + " but received only " + length + " bytes");
        }

        if (!Arrays.equals(expected, received)) {
            String expectedString = Hex.encodeHexString(expected);
            String receivedString = Hex.encodeHexString(received);
            throw new NetworkException("Expected " + expectedString + " but received " + receivedString);
        }
    }

    public static void expect(ByteBuffer buffer, byte... expected) throws NetworkException {
        byte[] received = new byte[expected.length];

        buffer.get(received);

        if (!Arrays.equals(expected, received)) {
            String expectedString = Hex.encodeHexString(expected);
            String receivedString = Hex.encodeHexString(received);
            throw new NetworkException("Expected " + expectedString + " but received " + receivedString);
        }
    }

    public static ByteBuffer readSbSeMessage(InputStream messageStream) throws IOException, NetworkException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        expect(messageStream, NetworkThread.IAC, NetworkThread.SB, NetworkThread.TN3270E);

        byte[] b = new byte[1];
        boolean lastByteFF = false;
        boolean terminated = false;
        while (messageStream.read(b) == 1) {
            if (b[0] == NetworkThread.IAC) {
                if (lastByteFF) {
                    byteArrayOutputStream.write(b);
                    lastByteFF = false;
                } else {
                    lastByteFF = true;
                }
            } else {
                if (b[0] == NetworkThread.SE && lastByteFF) {
                    terminated = true;
                    break;
                }

                byteArrayOutputStream.write(b);
            }
        }

        if (!terminated) {
            throw new NetworkException("3270 message did not terminate with IAC SE");
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return ByteBuffer.wrap(bytes);
    }

    public String getDeviceName() {
        return this.deviceName;
    }

}
