/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.util.List;

public class QueryReplySummary extends AbstractQueryReply {

    private static final byte              SUMMARY = (byte) 0x80;

    private final List<AbstractQueryReply> replies;

    public QueryReplySummary(List<AbstractQueryReply> replies) {
        this.replies = replies;
    }

    @Override
    public byte[] toByte() {
        int length = 5 + replies.size();

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putShort((short) length);
        buffer.put(AbstractQueryReply.QUERY_REPLY);
        buffer.put(SUMMARY);

        buffer.put(SUMMARY);
        for (AbstractQueryReply reply : replies) {
            buffer.put(reply.getID());
        }
        return buffer.array();
    }

    @Override
    public byte getID() {
        return SUMMARY;
    }

}
