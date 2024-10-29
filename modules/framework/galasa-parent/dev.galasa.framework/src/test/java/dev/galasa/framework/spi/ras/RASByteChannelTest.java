/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import org.junit.Assert;
import org.junit.Test;

import dev.galasa.framework.spi.ras.ResultArchiveStoreByteChannel;

public class RASByteChannelTest {

    @Test
    public void testWrite() throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(56565);
        buffer.flip();

        final ResultArchiveStoreByteChannel bc = new ResultArchiveStoreByteChannel();
        Assert.assertTrue("should be open", bc.isOpen());
        Assert.assertEquals("should have wrote 4 bytes", 4, bc.write(buffer));
        Assert.assertEquals("should have wrote 4 bytes", 4, bc.size());
        Assert.assertEquals("should have wrote 4 bytes", 4, bc.position());

        bc.close();
        Assert.assertFalse("should be closed", bc.isOpen());
    }

    @Test
    public void testClosedChecks() throws IOException {
        final ResultArchiveStoreByteChannel bc = new ResultArchiveStoreByteChannel();
        bc.close();
        Assert.assertFalse("Should be closed", bc.isOpen());

        try {
            bc.read(ByteBuffer.allocate(1));
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }

        try {
            bc.write(ByteBuffer.allocate(1));
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }

        try {
            bc.position();
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }

        try {
            bc.position(1);
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }

        try {
            bc.size();
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }

        try {
            bc.truncate(1);
            fail("Should have failed with closed exception");
        } catch (final ClosedChannelException e) {
        }
    }

    @Test
    public void testNotAvailable() throws IOException {
        final ResultArchiveStoreByteChannel bc = new ResultArchiveStoreByteChannel();

        try {
            bc.position(1);
            fail("Should have failed with unavailable");
        } catch (final IOException e) {
            Assert.assertEquals("incorrect message", "Not available in dummy RAS channel", e.getMessage());
        }

        try {
            bc.read(ByteBuffer.allocate(1));
            fail("Should have failed with unavailable");
        } catch (final IOException e) {
            Assert.assertEquals("incorrect message", "Not available in dummy RAS channel", e.getMessage());
        }

        try {
            bc.truncate(1);
            fail("Should have failed with unavailable");
        } catch (final IOException e) {
            Assert.assertEquals("incorrect message", "Not available in dummy RAS channel", e.getMessage());
        }
    }

}
