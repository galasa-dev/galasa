/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.Screen;

public class QueryReplyImplicitPartition extends AbstractQueryReply {

    private static final byte IMPLICIT_PARTITION = (byte) 0xa6;

    private final int         primaryX;
    private final int         primaryY;
    private final int         alternateX;
    private final int         alternateY;

    public QueryReplyImplicitPartition(Screen screen) {
        this.primaryX = screen.getNoOfColumns();
        this.primaryY = screen.getNoOfRows();
        
        int aX = screen.getAlternateColumns();
        int aY = screen.getAlternateRows();
        
        if (aX < 1 || aY < 1) {
            this.alternateX = this.primaryX;
            this.alternateY = this.primaryY;
        } else {
            this.alternateX = aX;
            this.alternateY = aY;
        }
    }

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(17);
        buffer.putShort((short) 17);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(IMPLICIT_PARTITION);
        buffer.putShort((short) 0); // *** Flags

        buffer.put((byte) 11); // *** Length of self defining parameter
        buffer.put((byte) 1); // *** Implicit Partition Size
        buffer.put((byte) 0); // *** Flags
        buffer.putShort((short) primaryX);
        buffer.putShort((short) primaryY);
        buffer.putShort((short) alternateX);
        buffer.putShort((short) alternateY);
        return buffer.array();
    }

    @Override
    public byte getID() {
        return IMPLICIT_PARTITION;
    }

}
