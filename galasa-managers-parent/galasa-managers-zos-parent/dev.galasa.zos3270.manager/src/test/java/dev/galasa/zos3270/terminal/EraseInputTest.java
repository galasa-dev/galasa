/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.terminal;

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


public class EraseInputTest extends Zos3270TestBase {

    /**
     * Test golden path with 4 fields
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
        orders.add(new OrderText("1234", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("5678", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("ABCD", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(15)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("EFGH", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("1234 should be in the first field before test").isEqualTo("1234");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field before test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCD should be in the third field before test").isEqualTo("ABCD");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("EFGH should be in the fourth field before test").isEqualTo("EFGH");

        screen.eraseInput();
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("nulls should be in the first field after test").isEqualTo("    ");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field after test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("nulls should be in the third field after test").isEqualTo("    ");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("EFGH should be in the fourth field after test").isEqualTo("EFGH");
    }

    /**
     * Test full protected
     * 
     * @throws Exception
     */
    @Test 
    public void testProtected() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("1234", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(5)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("5678", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(10)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("ABCD", ebcdic));
        orders.add(new OrderSetBufferAddress(new BufferAddress(15)));
        orders.add(new OrderStartField(true, false, true, false, false, false));
        orders.add(new OrderText("EFGH", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("1234 should be in the first field before test").isEqualTo("1234");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field before test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCD should be in the third field before test").isEqualTo("ABCD");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("EFGH should be in the fourth field before test").isEqualTo("EFGH");

        screen.eraseInput();
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("1234 should be in the first field after test").isEqualTo("1234");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field after test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCD should be in the third field after test").isEqualTo("ABCD");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("EFGH should be in the fourth field after test").isEqualTo("EFGH");

    }

    /**
     * Test unformatted
     * 
     * @throws Exception
     */
    @Test 
    public void testUnformatted() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderText("1234567890ABCDEFGHIJ", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("1234567890ABCDEFGHIJ should be in the first field before test").isEqualTo("1234567890ABCDEFGHIJ");

        screen.eraseInput();
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("nulls should be in the first field after test").isEqualTo("                    ");

    }

    /**
     * Test wrapped
     * 
     * @throws Exception
     */
    @Test 
    public void testWrapped() throws Exception {

        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

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
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("EFGH", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));

        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("12345 should be in the first field before test").isEqualTo("12345");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field before test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("ABCD should be in the third field before test").isEqualTo("ABCD");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("EFGH should be in the fourth field before test").isEqualTo("EFGH");

        screen.eraseInput();
        
        assertThat(screen.getFieldAt(0, 0).getFieldWithoutNulls()).as("nulls should be in the first field after test").isEqualTo("     ");
        assertThat(screen.getFieldAt(5, 0).getFieldWithoutNulls()).as("5678 should be in the second field after test").isEqualTo("5678");
        assertThat(screen.getFieldAt(0, 1).getFieldWithoutNulls()).as("nulls should be in the third field after test").isEqualTo("    ");
        assertThat(screen.getFieldAt(5, 1).getFieldWithoutNulls()).as("nulls should be in the fourth field after test").isEqualTo("    ");
    }

}
