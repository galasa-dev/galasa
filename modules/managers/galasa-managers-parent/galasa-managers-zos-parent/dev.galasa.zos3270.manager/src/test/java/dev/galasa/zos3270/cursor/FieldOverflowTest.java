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
 * Ensure if type over the end of a field,  the cursor will skip to the next unprotected field
 * 
 *  
 *
 */
public class FieldOverflowTest extends Zos3270TestBase {
    
    /**
     * Test with two fields
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
        orders.add(new OrderText("     ", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(6)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("     ", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(16)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        
        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        screen.setCursorPosition(1);
        
        screen.type("123456");
        
        assertThat(screen.getCursor()).as("Cursor should be at 12, should have filled the first field and the first char of the second").isEqualTo(12);
        
        assertThat(screen.getFieldAt(1, 0).getFieldWithoutNulls()).as("12345 should be in the first field").isEqualTo("12345");
        assertThat(screen.getFieldAt(1, 1).getFieldWithoutNulls()).as("6     should be in the first field").isEqualTo("6    ");

    }
    
}
