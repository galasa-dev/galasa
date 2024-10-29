/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

public class BufferEndOfMedium extends BufferChar {

    public BufferEndOfMedium() {
        super((char)0x19);
    }

    @Override
    public String toString() {
        return "endofmedium()";
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
        return 0x19;
    }

}
