/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import dev.galasa.zos3270.spi.NetworkException;

import java.util.Set;
import java.util.TreeSet;

public class StructuredFieldReadPartition extends StructuredField {

    public static final byte QUERY      = 0x02;
    public static final byte QUERY_LIST = 0x03;

    public static final byte REQTYP_LIST       = 0x00;
    public static final byte REQTYP_EQUIVALENT = 0x40;
    public static final byte REQTYP_ALL        = (byte) 0x80;

    public enum Type {
        QUERY,
        QUERY_LIST
    }

    private final int       pid;
    private final Type      type;
    private final Byte      reqtyp;
    private final Set<Byte> qcodes = new TreeSet<>();

    public StructuredFieldReadPartition(byte[] structuredFieldData) throws NetworkException {
        this.pid = structuredFieldData[1];

        switch (structuredFieldData[2]) {
            case QUERY:
                this.type = Type.QUERY;
                this.reqtyp = null;
                break;
            case QUERY_LIST:
                this.type = Type.QUERY_LIST;
                this.reqtyp = structuredFieldData[3];
                if (reqtyp != REQTYP_LIST && reqtyp != REQTYP_EQUIVALENT && reqtyp != REQTYP_ALL) {
                    throw new NetworkException("Unsupported Read Partition Request Type code = " + reqtyp);
                }
                for (int i = 4; i < structuredFieldData.length; i++) {
                    this.qcodes.add(structuredFieldData[i]);
                }
                break;
            default:
                throw new NetworkException("Unsupported Read Partition Type code = " + structuredFieldData[2]);
        }
    }

    public int getPartitionId() {
        return this.pid;
    }

    public Type getType() {
        return this.type;
    }

    public Byte getRequestType() {
        return this.reqtyp;
    }

    public Set<Byte> getQcodes() {
        return qcodes;
    }
}
