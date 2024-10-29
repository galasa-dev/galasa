/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import dev.galasa.zos3270.FieldNotFoundException;
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
 * Test the backSpace function
 * 
 *  
 *
 */
public class BackSpaceTest extends Zos3270TestBase {

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

        screen.setCursorPosition(0);

        screen.backSpace();

        assertThat(screen.getCursor()).as("Cursor should be at 0, empty screen").isEqualTo(0);

        assertThat(screen.retrieveFlatScreen()).as("should be an empty screen").isEqualTo("                    ");

    }


    /**
     * Test golden path with 2 fields
     * 
     * @throws Exception
     */
    @Test 
    public void testGoldenPath() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("123456789", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("ABCDEFGHI", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("123456789 should be in the first field before test").isEqualTo("123456789");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCDEFGHI should be in the second field before test").isEqualTo("ABCDEFGHI");

        screen.setCursorPosition(3);

        screen.backSpace();

        assertThat(screen.getCursor()).as("Cursor should be at 2").isEqualTo(2);
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("13456789 should be in the first field after test").isEqualTo("13456789 ");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCDEFGHI should be in the second field after test").isEqualTo("ABCDEFGHI");        
    }

    /**
     * Test position on SF or field pos 1
     * 
     * @throws Exception
     */
    @Test 
    public void testProtectedScreen() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("123456789", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("ABCDEFGHI", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("123456789 should be in the first field before test").isEqualTo("123456789");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCDEFGHI should be in the second field before test").isEqualTo("ABCDEFGHI");

        screen.setCursorPosition(0);

        try {
            screen.backSpace();
            fail("Should have failed as cursor is on SF");
        } catch(FieldNotFoundException e) {
            // will be locked
        }

        screen.setCursorPosition(1);

        try {
            screen.backSpace();
            fail("Should have failed as cursor is on field pos 0");
        } catch(FieldNotFoundException e) {
            // will be locked
        }
    }
    
    
    /**
     * Test golden path with 2 fields
     * 
     * @throws Exception
     */
    @Test 
    public void testSingleField() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("123456789ABCDEFGHIJ", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("123456789ABCDEFGHIJ should be in the first field before test").isEqualTo("123456789ABCDEFGHIJ");

        screen.setCursorPosition(19);

        screen.backSpace();

        assertThat(screen.getCursor()).as("Cursor should be at 18").isEqualTo(18);
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("123456789ABCDEFGHJ should be in the first field after test").isEqualTo("123456789ABCDEFGHJ ");

        screen.setCursorPosition(2);

        screen.backSpace();

        assertThat(screen.getCursor()).as("Cursor should be at 1").isEqualTo(1);
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("23456789ABCDEFGHJ should be in the first field after test").isEqualTo("23456789ABCDEFGHJ  ");
    }


}
