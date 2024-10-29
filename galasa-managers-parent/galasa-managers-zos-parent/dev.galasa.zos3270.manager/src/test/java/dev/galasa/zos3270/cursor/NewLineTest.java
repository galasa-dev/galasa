/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.cursor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

/**
 * Test the newline function
 * 
 *  
 *
 */
public class NewLineTest extends Zos3270TestBase {
    
    /**
     * Test what happens in a empty screen with no fields, should position to the next line
     * 
     * @throws Exception
     */
    @Test 
    public void emptyScreen() throws Exception {
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(2);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 10, empty screen").isEqualTo(10);
        
    }


    /**
     * Test with single field,  but the cursor at the start, should move a line down
     * 
     * @throws Exception
     */
    @Test 
    public void testSameFieldStartingAtBegginningOfField() throws Exception {
        
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(1);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 10, offsetted in same field").isEqualTo(10);
        
    }
    
    /**
     * Test with single field,  but the cursor offset it in,  so 
     * should move to the next line in the field
     * 
     * @throws Exception
     */
    @Test 
    public void testSameFieldStartingALineDown() throws Exception {
        
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(10);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 20, offsetted in same field").isEqualTo(20);
        
    }
    
    /**
     * Test with single field,  but the cursor on last line of screen,  should move to the top
     * 
     * @throws Exception
     */
    @Test 
    public void testSameFieldStartingBottom() throws Exception {
        
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(35);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 1, offsetted in same field").isEqualTo(1);
        
    }
    
    /**
     * Test with multiple fields, should move to second field
     * 
     * @throws Exception
     */
    @Test 
    public void testDifferentField() throws Exception {
        
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("UField1", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("PField1", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(20)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("UField2", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(30)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("UField3", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(1);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 21, start of next field").isEqualTo(21);
        
    }
    
    /**
     * Test with wrapped field,  should move to the 0
     * 
     * @throws Exception
     */
    @Test 
    public void testWrappedField() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(11);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 0, as field is wrapped").isEqualTo(0);
        
    }
    
    /**
     * Test what happens with a fully protected screen,  should move to 0
     * 
     * @throws Exception
     */
    @Test 
    public void testProtectedScreen() throws Exception {
        
        Screen screen = CreateTestScreen(10, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(15);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 20, move to next line as no unprotected fields").isEqualTo(20);
        
    }
    
    /**
     * Test a weird screen full of unprotected fields of zero length, should position origin
     * 
     * @throws Exception
     */
    @Test 
    public void testUnprotectedZeroLength() throws Exception {
        
        Screen screen = CreateTestScreen(2, 4, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(1)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(2)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(3)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(4)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(7)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(2);
        
        screen.newLine();
        
        assertThat(screen.getCursor()).as("Cursor should be at 4, move to next line as no unprotected fields with a char").isEqualTo(4);
        
    }
    
}
