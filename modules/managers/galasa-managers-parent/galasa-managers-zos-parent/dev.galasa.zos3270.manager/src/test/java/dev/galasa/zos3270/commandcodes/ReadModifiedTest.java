/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.commandcodes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.junit.Test;

import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractCommandCode;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.DummySocket;
import dev.galasa.zos3270.util.DummySocketImpl;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class ReadModifiedTest extends Zos3270TestBase {

   @Test 
    public void testGoldenPath() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DummySocket dummySocket = new DummySocket(new DummySocketImpl(bais, baos));
        Network network = new Network("dummy", 0, "dummy") {
            @Override
            public Socket createSocket() throws UnknownHostException, IOException {
                return dummySocket;
            }
        };
        network.connectClient();
        Screen screen = CreateTestScreen(10, 2, network);
        screen.erase();
        
        NetworkThread networkThread = new NetworkThread(null, screen, network, network.getInputStream());
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("1234", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartField(true, false, true, false, false, true)); // Modified - check we get protected fields as well
        orders.add(new OrderText("5678", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, true)); //Modified
        orders.add(new OrderText("ABCD", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(15)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("EFGH", ebcdic));
        screen.processOrders(orders, writeControlCharacter);
        screen.testingSetLastAid(AttentionIdentification.ENTER);
        screen.setCursorPosition(0, 1);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { AbstractCommandCode.READ_MODIFIED });
        Inbound3270Message message = networkThread.process3270Data(buffer);
        screen.processInboundMessage(message);
        
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);

        assertThat(bb.get()).as("Last AID").isEqualTo(AttentionIdentification.ENTER.getKeyValue());
        
        assertThat(bb.get()).as("Cursor byte 1").isEqualTo((byte)0x40);
        assertThat(bb.get()).as("Cursor byte 2").isEqualTo((byte)0x4a);
        
        assertThat(bb.get()).as("SBA Field 3").isEqualTo(OrderSetBufferAddress.ID);
        assertThat(bb.get()).as("Field pos 1").isEqualTo(new BufferAddress(6 /* SF + 1 */).getCharRepresentation()[0]);
        assertThat(bb.get()).as("Field pos 2").isEqualTo(new BufferAddress(6 /* SF + 1 */).getCharRepresentation()[1]);
        assertThat(bb.get()).as("Field char 1").isEqualTo("5678".getBytes(ebcdic)[0]);
        assertThat(bb.get()).as("Field char 2").isEqualTo("5678".getBytes(ebcdic)[1]);
        assertThat(bb.get()).as("Field char 3").isEqualTo("5678".getBytes(ebcdic)[2]);
        assertThat(bb.get()).as("Field char 4").isEqualTo("5678".getBytes(ebcdic)[3]);
        
        assertThat(bb.get()).as("SBA Field 3").isEqualTo(OrderSetBufferAddress.ID);
        assertThat(bb.get()).as("Field pos 1").isEqualTo(new BufferAddress(11 /* SF + 1 */).getCharRepresentation()[0]);
        assertThat(bb.get()).as("Field pos 2").isEqualTo(new BufferAddress(11 /* SF + 1 */).getCharRepresentation()[1]);
        assertThat(bb.get()).as("Field char 1").isEqualTo("ABCD".getBytes(ebcdic)[0]);
        assertThat(bb.get()).as("Field char 2").isEqualTo("ABCD".getBytes(ebcdic)[1]);
        assertThat(bb.get()).as("Field char 3").isEqualTo("ABCD".getBytes(ebcdic)[2]);
        assertThat(bb.get()).as("Field char 4").isEqualTo("ABCD".getBytes(ebcdic)[3]);
        
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.IAC);
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.EOR);
        
        assertThat(bb.remaining()).as("Should be nothing left").isEqualTo(0);
    }
    
   @Test 
    public void testWrappedfield() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DummySocket dummySocket = new DummySocket(new DummySocketImpl(bais, baos));
        Network network = new Network("dummy", 0, "dummy") {
            @Override
            public Socket createSocket() throws UnknownHostException, IOException {
                return dummySocket;
            }
        };
        network.connectClient();
        Screen screen = CreateTestScreen(10, 2, network);
        screen.erase();
        
        NetworkThread networkThread = new NetworkThread(null, screen, network, network.getInputStream());
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartField(true, false, true, false, false, false)); 
        orders.add(new OrderText("5678", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("ABCD", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(15)));
        orders.add(new OrderStartField(true, false, true, false, false, true)); // WRAPPED modified field
        orders.add(new OrderText("EFGH", ebcdic));
        screen.processOrders(orders, writeControlCharacter);
        screen.testingSetLastAid(AttentionIdentification.ENTER);
        screen.setCursorPosition(0, 1);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { AbstractCommandCode.READ_MODIFIED });
        Inbound3270Message message = networkThread.process3270Data(buffer);
        screen.processInboundMessage(message);
        
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);

        assertThat(bb.get()).as("Last AID").isEqualTo(AttentionIdentification.ENTER.getKeyValue());
        
        assertThat(bb.get()).as("Cursor byte 1").isEqualTo((byte)0x40);
        assertThat(bb.get()).as("Cursor byte 2").isEqualTo((byte)0x4a);
        
        assertThat(bb.get()).as("SBA Field 3").isEqualTo(OrderSetBufferAddress.ID);
        assertThat(bb.get()).as("Field pos 1").isEqualTo(new BufferAddress(16 /* SF + 1 */).getCharRepresentation()[0]);
        assertThat(bb.get()).as("Field pos 2").isEqualTo(new BufferAddress(16 /* SF + 1 */).getCharRepresentation()[1]);
        assertThat(bb.get()).as("Field char 1").isEqualTo("EFGH12345".getBytes(ebcdic)[0]);
        assertThat(bb.get()).as("Field char 2").isEqualTo("EFGH12345".getBytes(ebcdic)[1]);
        assertThat(bb.get()).as("Field char 3").isEqualTo("EFGH12345".getBytes(ebcdic)[2]);
        assertThat(bb.get()).as("Field char 4").isEqualTo("EFGH12345".getBytes(ebcdic)[3]);
        assertThat(bb.get()).as("Field char 5").isEqualTo("EFGH12345".getBytes(ebcdic)[4]);
        assertThat(bb.get()).as("Field char 6").isEqualTo("EFGH12345".getBytes(ebcdic)[5]);
        assertThat(bb.get()).as("Field char 7").isEqualTo("EFGH12345".getBytes(ebcdic)[6]);
        assertThat(bb.get()).as("Field char 8").isEqualTo("EFGH12345".getBytes(ebcdic)[7]);
        assertThat(bb.get()).as("Field char 9").isEqualTo("EFGH12345".getBytes(ebcdic)[8]);

        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.IAC);
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.EOR);

        assertThat(bb.remaining()).as("Should be nothing left").isEqualTo(0);
    }

   @Test 
    public void testUnformatted() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DummySocket dummySocket = new DummySocket(new DummySocketImpl(bais, baos));
        Network network = new Network("dummy", 0, "dummy") {
            @Override
            public Socket createSocket() throws UnknownHostException, IOException {
                return dummySocket;
            }
        };
        network.connectClient();
        Screen screen = CreateTestScreen(10, 2, network);
        screen.erase();
        
        NetworkThread networkThread = new NetworkThread(null, screen, network, network.getInputStream());
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10))); // make the first line nulls,  should suppress
        orders.add(new OrderText("1234", ebcdic));
        screen.processOrders(orders, writeControlCharacter);
        screen.testingSetLastAid(AttentionIdentification.ENTER);
        screen.setCursorPosition(0, 1);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { AbstractCommandCode.READ_MODIFIED });
        Inbound3270Message message = networkThread.process3270Data(buffer);
        screen.processInboundMessage(message);
        
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);

        assertThat(bb.get()).as("Last AID").isEqualTo(AttentionIdentification.ENTER.getKeyValue());
        
        assertThat(bb.get()).as("Cursor byte 1").isEqualTo((byte)0x40);
        assertThat(bb.get()).as("Cursor byte 2").isEqualTo((byte)0x4a);
        
        assertThat(bb.get()).as("Unformatted char 1").isEqualTo("1234".getBytes(ebcdic)[0]);
        assertThat(bb.get()).as("Unformatted char 2").isEqualTo("1234".getBytes(ebcdic)[1]);
        assertThat(bb.get()).as("Unformatted char 3").isEqualTo("1234".getBytes(ebcdic)[2]);
        assertThat(bb.get()).as("Unformatted char 4").isEqualTo("1234".getBytes(ebcdic)[3]);
        
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.IAC);
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.EOR);
        
        assertThat(bb.remaining()).as("Should be nothing left").isEqualTo(0);
    }
    
   @Test 
    public void testClear() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DummySocket dummySocket = new DummySocket(new DummySocketImpl(bais, baos));
        Network network = new Network("dummy", 0, "dummy") {
            @Override
            public Socket createSocket() throws UnknownHostException, IOException {
                return dummySocket;
            }
        };
        network.connectClient();
        Screen screen = CreateTestScreen(10, 2, network);
        screen.erase();
        
        NetworkThread networkThread = new NetworkThread(null, screen, network, network.getInputStream());

        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10))); // make the first line nulls,  should suppress
        orders.add(new OrderText("1234", ebcdic));
        screen.processOrders(orders, writeControlCharacter);
        screen.testingSetLastAid(AttentionIdentification.CLEAR);
        screen.setCursorPosition(0, 1);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { AbstractCommandCode.READ_MODIFIED });
        Inbound3270Message message = networkThread.process3270Data(buffer);
        screen.processInboundMessage(message);
        
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);

        assertThat(bb.get()).as("Last AID").isEqualTo(AttentionIdentification.CLEAR.getKeyValue());
        
        //*** The buffer should not be read 
        
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.IAC);
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.EOR);
        
        assertThat(bb.remaining()).as("Should be nothing left").isEqualTo(0);
    }
    
    @Test 
    public void testClearAll() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DummySocket dummySocket = new DummySocket(new DummySocketImpl(bais, baos));
        Network network = new Network("dummy", 0, "dummy") {
            @Override
            public Socket createSocket() throws UnknownHostException, IOException {
                return dummySocket;
            }
        };
        network.connectClient();
        Screen screen = CreateTestScreen(10, 2, network);
        screen.erase();
        
        NetworkThread networkThread = new NetworkThread(null, screen, network, network.getInputStream());
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10))); // make the first line nulls,  should suppress
        orders.add(new OrderText("1234", ebcdic));
        screen.processOrders(orders, writeControlCharacter);
        screen.testingSetLastAid(AttentionIdentification.CLEAR);
        screen.setCursorPosition(0, 1);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { AbstractCommandCode.READ_MODIFIED_ALL });
        Inbound3270Message message = networkThread.process3270Data(buffer);
        screen.processInboundMessage(message);
        
//        String hex = new String(Hex.encodeHex(baos.toByteArray()));
//        System.out.println(hex);
        
        ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);
        assertThat(bb.get()).as("Datastream header").isEqualTo((byte)0);

        assertThat(bb.get()).as("Last AID").isEqualTo(AttentionIdentification.CLEAR.getKeyValue());
      
        assertThat(bb.get()).as("Cursor byte 1").isEqualTo((byte)0x40);
        assertThat(bb.get()).as("Cursor byte 2").isEqualTo((byte)0x4a);
        
        // because ALL was requested,   the buffer is read anyway despite the CLEAR
        assertThat(bb.get()).as("Unformatted char 1").isEqualTo("1234".getBytes(ebcdic)[0]);
        assertThat(bb.get()).as("Unformatted char 2").isEqualTo("1234".getBytes(ebcdic)[1]);
        assertThat(bb.get()).as("Unformatted char 3").isEqualTo("1234".getBytes(ebcdic)[2]);
        assertThat(bb.get()).as("Unformatted char 4").isEqualTo("1234".getBytes(ebcdic)[3]);
        
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.IAC);
        assertThat(bb.get()).as("End of stream").isEqualTo(NetworkThread.EOR);
        
        assertThat(bb.remaining()).as("Should be nothing left").isEqualTo(0);
    }
    
}
