/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class VampScreenTest extends Zos3270TestBase {

    @Test
    public void testVampScreen() throws IOException, DecoderException, NetworkException, TerminalInterruptedException {
        URL vampFile = getClass().getClassLoader().getResource("vampstream.txt");
        String vampHex = IOUtils.toString(vampFile.openStream(), "utf-8");
        byte[] stream = Hex.decodeHex(vampHex);
        ByteBuffer buffer = ByteBuffer.wrap(stream);

        NetworkThread networkThread = new NetworkThread(null, CreateTestScreen(), null, null);
        
        List<AbstractOrder> orders = networkThread.process3270Data(buffer).getOrders();
        Assert.assertEquals("Count of orders is incorrect", 225, orders.size());
    }

}
