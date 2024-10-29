/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public abstract class AbstractOrder {

    @Override
    public String toString() {
        return "Ignore SQ";
    }

    public abstract byte[] getBytes();

}
