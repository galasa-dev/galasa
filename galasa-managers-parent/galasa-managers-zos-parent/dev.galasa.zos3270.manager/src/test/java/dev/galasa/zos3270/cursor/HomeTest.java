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
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

/**
 * Test the home function
 * 
 *  
 *
 */
public class HomeTest extends Zos3270TestBase {
    
    /**
     * Test what happens in a empty screen with no fields, should position at 0
     * 
     * @throws Exception
     */
    @Test 
    public void emptyScreen() throws Exception {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(2);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 0, empty screen").isEqualTo(0);
        
    }


    /**
     * Test with single field,  but the cursor offset it in,  so 
     * should return at the beginning of the same field
     * 
     * @throws Exception
     */
    @Test 
    public void testSameFieldCursorOffset() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(10);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 1, offsetted in same field").isEqualTo(1);
        
    }
    
    /**
     * Test with a single field with the cursor already at the beginning, should stay at same place
     * 
     * @throws Exception
     */
    @Test 
    public void testSameFieldCursorAtBeginning() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(1);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 1, start of same field").isEqualTo(1);
        
    }
    
    /**
     * Test with multiple fields, should move to first field
     * 
     * @throws Exception
     */
    @Test 
    public void testDifferentField() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(15);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 1, start of next field").isEqualTo(1);
        
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
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 0, as field is wrapped").isEqualTo(0);
        
    }
    
    /**
     * Test what happens with a fully protected screen,  should move to 0
     * 
     * @throws Exception
     */
    @Test 
    public void testProtectedScreen() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(true, false, true, false, false, false));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(15);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 0, start of screen as no unprotected fields").isEqualTo(0);
        
    }
    
    /**
     * Test a weird screen full of unprotected fields of zero length, should position origin
     * 
     * @throws Exception
     */
    @Test 
    public void testUnprotectedZeroLength() throws Exception {
        
        Screen screen = CreateTestScreen(5, 1, null);
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

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(3);
        
        screen.home();
        
        assertThat(screen.getCursor()).as("Cursor should be at 0, start of screen as no unprotected fields with a char").isEqualTo(0);
        
    }
    
}
