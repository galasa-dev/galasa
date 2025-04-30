/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos3270.IDatastreamListener.DatastreamDirection;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.OrderCarrageReturn;
import dev.galasa.zos3270.internal.datastream.OrderEndOfMedium;
import dev.galasa.zos3270.internal.datastream.OrderEraseUnprotectedToAddress;
import dev.galasa.zos3270.internal.datastream.OrderFormFeed;
import dev.galasa.zos3270.internal.datastream.OrderGraphicsEscape;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderModifyField;
import dev.galasa.zos3270.internal.datastream.OrderNewLine;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetAttribute;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderStartFieldExtended;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.spi.Terminal;

public class NetworkThread extends Thread {

    public static final byte    IAC             = -1;

    public static final byte    DONT            = -2;
    public static final byte    DO              = -3;
    public static final byte    WONT            = -4;
    public static final byte    WILL            = -5;
    public static final byte    SB              = -6;
    public static final byte    SE              = -16;
    public static final byte    EOR             = -17;

    public static final byte    ASSOCIATE       = 0;
    public static final byte    TELNET_BINARY   = 0;
    public static final byte    FUNC_BIND_IMAGE = 0;
    public static final byte    CONNECT         = 1;
    public static final byte    TT_SEND         = 1;
    public static final byte    FOLLOWS         = 1;
    public static final byte    DEVICE_TYPE     = 2;
    public static final byte    RESPONSES       = 2;
    public static final byte    FUNCTIONS       = 3;
    public static final byte    IS              = 4;
    public static final byte    FUNC_SYSREQ     = 4;
    public static final byte    REASON          = 5;
    public static final byte    REJECT          = 6;
    public static final byte    TIMING_MARK     = 6;
    public static final byte    REQUEST         = 7;
    public static final byte    SEND            = 8;
    public static final byte    TERMINAL_TYPE   = 24;
    public static final byte    TELNET_EOR      = 25;
    public static final byte    TN3270E         = 40;
    public static final byte    START_TLS       = 46;

    public static final byte    CONN_PARTNER     = 0;
    public static final byte    DEVICE_IN_USE    = 1;
    public static final byte    INV_ASSOCIATE    = 2;
    public static final byte    INV_NAME         = 3;
    public static final byte    INV_DEVICE_TYPE  = 4;
    public static final byte    TYPE_NAME_ERROR  = 5;
    public static final byte    UNKNOWN_ERROR    = 6;
    public static final byte    UNSUPPORTED_REQ  = 7;

    public static final byte  DT_3270_DATA    = 0;
    public static final byte  DT_SCS_DATA     = 1;
    public static final byte  DT_RESPONSE     = 2;
    public static final byte  DT_BIND_IMAGE   = 3;
    public static final byte  DT_UNBIND       = 4;
    public static final byte  DT_NVT_DATA     = 5;
    public static final byte  DT_REQUEST      = 6;
    public static final byte  DT_SSCP_LU_DATA = 7;
    public static final byte  DT_PRINT_EOJ    = 8;

    public static final Charset ascii7          = Charset.forName("us-ascii");

    private InputStream inputStream;
    private final Screen      screen;
    private final Network     network;
    private final Terminal    terminal;

    private boolean telnetSessionStarted      = false;
    private boolean basicTelnetDatastream     = false;

    private boolean           endOfStream     = false;

    private static Log               logger          = LogFactory.getLog(NetworkThread.class);

    private final ArrayList<String>  possibleDeviceTypes = new ArrayList<>();
    private String                   selectedDeviceType;

    private ByteArrayOutputStream    commandSoFar;

    private SSCPLUDataTransform sscpLuDataTransform;

    public NetworkThread(Terminal terminal, Screen screen, Network network, InputStream inputStream) {
        this(terminal, screen, network, inputStream, null);
    }

    public NetworkThread(Terminal terminal, Screen screen, Network network, InputStream inputStream, List<String> deviceTypes) {
        this.screen = screen;
        this.network = network;
        this.inputStream = inputStream;
        this.terminal = terminal;
        this.sscpLuDataTransform = new SSCPLUDataTransform(screen);

        if (deviceTypes == null || deviceTypes.isEmpty()) {
            this.possibleDeviceTypes.add("IBM-DYNAMIC");
            this.possibleDeviceTypes.add("IBM-3278-2");
        } else {
            this.possibleDeviceTypes.addAll(deviceTypes);
        }
    }

    @Override
    public void run() {
        logger.trace("Starting network thread on terminal " + terminal.getId());

        while (!endOfStream) {
            try {
                processMessage(inputStream);
            } catch (NetworkException e) {
                logger.error("Problem with Network Thread", e);
                break;
            } catch (IOException e) {
                if (e.getMessage().contains("Socket closed")) {
                    break;
                }
                logger.error("Problem with Network Thread", e);
                break;
            }
        }
        try {
            screen.networkClosed();
        } catch (TerminalInterruptedException e) {
            logger.error("Problem locking keyboard on network close",e);
        }

        logger.trace("Ending network thread on terminal " + terminal.getId());
        terminal.networkClosed();
    }

    public void processMessage(InputStream messageStream) throws IOException, NetworkException {
        this.commandSoFar = new ByteArrayOutputStream();

        Byte header = readByte(messageStream);
        if (header == null) {
            return;
        }

        if (header == IAC) {
            doIac(messageStream);
            return;
        }

        if (basicTelnetDatastream) {
            this.telnetSessionStarted = true;  // must be started if receiving 3270

            ByteBuffer buffer = readTerminatedMessage(header, messageStream);


            Inbound3270Message inbound3270Message = process3270Data(buffer);
            this.screen.processInboundMessage(inbound3270Message);
            return;
        } else {
            this.telnetSessionStarted = true;  // must be started if receiving 3270

            ByteBuffer buffer = readTerminatedMessage(header, messageStream);

            if (buffer.remaining() < 5) {
                throw new NetworkException("Missing 5 bytes of the TN3270E datastream header");
            }

            byte tn3270eHeader = buffer.get();
            if (tn3270eHeader == DT_BIND_IMAGE) {
                logger.trace("BIND_IMAGE received");
                logger.trace("Received message header: " + reportCommandSoFar());
                logger.trace("Received message buffer: " + Hex.encodeHexString(buffer.array()));
                return;
            }
            if (tn3270eHeader == DT_UNBIND) {
                logger.trace("UNBIND_IMAGE received");
                logger.trace("Received message header: " + reportCommandSoFar());
                logger.trace("Received message buffer: " + Hex.encodeHexString(buffer.array()));
                return;
            }

            // 3270 datastream headers consist of 5 bytes in the following format:
            // -----------------------------------------------------------
            //   DATA-TYPE | REQUEST-FLAG | RESPONSE-FLAG |  SEQ-NUMBER  
            //    1 byte        1 byte         1 byte         2 bytes
            // -----------------------------------------------------------
            // This advances through the message buffer to skip the header bytes after DATA-TYPE.
            buffer.get(new byte[4]);
            
            Inbound3270Message inbound3270Message = null;
            if (tn3270eHeader == DT_SSCP_LU_DATA) {
                logger.trace("SSCP_LU_DATA received");
                logger.trace("Received message header: " + reportCommandSoFar());
                logger.trace("Received message buffer: " + Hex.encodeHexString(buffer.array()));

                // Convert the SSCP-LU-DATA datastream into a 3270 message
                // This makes the assumption that SSCP-LU-DATA datastreams are unformatted and do not
                // contain any special command codes or orders that are in normal 3270 datastreams.
                // The resulting 3270 message will only contain text and newline orders.
                inbound3270Message = this.sscpLuDataTransform.processSSCPLUData(buffer);

            } else if (tn3270eHeader == DT_3270_DATA) {
                inbound3270Message = process3270Data(buffer);
            } else {
                throw new NetworkException("Was expecting a TN3270E datastream header of zeros - " + reportCommandSoFar());
            }

            this.screen.processInboundMessage(inbound3270Message);
        }
    }

    private void doIac(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC terminated early - " + reportCommandSoFar());
        }

        if (iac == DO) {
            doIacDo(messageStream);
            return;
        }
        if (iac == DONT) {
            doIacDont(messageStream);
            return;
        }
        if (iac == SB) {
            doIacSb(messageStream);
            return;
        }
        if (iac == WILL) {
            doIacWill(messageStream);
            return;
        }
        if (iac == WONT) {
            doIacWont(messageStream);
            return;
        }


        throw new NetworkException("Unrecognised IAC Command - " + reportCommandSoFar());
    }

    private void doIacSb(InputStream messageStream) throws NetworkException, IOException {
        // Read the whole SB SE command
        ByteBuffer remainingSb = readTerminatedSB(messageStream);

        byte sb = remainingSb.get();

        if (sb == TN3270E) {
            doIacSbTn3270e(remainingSb);
            return;
        }
        if (sb == START_TLS) {
            doIacSbStartTls(remainingSb);
            return;
        }
        if (sb == TERMINAL_TYPE) {
            doIacSbTerminalType(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB Command - " + reportCommandSoFar());
    }

    private void doIacWill(InputStream messageStream) throws NetworkException, IOException {
        Byte will = readByte(messageStream);
        if (will == null) {
            throw new NetworkException("Unrecognised IAC WILL terminated early - " + reportCommandSoFar());
        }

        throw new NetworkException("Unrecognised IAC WILL Command - " + reportCommandSoFar());
    }

    private void doIacWont(InputStream messageStream) throws NetworkException, IOException {
        Byte will = readByte(messageStream);
        if (will == null) {
            throw new NetworkException("Unrecognised IAC WONT terminated early - " + reportCommandSoFar());
        }

        if (will == TIMING_MARK) {
            // Ignore
            return;
        }

        throw new NetworkException("Unrecognised IAC WONT Command - " + reportCommandSoFar());
    }

    private void doIacSbTn3270e(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == SEND) {
            doIacSbTn3270eSend(remainingSb);
            return;
        }
        if (sb == DEVICE_TYPE) {
            doIacSbTn3270eDeviceType(remainingSb);
            return;
        }
        if (sb == FUNCTIONS) {
            doIacSbTn3270eFunctions(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TN3270E Command - " + reportCommandSoFar());
    }

    private void doIacSbStartTls(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == FOLLOWS) {
            doIacSbStartTlsFollows(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB START_TLS Command - " + reportCommandSoFar());
    }

    private void doIacSbStartTlsFollows(ByteBuffer remainingSb) throws NetworkException, IOException {
        logger.trace("TN3270E switching to TLS");

        Socket newSocket = this.network.startTls();
        this.inputStream = newSocket.getInputStream();
        this.network.switchedSSL(true);


        logger.trace("TN3270E switched to TLS");
    }

    private void doIacSbTn3270eSend(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == DEVICE_TYPE) {
            doIacSbTn3270eSendDeviceType(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TN3270E SEND Command - " + reportCommandSoFar());
    }

    private void doIacSbTn3270eSendDeviceType(ByteBuffer remainingSb) throws NetworkException, IOException {
        logger.trace("IAC SB TN3270E SEND DEVICE_TYPE received from server");

        requestDeviceTypeDeviceName();
    }


    private void doIacSbTerminalType(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == TT_SEND) {
            doIacSbTerminalTypeSend(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TERMINAL-TYPE Command - " + reportCommandSoFar());
    }

    private void doIacSbTerminalTypeSend(ByteBuffer remainingSb) throws NetworkException, IOException {
        logger.trace("IAC SB TERMINAL-TYPE SEND received from server");

        requestTerminalType();
    }


    private void requestDeviceTypeDeviceName() throws NetworkException, IOException {
        if (this.possibleDeviceTypes.isEmpty()) {
            throw new NetworkException("Ran out of TN3270E device types to negotiate for");
        }

        if (this.selectedDeviceType != null) {
            throw new NetworkException("logic error, nothing new to negotiate device type with");
        }

        this.selectedDeviceType = this.possibleDeviceTypes.remove(0);
        logger.trace("Requesting TN3270E device type " + this.selectedDeviceType);

        byte[] deviceType = this.selectedDeviceType.getBytes(ascii7);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(IAC);
        baos.write(SB);
        baos.write(TN3270E);
        baos.write(DEVICE_TYPE);
        baos.write(REQUEST);
        baos.write(deviceType);
        baos.write(IAC);
        baos.write(SE);

        this.network.sendIac(baos.toByteArray());

        return;
    }

    private void requestTerminalType() throws NetworkException, IOException {
        if (this.possibleDeviceTypes.isEmpty()) {
            throw new NetworkException("Ran out of terminal types to negotiate for");
        }

        if (this.selectedDeviceType != null) {
            throw new NetworkException("logic error, nothing new to negotiate device type with");
        }

        this.selectedDeviceType = this.possibleDeviceTypes.remove(0);
        logger.trace("Requesting terminal type " + this.selectedDeviceType);

        byte[] deviceType = this.selectedDeviceType.getBytes(ascii7);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(IAC);
        baos.write(SB);
        baos.write(TERMINAL_TYPE);
        baos.write(0);
        baos.write(deviceType);
        baos.write(IAC);
        baos.write(SE);

        this.network.sendIac(baos.toByteArray());

        return;
    }

    private void doIacSbTn3270eDeviceType(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == IS) {
            doIacSbTn3270eDeviceTypeIs(remainingSb);
            return;
        }
        if (sb == REJECT) {
            doIacSbTn3270eDeviceTypeReject(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TN3270E DEVICE_TYPE Command - " + reportCommandSoFar());
    }

    private void doIacSbTn3270eDeviceTypeIs(ByteBuffer remainingSb) throws NetworkException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            byte b = remainingSb.get();

            if (b == CONNECT) {
                break;
            }

            baos.write(b);
        }

        this.selectedDeviceType = new String(baos.toByteArray(), ascii7);

        baos = new ByteArrayOutputStream();
        while (remainingSb.hasRemaining()) {
            byte b = remainingSb.get();

            if (b == IAC) {
                break;
            }

            baos.write(b);
        }

        String luName = new String(baos.toByteArray(), ascii7);
        logger.trace("TN3270 device type " + this.selectedDeviceType + " with LU " + luName + " was agreed");

        negotiateFunctions();
    }

    private void doIacSbTn3270eFunctions(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == IS) {
            doIacSbTn3270eFunctionsIs(remainingSb);
            return;
        }
        if (sb == REQUEST) {
            doIacSbTn3270eFunctionsRequest(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TN3270E FUNCTIONS Command - " + reportCommandSoFar());
    }

    private void doIacSbTn3270eFunctionsRequest(ByteBuffer remainingSb) throws NetworkException, IOException {
        logger.trace("TN3270E server renegoiating FUNCTIONS");
        boolean bind   = false;
        boolean sysreq = false;
        boolean rerequest = false;
        while(remainingSb.hasRemaining()) {
            byte function = remainingSb.get();
            if (function == FUNC_BIND_IMAGE) {
                logger.trace("TN3270E function BIND_IMAGE requested");
                bind = true;
            } else if (function == FUNC_SYSREQ) {
                logger.trace("TN3270E function SYSREQ requested");
                sysreq = true;
            } else {
                logger.trace("Unexpected function on FUNCTIONS REQUEST = 0x" + Hex.encodeHexString(new byte[] { function }) + ", rejecting");
                rerequest = true;
            }
        }

        // need to indicate renegotiation,  or say we accept

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(IAC);
        baos.write(SB);
        baos.write(TN3270E);
        baos.write(FUNCTIONS);
        if (rerequest) {
            logger.trace("Renegotiating FUNCTIONS with :-");
            baos.write(REQUEST);
        } else {
            logger.trace("TN3270E FUNCTIONS accepted with :-");
            baos.write(IS);
        }
        if (bind) {
            baos.write(FUNC_BIND_IMAGE);
            logger.trace("     BIND_IMAGE");
        }
        if (sysreq) {
            baos.write(FUNC_SYSREQ);
            logger.trace("     SYSREQ");
        }
        baos.write(IAC);
        baos.write(SE);
        this.network.sendIac(baos.toByteArray());
    }







    private void doIacSbTn3270eFunctionsIs(ByteBuffer remainingSb) throws NetworkException, IOException {
        logger.trace("TN3270E FUNCTIONS IS accepted with :-");
        while(remainingSb.hasRemaining()) {
            byte function = remainingSb.get();
            if (function == FUNC_BIND_IMAGE) {
                logger.trace("     BIND_IMAGE");
            } else if (function == FUNC_SYSREQ) {
                logger.trace("     SYSREQ");
            } else {
                throw new NetworkException("Unexpected function on FUNCTIONS IS = 0x" + Hex.encodeHexString(new byte[] { function }));
            }
        }

        // At this point we should be fully negotiated, so mark thread ready
        logger.trace("TN3270E negotiation complete, 3270 datastream should now start");
        this.telnetSessionStarted = true;
    }

    private void negotiateFunctions() throws NetworkException {
        logger.trace("Requesting TN3270E functions BIND_IMAGE, SYSREQ");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(IAC);
        baos.write(SB);
        baos.write(TN3270E);
        baos.write(FUNCTIONS);
        baos.write(REQUEST);
        baos.write(FUNC_BIND_IMAGE);
        baos.write(FUNC_SYSREQ);
    //    baos.write(0x34); // invalid function to drive negotiation
        baos.write(IAC);
        baos.write(SE);
        this.network.sendIac(baos.toByteArray());
    }

    private void doIacSbTn3270eDeviceTypeReject(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte sb = remainingSb.get();

        if (sb == REASON) {
            doIacSbTn3270eDeviceTypeRejectReason(remainingSb);
            return;
        }

        throw new NetworkException("Unrecognised IAC SB TN3270E DEVICE_TYPE REJECT Command - " + reportCommandSoFar());
    }

    private void doIacSbTn3270eDeviceTypeRejectReason(ByteBuffer remainingSb) throws NetworkException, IOException {
        byte reasonCode = remainingSb.get();

        switch(reasonCode) {
            case CONN_PARTNER:
                throw new NetworkException("Device negotiation failed due to CONN_PARTNER");
            case DEVICE_IN_USE:
                throw new NetworkException("Device negotiation failed due to DEVICE_IN_USE");
            case INV_ASSOCIATE:
                throw new NetworkException("Device negotiation failed due to INV_ASSOCIATE");
            case INV_NAME:
                throw new NetworkException("Device negotiation failed due to INV_NAME");
            case INV_DEVICE_TYPE :
                logger.trace("TN3270 device type " + this.selectedDeviceType + " was rejected as invalid INV_DEVICE_TYPE");
                this.selectedDeviceType = null;
                requestDeviceTypeDeviceName();
                return;
            case TYPE_NAME_ERROR:
                throw new NetworkException("Device negotiation failed due to TYPE_NAME_ERROR");
            case UNKNOWN_ERROR:
                throw new NetworkException("Device negotiation failed due to UNKNOWN_ERROR");
            case UNSUPPORTED_REQ:
                throw new NetworkException("Device negotiation failed due to UNSUPPORTED_REQ");
            default:
                throw new NetworkException("Unrecognised reason code for rejected device type =" + reasonCode);
        }
    }

    private void doIacDo(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO terminated early - " + reportCommandSoFar());
        }

        if (iac == TIMING_MARK) {
            doIacDoTimingMark(messageStream);
            return;
        }
        if (iac == TN3270E) {
            doIacDoTn3270e(messageStream);
            return;
        }
        if (iac == START_TLS) {
            doIacDoStartTls(messageStream);
            return;
        }
        if (iac == TERMINAL_TYPE) {
            doIacDoTerminalType(messageStream);
            return;
        }
        if (iac == TELNET_EOR) {
            doIacDoTelnetEor(messageStream);
            return;
        }
        if (iac == TELNET_BINARY) {
            doIacDoTelnetBinary(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO Command - " + reportCommandSoFar());

    }

    private void doIacDont(InputStream messageStream) throws NetworkException, IOException {
        Byte dont = readByte(messageStream);
        if (dont == null) {
            throw new NetworkException("Unrecognised IAC DO terminated early - " + reportCommandSoFar());
        }

        if (dont == TN3270E) {
            logger.trace("Received IAC DONT TN3270E");

            if (this.selectedDeviceType != null) {
                this.possibleDeviceTypes.add(0, selectedDeviceType);
                this.selectedDeviceType = null;
            }


            return; // IGNORE
        }

        if (dont == TIMING_MARK) {
            // Ignore
            return;
        }

        throw new NetworkException("Unrecognised IAC DONT Command - " + reportCommandSoFar());
    }

    private void doIacDoTimingMark(InputStream messageStream) throws NetworkException {
        logger.trace("timing received");
        this.network.sendIac(new byte[] {IAC, WILL, TIMING_MARK});
    }

    private void doIacDoTelnetEor(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO EOR terminated early - " + reportCommandSoFar());
        }

        if (iac == IAC) {
            doIacDoTelnetEorIac(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO EOR Command - " + reportCommandSoFar());
    }


    private void doIacDoTelnetEorIac(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO EOR IAC terminated early - " + reportCommandSoFar());
        }

        if (iac == WILL) {
            doIacDoTelnetEorIacWill(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO EOR WILL Command - " + reportCommandSoFar());
    }


    private void doIacDoTelnetEorIacWill(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO EOR IAC WILL terminated early - " + reportCommandSoFar());
        }

        if (iac == TELNET_EOR) {
            doIacDoTelnetEorIacWillEor(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO EOR WILL Command - " + reportCommandSoFar());
    }

    private void doIacDoTelnetEorIacWillEor(InputStream messageStream) throws NetworkException, IOException {
        logger.trace("IAC DO EOR WILL EOR received from server");
        this.network.sendIac(new byte[] {IAC, WILL, TELNET_EOR, IAC, DO, TELNET_EOR});
        this.basicTelnetDatastream = true;

        this.network.setBasicTelnet(true);

    }


    private void doIacDoTelnetBinary(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO BINARY terminated early - " + reportCommandSoFar());
        }

        if (iac == IAC) {
            doIacDoTelnetBinaryIac(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO BINARY Command - " + reportCommandSoFar());
    }


    private void doIacDoTelnetBinaryIac(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO BINARY IAC terminated early - " + reportCommandSoFar());
        }

        if (iac == WILL) {
            doIacDoTelnetBinaryWill(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO BINARY WILL Command - " + reportCommandSoFar());
    }


    private void doIacDoTelnetBinaryWill(InputStream messageStream) throws NetworkException, IOException {
        Byte iac = readByte(messageStream);
        if (iac == null) {
            throw new NetworkException("Unrecognised IAC DO BINARY IAC WILL terminated early - " + reportCommandSoFar());
        }

        if (iac == TELNET_BINARY) {
            doIacDoTelnetBinaryWillBinary(messageStream);
            return;
        }

        throw new NetworkException("Unrecognised IAC DO BINARY WILL Command - " + reportCommandSoFar());
    }

    private void doIacDoTelnetBinaryWillBinary(InputStream messageStream) throws NetworkException, IOException {
        logger.trace("IAC DO BINARY WILL BINARY received from server");
        this.network.sendIac(new byte[] {IAC, WILL, TELNET_BINARY, IAC, DO, TELNET_BINARY});
    }


    private void doIacDoTerminalType(InputStream messageStream) throws NetworkException {
        logger.trace("IAC DO TERMINAL-TYPE received from server");
        this.network.sendIac(new byte[] {IAC, WILL, TERMINAL_TYPE});
    }

    private void doIacDoTn3270e(InputStream messageStream) throws NetworkException {
        logger.trace("IAC DO TN3270E received from server, responding with IAC WILL TN3270E");

        this.network.sendIac(new byte[] {IAC, WILL, TN3270E});

        this.network.setBasicTelnet(false);
    }

    private void doIacDoStartTls(InputStream messageStream) throws NetworkException, IOException {
        if (this.network.isDoStartTls()) {
            logger.trace("IAC DO START_TLS received from server, agreeing to switch to TLS");
            this.network.sendIac(new byte[] {IAC, WILL, START_TLS, IAC, SB, START_TLS, FOLLOWS, IAC, SE});
        } else {
            logger.trace("IAC DO START_TLS received from server, refusing");
            this.network.sendIac(new byte[] {IAC, WONT, START_TLS});
        }
    }

    private Byte readByte(InputStream messageStream) throws IOException {
        byte[] b = new byte[1];
        int length = messageStream.read(b);

        if (length == -1) {
            endOfStream = true;
            logger.trace("Terminal has been disconnected");
            return null;
        }
        if (length == 0) {
            return null;
        }

        this.commandSoFar.write(b);

        return b[0];
    }

    private String reportCommandSoFar() {
        return Hex.encodeHexString(this.commandSoFar.toByteArray());
    }

    public Inbound3270Message process3270Data(ByteBuffer buffer) throws NetworkException {

        String inboundHex = new String(Hex.encodeHex(buffer.array()));
        screen.setDatastreamListenersDirection(inboundHex, DatastreamDirection.INBOUND);

        AbstractCommandCode commandCode = AbstractCommandCode.getCommandCode(buffer.get());
        if (commandCode instanceof CommandWriteStructured) {
            return processStructuredFields((CommandWriteStructured) commandCode, buffer, screen.getCodePage());
        } else {
            return process3270Datastream(commandCode, buffer, screen.getCodePage());
        }
    }

    public static Inbound3270Message process3270Datastream(AbstractCommandCode commandCode, ByteBuffer buffer, Charset codePage)
            throws DatastreamException {

        if (!buffer.hasRemaining()) {
            return new Inbound3270Message(commandCode, null, null);
        }

        WriteControlCharacter writeControlCharacter = new WriteControlCharacter(buffer.get());

        List<AbstractOrder> orders = processOrders(buffer, codePage);

        return new Inbound3270Message(commandCode, writeControlCharacter, orders);
    }

    public static List<AbstractOrder> processOrders(ByteBuffer buffer, Charset codePage) throws DatastreamException {
        OrderText orderText = null;

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        while (buffer.remaining() > 0) {
            byte orderByte = buffer.get();

            if (orderByte > 0x00 && orderByte <= 0x3f) {
                orderText = null;

                AbstractOrder order = null;
                switch (orderByte) {
                    case OrderSetBufferAddress.ID:
                        order = new OrderSetBufferAddress(buffer);
                        break;
                    case OrderRepeatToAddress.ID:
                        order = new OrderRepeatToAddress(buffer, codePage);
                        break;
                    case OrderStartField.ID:
                        order = new OrderStartField(buffer);
                        break;
                    case OrderStartFieldExtended.ID:
                        order = new OrderStartFieldExtended(buffer);
                        break;
                    case OrderSetAttribute.ID:
                        order = new OrderSetAttribute(buffer);
                        break;
                    case OrderModifyField.ID:
                        order = new OrderModifyField(buffer);
                        break;
                    case OrderInsertCursor.ID:
                        order = new OrderInsertCursor();
                        break;
                    case OrderEraseUnprotectedToAddress.ID:
                        order = new OrderEraseUnprotectedToAddress(buffer);
                        break;
                    case OrderNewLine.ID:
                        order = new OrderNewLine();
                        break;
                    case OrderFormFeed.ID:
                        order = new OrderFormFeed();
                        break;
                    case OrderCarrageReturn.ID:
                        order = new OrderCarrageReturn();
                        break;
                    case OrderEndOfMedium.ID:
                        order = new OrderEndOfMedium();
                        break;
                    case OrderGraphicsEscape.ID:
                        order = new OrderGraphicsEscape(buffer);
                        break;
                    default:
                        String byteHex = Hex.encodeHexString(new byte[] { orderByte });
                        logger.trace("Invalid byte detected in datastream, unrecognised byte order or text byte - 0x" + byteHex);
                        order = new OrderText(" ", codePage);
                }
                orders.add(order);
            } else {
                if (orderText == null) {
                    orderText = new OrderText(codePage);
                    orders.add(orderText);
                }
                orderText.append(orderByte);
            }
        }
        return orders;
    }

    public static Inbound3270Message processStructuredFields(CommandWriteStructured commandCode, ByteBuffer buffer, Charset codePage)
            throws NetworkException {
        ArrayList<StructuredField> structuredFields = new ArrayList<>();

        while (buffer.remaining() > 0) {
            int length = buffer.getShort();
            if (length == 0) {
                if (buffer.remaining() == 0) {
                    break;
                } else {
                    length = buffer.remaining() + 2;
                }
            }
            byte[] sfData = new byte[length - 2];
            buffer.get(sfData);

            structuredFields.add(StructuredField.getStructuredField(sfData, codePage));
        }

        return new Inbound3270Message(commandCode, structuredFields);
    }

    public static ByteBuffer readTerminatedMessage(byte header, InputStream messageStream) throws IOException, NetworkException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(header);

        byte[] b = new byte[1];
        boolean lastByteFF = false;
        boolean terminated = false;
        while (messageStream.read(b) == 1) {
            if (b[0] == IAC) {
                if (lastByteFF) {
                    byteArrayOutputStream.write(b);
                    lastByteFF = false;
                } else {
                    lastByteFF = true;
                }
            } else {
                if (b[0] == EOR && lastByteFF) {
                    terminated = true;
                    break;
                }

                byteArrayOutputStream.write(b);
            }
        }

        if (!terminated) {
            throw new NetworkException("3270 message did not terminate with IAC EOR");
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return ByteBuffer.wrap(bytes);
    }

    public ByteBuffer readTerminatedSB(InputStream messageStream) throws IOException, NetworkException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        boolean lastByteFF = false;
        boolean terminated = false;
        Byte b;
        while ((b = readByte(messageStream)) != null) {
            if (b == IAC) {
                if (lastByteFF) {
                    byteArrayOutputStream.write(b);
                    lastByteFF = false;
                } else {
                    lastByteFF = true;
                }
            } else {
                if (b == SE && lastByteFF) {
                    terminated = true;
                    break;
                }

                byteArrayOutputStream.write(b);
            }
        }

        if (!terminated) {
            throw new NetworkException("IAC SB message did not terminate with IAC SE");
        }

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return ByteBuffer.wrap(bytes);
    }

    public boolean isStarted() {
        return this.telnetSessionStarted;
    }

}
