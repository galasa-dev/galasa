/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.terminal;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.CommandWrite;
import dev.galasa.zos3270.internal.datastream.CommandWriteStructured;
import dev.galasa.zos3270.internal.datastream.OrderInsertCursor;
import dev.galasa.zos3270.internal.datastream.OrderRepeatToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.StructuredField;
import dev.galasa.zos3270.internal.datastream.StructuredFieldReadPartition;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class ScreenTest extends Zos3270TestBase {

    @Test
    public void testPrintScreenRendersScreenCorrectly() throws Exception {
        // Given...
        String screenText = "Hello world!";
        int columns = screenText.length();
        int rows = 1;

        Screen screen = CreateTestScreen(columns, rows, null);
        ArrayList<AbstractOrder> orders = new ArrayList<>();

        Charset codePage = Charset.forName("1047");
        orders.add(new OrderText(screenText, codePage));

        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        Inbound3270Message inboundMessage = new Inbound3270Message(new CommandWrite(), writeControlCharacter, orders);
        
        // When...
        screen.processInboundMessage(inboundMessage);
        String printedScreen = screen.printScreen();

        // Then...
        assertThat(printedScreen).isEqualTo(screenText + "\n");
    }

    @Test
    public void testPrintScreenWithMultipleRowsRendersScreenCorrectly() throws Exception {
        // Given...
        String screenText = "1st2nd3rd";
        int columns = 3;
        int rows = 3;

        Screen screen = CreateTestScreen(columns, rows, null);
        ArrayList<AbstractOrder> orders = new ArrayList<>();

        Charset codePage = Charset.forName("1047");
        orders.add(new OrderText(screenText, codePage));

        WriteControlCharacter writeControlCharacter = new WriteControlCharacter();
        Inbound3270Message inboundMessage = new Inbound3270Message(new CommandWrite(), writeControlCharacter, orders);
        
        // When...
        screen.processInboundMessage(inboundMessage);
        String printedScreen = screen.printScreen();

        // Then...
        String expectedScreen =
            "1st\n"+
            "2nd\n"+
            "3rd\n";
        assertThat(printedScreen).isEqualTo(expectedScreen);
    }

    @Test
    public void testPrintScreenWithCursorRendersScreenCorrectly() throws Exception {
        // Given...
        String screenText = "1st   2nd   3rd";
        int columns = 6;
        int rows = 3;

        Network network = new Network("host", 123, "terminalId");
        Screen screen = CreateTestScreen(columns, rows, network);
        ArrayList<AbstractOrder> orders = new ArrayList<>();

        Charset codePage = Charset.forName("1047");

        orders.add(new OrderText(screenText, codePage));

        boolean unlockKeyboard = true;
        WriteControlCharacter writeControlCharacter = new WriteControlCharacter(false, false, false, false, false, false, unlockKeyboard, false);
        Inbound3270Message inboundMessage = new Inbound3270Message(new CommandWrite(), writeControlCharacter, orders);
        
        // When...
        screen.processInboundMessage(inboundMessage);
        screen.cursorDown();
        screen.cursorRight();
        String printedScreen = screen.printScreenTextWithCursor();

        // Then...
        String expectedScreen =
            "=|1st   |\n"+
            "=|2nd   |\n"+
            "^| ^\n"+
            "=|3rd   |\n"+
            "!| Disconnected-Plain Size=3x6 Cursor=7,2x1 Keyboard Unlocked\n";

        assertThat(printedScreen).isEqualTo(expectedScreen);
    }

    @Test
    public void testScreenSize() throws TerminalInterruptedException {
        Assert.assertEquals("default screen size incorrect", 1920, CreateTestScreen().getScreenSize());
        Assert.assertEquals("small screen size incorrect", 20, CreateTestScreen(10, 2, null).getScreenSize());
    }

    @Test
    public void testErase() throws TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        Assert.assertEquals("Erase fields are incorrect",
                "Field(pos=-1,p=false,n=false,d=true,i=false,s=false,m=false,                    )\n",
                screen.printFields());

    }

    @Test
    public void testEraseUsingRA() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);
        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress((char) 0x00, new BufferAddress(0), ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        Assert.assertEquals("Clear fields are incorrect",
                "Field(pos=-1,p=false,n=false,d=true,i=false,s=false,m=false,                    )\n",
                screen.printFields());
    }

    @Test
    public void testOrders() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("Hello", ebcdic));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderInsertCursor());
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(10), ebcdic));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderRepeatToAddress('y', new BufferAddress(14), ebcdic));
        orders.add(new OrderRepeatToAddress('z', new BufferAddress(17), ebcdic));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=true,n=false,d=true,i=false,s=false,m=false,Hello)\n"
                        + "Field(pos=6,p=false,n=false,d=false,i=false,s=false,m=false,XXX)\n"
                        + "Field(pos=10,p=true,n=false,d=true,i=false,s=false,m=false,yyyzzz)\n"
                        + "Field(pos=17,p=true,n=false,d=true,i=false,s=false,m=false,  )\n",
                fields);
    }

    @Test
    public void testOrdersInsertAndTail() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('x', new BufferAddress(19), ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(19)));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=true,n=false,d=true,i=false,s=false,m=false,xxxxxxxxxxxxxxxxxx)\n"
                        + "Field(pos=19,p=true,n=false,d=true,i=false,s=false,m=false,)\n",
                fields);
    }

    @Test
    public void testOrdersJumbled() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(11)));
        orders.add(new OrderText("123456789", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(1)));
        orders.add(new OrderText("abcdefghi", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=0,p=false,n=false,d=false,i=false,s=false,m=false,abcdefghi)\n"
                        + "Field(pos=10,p=false,n=false,d=false,i=false,s=false,m=false,123456789)\n",
                fields);
    }

    @Test
    public void testOrdersReplacedAll() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, false, false, false, false));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(20), ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(20), ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=-1,p=false,n=false,d=true,i=false,s=false,m=false,XXXXXXXXXXXXXXXXXXXX)\n", fields);
    }

    @Test
    public void testOrderReplaceMiddle() throws DatastreamException, TerminalInterruptedException {
        Screen screen = CreateTestScreen(10, 2, null);

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderRepeatToAddress('X', new BufferAddress(10), ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderRepeatToAddress('Y', new BufferAddress(20), ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderRepeatToAddress('Z', new BufferAddress(10), ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        String fields = screen.printFields();
        Assert.assertEquals("Screen layout is incorrect",
                "Field(pos=-1,p=false,n=false,d=true,i=false,s=false,m=false,XXXXXZZZZZYYYYYYYYYY)\n", fields);
    }

  //  @Test
    public void testProcessReadPartitionQueryListEquivalent() throws TerminalInterruptedException, NetworkException {
        Network network = mock(Network.class);
        Screen screen = CreateTestScreen(80, 24, network);

        ByteBuffer buffer = createQueryListBuffer(StructuredFieldReadPartition.REQTYP_EQUIVALENT, (byte) 0x80, (byte) 0x81, (byte) 0xa6, (byte) 0x85);
        Inbound3270Message inbound = NetworkThread.processStructuredFields(new CommandWriteStructured(), buffer, ebcdic);

        screen.processInboundMessage(inbound);

        verify(network, times(1)).sendDatastream(any(byte[].class));
    }

 //   @Test
    public void testProcessReadPartitionQueryListNoSupportedFunctions() throws TerminalInterruptedException, NetworkException {
        Network network = mock(Network.class);
        Screen screen = CreateTestScreen(80, 24, network);

        // query "Graphic Color" & "Graphic Symbol Sets" which are unsupported
        ByteBuffer buffer = createQueryListBuffer(StructuredFieldReadPartition.REQTYP_LIST, (byte) 0xb4, (byte) 0xb6);
        Inbound3270Message inbound = NetworkThread.processStructuredFields(new CommandWriteStructured(), buffer, ebcdic);

        screen.processInboundMessage(inbound);

        verify(network, times(1)).sendDatastream(new byte[]{
                (byte) 0x88, // structured field AID
                (byte) 0x00, (byte) 0x08, // Summary reply length
                (byte) 0x81, (byte) 0x80, (byte) 0x80, (byte) 0x81, (byte) 0xA6, (byte) 0x85, // Summary reply
                (byte) 0x00, (byte) 0x04, // Null reply length
                (byte) 0x81, (byte) 0xFF // Null reply
        });
    }

    private ByteBuffer createQueryListBuffer(byte reqtyp, byte... qcodes) {
        int length = 6 + qcodes.length;
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putShort((short) length);
        buffer.put(StructuredField.SF_READ_PARTITION);
        buffer.put((byte) -1);
        buffer.put(StructuredFieldReadPartition.QUERY_LIST);
        buffer.put(reqtyp);
        buffer.put(qcodes); 
        buffer.flip();
        return buffer;
    }

}
