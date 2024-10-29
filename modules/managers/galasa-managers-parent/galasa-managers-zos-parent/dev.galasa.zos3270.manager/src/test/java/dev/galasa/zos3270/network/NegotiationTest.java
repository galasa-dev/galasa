/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class NegotiationTest {

    public static final byte IAC         = -1;

    public static final byte DO          = -3;
    public static final byte WILL        = -5;
    public static final byte SB          = -6;
    public static final byte SE          = -16;

    public static final byte CONNECT     = 1;
    public static final byte DEVICE_TYPE = 2;
    public static final byte RESPONSES   = 2;
    public static final byte FUNCTIONS   = 3;
    public static final byte IS          = 4;
    public static final byte REQUEST     = 7;
    public static final byte SEND        = 8;
    public static final byte TN3270E     = 40;

//    @Test
//    public void testSuccessfulNegotiationBasicNoFunctions()
//            throws NetworkException, UnsupportedEncodingException, IOException {
//        ByteArrayInputStream fromServer = new ByteArrayInputStream(getServerNegotiation().toByteArray());
//        ByteArrayOutputStream toServer = new ByteArrayOutputStream();
//
//        Network network = new Network("dummy", 0);
//
//        network.negotiate(new DummySocket(fromServer, toServer));
//
//        Assert.assertArrayEquals("Responses are not correct from client to server",
//                getClientNegotiation().toByteArray(), toServer.toByteArray());
//    }
//
//    @Test
//    public void testNegotiationIoException() {
//
//        IOExceptionInputStream fromServer = new IOExceptionInputStream();
//        ByteArrayOutputStream toServer = new ByteArrayOutputStream();
//
//        try {
//            Network network = new Network("dummy", 0);
//            network.negotiate(new DummySocket(fromServer, toServer));
//            fail("Should have thrown an NetworkException");
//        } catch (NetworkException e) {
//        }
//    }
//
//    @Test
//    public void testExpectOk() throws NetworkException, IOException {
//        ByteArrayOutputStream prepInput = new ByteArrayOutputStream();
//        prepInput.write(IAC);
//        prepInput.write(DO);
//        prepInput.write(TN3270E);
//
//        ByteArrayInputStream fromServer = new ByteArrayInputStream(prepInput.toByteArray());
//        Network.expect(fromServer, IAC, DO, TN3270E);
//        Assert.assertTrue("dummy for SonarQube", true);
//    }
//
//    @Test
//    public void testExpectShort() throws NetworkException, IOException {
//        ByteArrayOutputStream prepInput = new ByteArrayOutputStream();
//        prepInput.write(IAC);
//        prepInput.write(DO);
//
//        ByteArrayInputStream fromServer = new ByteArrayInputStream(prepInput.toByteArray());
//        try {
//            Network.expect(fromServer, IAC, DO, TN3270E);
//            fail("Should have thrown an NetworkException");
//        } catch (NetworkException e) {
//            Assert.assertEquals("Error message incorrect", "Expected 3 but received only 2 bytes", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testExpectDifferent() throws NetworkException, IOException {
//        ByteArrayOutputStream prepInput = new ByteArrayOutputStream();
//        prepInput.write(IAC);
//        prepInput.write(DO);
//        prepInput.write(WILL);
//
//        ByteArrayInputStream fromServer = new ByteArrayInputStream(prepInput.toByteArray());
//        try {
//            Network.expect(fromServer, IAC, DO, TN3270E);
//            fail("Should have thrown an NetworkException");
//        } catch (NetworkException e) {
//            Assert.assertEquals("Error message incorrect", "Expected fffd28 but received fffdfb", e.getMessage());
//        }
//    }

    public static ByteArrayOutputStream getServerNegotiation() throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream prepInput = new ByteArrayOutputStream();
        prepInput.write(IAC);
        prepInput.write(DO);
        prepInput.write(TN3270E);

        prepInput.write(IAC);
        prepInput.write(SB);
        prepInput.write(TN3270E);
        prepInput.write(SEND);
        prepInput.write(DEVICE_TYPE);
        prepInput.write(IAC);
        prepInput.write(SE);

        prepInput.write(IAC);
        prepInput.write(SB);
        prepInput.write(TN3270E);
        prepInput.write(DEVICE_TYPE);
        prepInput.write(IS);
        prepInput.write("IBM-3278-2-E".getBytes("us-ascii"));
        prepInput.write(CONNECT);
        prepInput.write("TERM0001".getBytes("us-ascii"));
        prepInput.write(IAC);
        prepInput.write(SE);

        prepInput.write(IAC);
        prepInput.write(SB);
        prepInput.write(TN3270E);
        prepInput.write(FUNCTIONS);
        prepInput.write(IS);
        prepInput.write(IAC);
        prepInput.write(SE);
        return prepInput;
    }

    public static ByteArrayOutputStream getClientNegotiation() throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream prepOutput = new ByteArrayOutputStream();
        prepOutput.write(IAC);
        prepOutput.write(WILL);
        prepOutput.write(TN3270E);

        prepOutput.write(IAC);
        prepOutput.write(SB);
        prepOutput.write(TN3270E);
        prepOutput.write(DEVICE_TYPE);
        prepOutput.write(REQUEST);
        prepOutput.write("IBM-3278-2-E".getBytes("us-ascii"));
        prepOutput.write(IAC);
        prepOutput.write(SE);

        prepOutput.write(IAC);
        prepOutput.write(SB);
        prepOutput.write(TN3270E);
        prepOutput.write(FUNCTIONS);
        prepOutput.write(REQUEST);
        prepOutput.write(IAC);
        prepOutput.write(SE);
        return prepOutput;
    }

    public static class DummySocket extends Socket {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public DummySocket(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return this.inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.outputStream;
        }

    }

}
