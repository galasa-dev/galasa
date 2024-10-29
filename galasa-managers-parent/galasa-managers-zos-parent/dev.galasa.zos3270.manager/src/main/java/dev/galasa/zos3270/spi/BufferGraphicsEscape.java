/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Hex;

public class BufferGraphicsEscape extends BufferChar {
    
    private final byte data;

    public BufferGraphicsEscape(byte data) {
        super(' ');
        
        this.data = data;
    }

    @Override
    public String toString() {
        String hex = Hex.encodeHexString(new byte[] {this.data});

        return "graphicscsescape(0x" + hex + ")";
    }

    @Override
    public String getStringWithoutNulls() {
        return " ";
    }

    @Override
    public char getChar() {
        return ' ';
    }

    @Override
    public byte getFieldEbcdic(Charset codePage) {
        return this.data;
    }

}
