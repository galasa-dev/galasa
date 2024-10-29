/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class QueryReplyNull extends AbstractQueryReply {

    private static final byte NULL_QCODE = (byte) 0xff;

    @Override
    public byte[] toByte() {
        int length = 4;

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putShort((short) length);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(NULL_QCODE);

        return buffer.array();
    }

    @Override
    public byte getID() {
        return NULL_QCODE;
    }

}
