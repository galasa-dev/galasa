/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public class OrderCarrageReturn extends AbstractOrder {
    
    public static final byte ID = 0x0d;

    public OrderCarrageReturn() {
    }

    @Override
    public String toString() {
        return "CARRAGERETURN()";
    }

    public String getText() {
        return " ";
    }

    @Override
    public byte[] getBytes() {
        return new byte[] {ID};
    }

}
