/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.datastream;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.OrderEraseUnprotectedToAddress;
import dev.galasa.zos3270.internal.datastream.OrderSetBufferAddress;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class EraseUnprotectedToAddressTest extends Zos3270TestBase {

    @Test
    public void testEraseAllUnprotected() throws IOException, DecoderException, NetworkException, TerminalInterruptedException {
        String vampHex = "f140114040124040";
        byte[] stream = Hex.decodeHex(vampHex);
        ByteBuffer buffer = ByteBuffer.wrap(stream);

        NetworkThread networkThread = new NetworkThread(null, CreateTestScreen(), null, null);
        
        List<AbstractOrder> orders = networkThread.process3270Data(buffer).getOrders();
        assertThat(orders.size()).as("Should have returned 2 orders, SBA, EUA").isEqualTo(2);
        
        AbstractOrder osba = orders.get(0);
        assertThat(osba instanceof OrderSetBufferAddress).as("Should be SBA").isTrue();
        OrderSetBufferAddress sba = (OrderSetBufferAddress) osba; 
        assertThat(sba.getBufferAddress()).as("Address should be 0").isEqualTo(0);
        
        AbstractOrder oeua = orders.get(1);
        assertThat(oeua instanceof OrderEraseUnprotectedToAddress).as("Should be EUA").isTrue();
        OrderEraseUnprotectedToAddress eua = (OrderEraseUnprotectedToAddress) oeua; 
        assertThat(eua.getBufferAddress()).as("End Address should be 0").isEqualTo(0);
        
    }

}
