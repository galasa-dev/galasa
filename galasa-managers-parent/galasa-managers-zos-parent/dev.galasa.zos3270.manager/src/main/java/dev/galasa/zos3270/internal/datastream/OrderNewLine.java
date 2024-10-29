/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public class OrderNewLine extends AbstractOrder {

    public static final byte ID = 0x15;

    public OrderNewLine() {
    }

    @Override
    public String toString() {
        return "NEWLINE()";
    }

    public String getText() {
        return " ";
    }

    @Override
    public byte[] getBytes() {
        return new byte[] {ID};
    }

}
