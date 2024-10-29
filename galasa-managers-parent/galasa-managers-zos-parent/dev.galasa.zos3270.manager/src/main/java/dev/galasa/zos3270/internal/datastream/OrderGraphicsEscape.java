/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;

public class OrderGraphicsEscape extends AbstractOrder {
    
    public static final byte ID = 0x08;
    
    private final byte data;

    public OrderGraphicsEscape(ByteBuffer buffer) {
        this.data = buffer.get();
    }

    @Override
    public String toString() {
        String hex = Hex.encodeHexString(new byte[] {this.data});
        
        return "GRAPHICSESCAPE(0x" + hex + ")";
    }

    public String getText() {
        return " ";
    }

    @Override
    public byte[] getBytes() {
        return new byte[] {ID, data};
    }

    public byte getByte() {
        return this.data;
    }

}
