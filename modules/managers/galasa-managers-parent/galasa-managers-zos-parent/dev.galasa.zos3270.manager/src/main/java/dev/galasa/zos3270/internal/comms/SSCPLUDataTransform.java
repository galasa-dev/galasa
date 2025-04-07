/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.comms;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import dev.galasa.zos3270.IDatastreamListener.DatastreamDirection;
import dev.galasa.zos3270.internal.datastream.AbstractOrder;
import dev.galasa.zos3270.internal.datastream.OrderText;
import dev.galasa.zos3270.internal.datastream.WriteControlCharacter;
import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;

/**
 * Provides utilities to process and transform SSCP-LU-DATA datastreams into
 * 3270 datastreams that the Galasa 3270 manager can recognise.
 */
public class SSCPLUDataTransform {

    private Screen screen;

    public SSCPLUDataTransform(Screen screen) {
        this.screen = screen;
    }

    public Inbound3270Message processSSCPLUData(ByteBuffer buffer) throws NetworkException {

        String inboundHex = new String(Hex.encodeHex(buffer.array()));
        screen.setDatastreamListenersDirection(inboundHex, DatastreamDirection.INBOUND);

        return processSSCPLUDatastream(buffer, screen.getCodePage());
    }

    private Inbound3270Message processSSCPLUDatastream(ByteBuffer buffer, Charset codePage) throws DatastreamException {

        Inbound3270Message messageToReturn;
        if (!buffer.hasRemaining()) {
            messageToReturn = new Inbound3270Message(null, null, null);
        } else {
            
            // SSCP-LU-DATA datastreams don't seem to have a write control character,
            // so make a basic one that keeps the keyboard unlocked...
            boolean unlockKeyboard = true;
            WriteControlCharacter writeControlCharacter = new WriteControlCharacter(
                false,
                false,
                false,
                false,
                false,
                false,
                unlockKeyboard,
                false
            );

            // The display screen in an SSCP-LU session is unformatted, so just 
            // convert the datastream into a list of 3270 text orders
            List<AbstractOrder> orders = convertToTextOrders(buffer, codePage);
            messageToReturn = new Inbound3270Message(null, writeControlCharacter, orders);
        }

        return messageToReturn;
    }

    private List<AbstractOrder> convertToTextOrders(ByteBuffer buffer, Charset codePage) throws DatastreamException {
        List<AbstractOrder> orders = new ArrayList<>();
        while (buffer.remaining() > 0) {
            byte currentByte = buffer.get();

            OrderText orderText = new OrderText(codePage);
            orders.add(orderText);

            orderText.append(currentByte);
        }
        return orders;
    }
}
