/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;

public class BufferChar implements IBufferHolder {

    private final char           character;

    public BufferChar(char character) {
        if (character == -1) {
            character = ' ';
        }

        this.character = character;
    }

    @Override
    public String toString() {
        return "char(" + this.character + ")";
    }

    @Override
    public String getStringWithoutNulls() {
        if (character == 0) {
            return " ";
        }
        return "" + this.character;
    }

    @Override
    public char getChar() {
        return this.character;
    }

    public byte getFieldEbcdic(Charset codePage) {
        if (this.character == 0) {
            return 0;
        }

        String value = new String(new char[] { this.character });

        return value.getBytes(codePage)[0];
    }

}
