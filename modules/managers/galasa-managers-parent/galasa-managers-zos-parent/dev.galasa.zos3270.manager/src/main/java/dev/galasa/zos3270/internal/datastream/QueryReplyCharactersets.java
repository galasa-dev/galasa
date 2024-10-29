/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class QueryReplyCharactersets extends AbstractQueryReply {

    private static final byte CHARACTER_SETS = (byte) 0x85;

    @Override
    public byte[] toByte() {
        ByteBuffer buffer = ByteBuffer.allocate(22);
        buffer.putShort((short) 22);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(CHARACTER_SETS);
        buffer.put((byte) 0X02); // *** Flags cgcsgid PRESENT
        buffer.put((byte) 0X40); // *** Flags ccsid PRESENT

        buffer.put((byte) 0x09); // *** SDW
        buffer.put((byte) 0x0c); // *** SDH
        buffer.putInt(0); // *** FORM

        buffer.put((byte) 9); // *** DL

        buffer.put((byte) 0); // *** SET
        buffer.put((byte) 0); // *** FLAGS
        buffer.put((byte) 0); // *** LCID
        buffer.put(new byte[] { (byte) 0x02, (byte) 0xb9, (byte) 0x00, (byte) 0x25 }); // *** CGCSGID
        buffer.putShort((short) 35); // *** CCSID
        return buffer.array();
    }

    @Override
    public byte getID() {
        return CHARACTER_SETS;
    }

}
