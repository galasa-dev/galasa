/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class QueryReplyHighlite extends AbstractQueryReply {

    private static final byte HIGHLITE = (byte) 0x87;

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(13);
        buffer.putShort((short) 13);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(HIGHLITE);
        
        buffer.put((byte)4); // Number of color combinations
        
        putColor(buffer, (byte)0, (byte)0xf0);
        putColor(buffer, (byte)0xf1, (byte)0xf1);
        putColor(buffer, (byte)0xf2, (byte)0xf2);
        putColor(buffer, (byte)0xf4, (byte)0xf4);

        return buffer.array();
    }

    private void putColor(ByteBuffer buffer, byte colorAcceptedValue, byte colorIdentity) {
        buffer.put(colorAcceptedValue);
        buffer.put(colorIdentity);
        
        return;
    }

    @Override
    public byte getID() {
        return HIGHLITE;
    }

}
