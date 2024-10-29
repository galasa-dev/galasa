/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.terminal;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.TextNotFoundException;
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

public class FindTextTest extends Zos3270TestBase {

    @Test
    public void testSimple() throws Exception {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("Find this message here", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));


        try {
            screen.waitForTextInField("message", 0);
        } catch(Throwable t) {
            throw new Exception("Should have found message and not thrown exception",t);
        }

        try {
            screen.waitForTextInField("notpresent", 0);
            Assert.fail("Should have thrown TextNotFoundException");
        } catch(TextNotFoundException e) {
        } catch(Throwable t) {
            throw new Exception("Should not thrown exception other than TextNotFoundException",t);
        }
    }

    @Test
    public void testOk() throws Exception {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("Find this message here", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));


        try {
            screen.waitForTextInField(new String[]{"message"}, null, 0);
        } catch(Throwable t) {
            throw new Exception("Should have found message and not thrown exception",t);
        }

        try {
            screen.waitForTextInField(new String[]{"notpresent"}, null, 0);
            Assert.fail("Should have thrown TextNotFoundException");
        } catch(TextNotFoundException e) {
        } catch(Throwable t) {
            throw new Exception("Should not thrown exception other than TextNotFoundException",t);
        }
    }

    @Test
    public void testError() throws Exception {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("Find this message here", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));


        try {
            screen.waitForTextInField(new String[]{"notpresent"}, new String[]{"message"}, 0);
            Assert.fail("Should have thrown ErrorTextFoundException");
        } catch (ErrorTextFoundException e) {
        } catch(Throwable t) {
            throw new Exception("Should not thrown exception other than ErrorTextFoundException",t);
        }

        try {
            screen.waitForTextInField(new String[]{"this"}, new String[]{"message"}, 0);
            Assert.fail("Should have thrown ErrorTextFoundException even though an ok text is present");
        } catch (ErrorTextFoundException e) {
        } catch(Throwable t) {
            throw new Exception("Should not thrown exception other than ErrorTextFoundException",t);
        }

        try {
            screen.waitForTextInField(new String[]{"message"}, new String[]{"notpresent"}, 0);
        } catch(Throwable t) {
            throw new Exception("Should have found message and not thrown exception",t);
        }
    }

    @Test
    public void testMultiSearch() throws Exception {
        Screen screen = CreateTestScreen(10, 2, null);
        screen.erase();

        ArrayList<AbstractOrder> orders = new ArrayList<>();
        orders.add(new OrderSetBufferAddress(new BufferAddress(0)));
        orders.add(new OrderStartField(false, false, true, false, false, false));
        orders.add(new OrderText("Find this message here", ebcdic));

        screen.processInboundMessage(new Inbound3270Message(new CommandEraseWrite(),
                new WriteControlCharacter(false, false, false, false, false, false, true, true), orders));


        try {
            screen.waitForTextInField(new String[]{"notpresent", "stillnotpresent"}, new String[]{"notpresent", "message"}, 0);
            Assert.fail("Should have thrown ErrorTextFoundException");
        } catch (ErrorTextFoundException e) {
        } catch(Throwable t) {
            throw new Exception("Should not thrown exception other than ErrorTextFoundException",t);
        }

        try {
            screen.waitForTextInField(new String[]{"notpresent", "message"}, new String[]{"stillnotpresent", "notpresent"}, 0);
        } catch(Throwable t) {
            throw new Exception("Should have found message and not thrown exception",t);
        }
    }

}
