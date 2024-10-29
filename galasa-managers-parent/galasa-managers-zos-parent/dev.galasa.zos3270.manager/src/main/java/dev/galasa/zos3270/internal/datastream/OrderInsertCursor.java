/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public class OrderInsertCursor extends AbstractOrder {

    public static final byte ID = 0x13;

    @Override
    public String toString() {
        return "IC";
    }

    public byte[] getBytes() {
        return new byte[] { ID };
    }

}
