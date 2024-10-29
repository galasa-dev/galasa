/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.BufferAddress;
import dev.galasa.zos3270.internal.datastream.CommandEraseWrite;
import dev.galasa.zos3270.internal.datastream.CommandWrite;
import dev.galasa.zos3270.internal.datastream.OrderEraseUnprotectedToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

/**
 * Test the Erase All Unprotected order 
 * 
 *  
 *
 */
public class EraseAllUnprotectedTest extends Zos3270TestBase {

    /**
     * Test with two fields
     * 
     * @throws Exception
     */
    @Test 
    public void testEuaGoldPath() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABC", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("67890", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("67890 should be in the third field before test").isEqualTo("67890");

        orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderEraseUnprotectedToAddress(new BufferAddress(15)));
        screen.processInboundMessage(new Inbound3270Message(new CommandWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        // should have erased the 12345 and 6789 but left the 0
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("null should be in the first field before test").isEqualTo("     ");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("0 should be in the third field before test").isEqualTo("    0");

    }
    
    /**
     * Test with two fields
     * 
     * @throws Exception
     */
    @Test 
    public void testEuaFullScreen() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABC", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("67890", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("67890 should be in the third field before test").isEqualTo("67890");

        orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderEraseUnprotectedToAddress(new BufferAddress(0)));
        screen.processInboundMessage(new Inbound3270Message(new CommandWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        // should have erased the 12345 and 6789 but left the 0
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("null should be in the first field before test").isEqualTo("     ");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("null should be in the third field before test").isEqualTo("     ");

    }
    
    /**
     * Test with two fields
     * 
     * @throws Exception
     */
    @Test 
    public void testEuaMidField() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABC", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("67890", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("67890 should be in the third field before test").isEqualTo("67890");

        orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(3)));
        orders.add(new OrderEraseUnprotectedToAddress(new BufferAddress(15)));
        screen.processInboundMessage(new Inbound3270Message(new CommandWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        // should have erased the 45 and 6789 but left the 123 and 0
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12 should be in the first field before test").isEqualTo("12   ");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("0 should be in the third field before test").isEqualTo("    0");

    }
    
    /**
     * Test with two fields
     * 
     * @throws Exception
     */
    @Test 
    public void testEuaWrap() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABC", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("67890", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("67890 should be in the third field before test").isEqualTo("67890");

        orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(12)));
        orders.add(new OrderEraseUnprotectedToAddress(new BufferAddress(3)));
        screen.processInboundMessage(new Inbound3270Message(new CommandWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        // should have erased the 12 and 7890
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("345 should be in the first field before test").isEqualTo("  345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("6 should be in the third field before test").isEqualTo("6    ");

    }
    
    /**
     * Test starting in protected field
     * 
     * @throws Exception
     */
    @Test 
    public void testEuaProtected() throws Exception {
        
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("12345", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABC", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("67890", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("67890 should be in the third field before test").isEqualTo("67890");

        orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(8)));
        orders.add(new OrderEraseUnprotectedToAddress(new BufferAddress(3)));
        screen.processInboundMessage(new Inbound3270Message(new CommandWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));
        
        // should have erased the 12 and 67890
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("345 should be in the first field before test").isEqualTo("  345");
        assertThat(screen.getFieldAt(6, 0).getFieldWithoutNulls()).as("ABC should be in the second field before test").isEqualTo("ABC");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("null should be in the third field before test").isEqualTo("     ");

    }
    
}
