/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public abstract class AbstractQueryReply {

    public static final byte QUERY_REPLY = (byte) 0x81;

    public abstract byte[] toByte();

    public abstract byte getID();

}
