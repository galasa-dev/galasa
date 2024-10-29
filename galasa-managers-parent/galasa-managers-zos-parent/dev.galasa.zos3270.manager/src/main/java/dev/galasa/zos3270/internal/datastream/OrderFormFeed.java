/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public class OrderFormFeed extends AbstractOrder {

    public static final byte ID = 0x0c;

    public OrderFormFeed() {
    }

    @Override
    public String toString() {
        return "FORMFEED()";
    }

    public String getText() {
        return " ";
    }

    @Override
    public byte[] getBytes() {
        return new byte[] {ID};
    }

}
