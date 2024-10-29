/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.internal.datastream.OrderStartField;
import dev.galasa.zos3270.spi.DatastreamException;

public class StartFieldTest {

    @Test
    public void testProtected() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x20);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertTrue("Should be protected", sf.isFieldProtected());

        String shouldbe = "SF(Protected Alphanumeric Display Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testUnProtected() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0xdf);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertFalse("Should be unprotected", sf.isFieldProtected());

        String shouldbe = "SF(Unprotected Numeric Nondisplay Modified)";
        Assert.assertEquals("SF not translating correctly", shouldbe, sf.toString());
    }

    @Test
    public void testNumeric() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x10);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertTrue("Should be numeric", sf.isFieldNumeric());

        String shouldbe = "SF(Unprotected Numeric Display Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testAlphanumeric() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0xef);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertFalse("Should be alphanumeric", sf.isFieldNumeric());

        String shouldbe = "SF(Protected Alphanumeric Nondisplay Modified)";
        Assert.assertEquals("SF not translating correctly", shouldbe, sf.toString());
    }

    @Test
    public void testDisplayNonSelpen() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x00);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertTrue("Should be Display", sf.isFieldDisplay());
        Assert.assertFalse("Should not be Intense", sf.isFieldIntenseDisplay());
        Assert.assertFalse("Should not be selpen", sf.isFieldSelectorPen());

        String shouldbe = "SF(Unprotected Alphanumeric Display Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testDisplaySelpen() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x04);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertTrue("Should be Display", sf.isFieldDisplay());
        Assert.assertFalse("Should not be Intense", sf.isFieldIntenseDisplay());
        Assert.assertTrue("Should be selpen", sf.isFieldSelectorPen());

        String shouldbe = "SF(Unprotected Alphanumeric Display SelectorPen Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testIntenseSelpen() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x08);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertFalse("Should not be Display", sf.isFieldDisplay());
        Assert.assertTrue("Should be Intense", sf.isFieldIntenseDisplay());
        Assert.assertTrue("Should be selpen", sf.isFieldSelectorPen());

        String shouldbe = "SF(Unprotected Alphanumeric Intense SelectorPen Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testNoDisplayNoSelpen() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x0C);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertFalse("Should not be Display", sf.isFieldDisplay());
        Assert.assertFalse("Should not be Intense", sf.isFieldIntenseDisplay());
        Assert.assertFalse("Should not be selpen", sf.isFieldSelectorPen());

        String shouldbe = "SF(Unprotected Alphanumeric Nondisplay Unmodified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testModified() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0x01);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertTrue("Should be modified", sf.isFieldModifed());

        String shouldbe = "SF(Unprotected Alphanumeric Display Modified)";
        Assert.assertEquals("SF not translating correct to " + shouldbe, shouldbe, sf.toString());
    }

    @Test
    public void testUnmodified() throws DatastreamException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0xfe);
        buffer.flip();

        OrderStartField sf = new OrderStartField(buffer);

        Assert.assertFalse("Should be unmodified", sf.isFieldModifed());

        String shouldbe = "SF(Protected Numeric Nondisplay Unmodified)";
        Assert.assertEquals("SF not translating correctly", shouldbe, sf.toString());
    }

}
