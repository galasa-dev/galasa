/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.Colour;

public class QueryReplyColor extends AbstractQueryReply {

    private static final byte COLOR = (byte) 0x86;

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(22);
        buffer.putShort((short) 22);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(COLOR);
        buffer.put((byte) 0); // *** Flags
        
        buffer.put((byte)8); // Number of color combinations
        
        putColor(buffer, Colour.DEFAULT.getCode(),   Colour.GREEN.getCode());
        putColor(buffer, Colour.BLUE.getCode(),      Colour.BLUE.getCode());
        putColor(buffer, Colour.RED.getCode(),       Colour.RED.getCode());
        putColor(buffer, Colour.PINK.getCode(),      Colour.PINK.getCode());
        putColor(buffer, Colour.GREEN.getCode(),     Colour.GREEN.getCode());
        putColor(buffer, Colour.TURQUOISE.getCode(), Colour.TURQUOISE.getCode());
        putColor(buffer, Colour.YELLOW.getCode(),    Colour.YELLOW.getCode());
        putColor(buffer, Colour.NEUTRAL.getCode(),   Colour.NEUTRAL.getCode());

        return buffer.array();
    }

    private void putColor(ByteBuffer buffer, byte colorAcceptedValue, byte colorIdentity) {
        buffer.put(colorAcceptedValue);
        buffer.put(colorIdentity);
        
        return;
    }

    @Override
    public byte getID() {
        return COLOR;
    }

}
