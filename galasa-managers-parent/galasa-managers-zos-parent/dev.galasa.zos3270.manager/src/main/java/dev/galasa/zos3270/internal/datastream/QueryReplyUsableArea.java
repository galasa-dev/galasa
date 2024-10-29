/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.Screen;

public class QueryReplyUsableArea extends AbstractQueryReply {

    private static final byte   USUABLE_AREA = (byte) 0x81;

    private final int           cellX;
    private final int           cellY;
    private final int           maxBuffer;

    private static final byte   UNITS        = 0x01;
    private static final byte[] XR           = new byte[] { 0x00, 0x0a, 0x02, (byte) 0xe5 };
    private static final byte[] YR           = new byte[] { 0x00, 0x02, 0x00, (byte) 0x6f };
    private static final byte   AW           = 0x09;
    private static final byte   AH           = 0x0c;

    public QueryReplyUsableArea(Screen screen) {
        this.cellX = screen.getNoOfColumns();
        this.cellY = screen.getNoOfRows();
        
        int primaryBuffer = screen.getPrimaryColumns() * screen.getPrimaryRows();
        int alternateBuffer = screen.getAlternateColumns() * screen.getAlternateRows();
        
        if (primaryBuffer >= alternateBuffer) {
            this.maxBuffer = primaryBuffer;
        } else {
            this.maxBuffer = alternateBuffer;
        }
        
    }

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(23);
        buffer.putShort((short) 23);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(USUABLE_AREA);
        buffer.put((byte) 0x01); // *** 12/14 bit addressing allowed
        buffer.put((byte) 0x00); // *** Variable cells nosupported, matrix chars, cell units
        buffer.putShort((short) cellX);
        buffer.putShort((short) cellY);
        buffer.put(UNITS);
        buffer.put(XR);
        buffer.put(YR);
        buffer.put(AW);
        buffer.put(AH);
        buffer.putShort((short) this.maxBuffer);
        return buffer.array();
    }

    @Override
    public byte getID() {
        return USUABLE_AREA;
    }

}
