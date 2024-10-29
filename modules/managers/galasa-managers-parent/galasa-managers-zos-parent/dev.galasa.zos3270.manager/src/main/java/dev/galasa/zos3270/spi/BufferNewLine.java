/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

public class BufferNewLine extends BufferChar {

    public BufferNewLine() {
        super((char)0x15);
    }

    @Override
    public String toString() {
        return "newline()";
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
        return 0x15;
    }

}
