/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeResetAllAttributes implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x00;

    private final byte       reset;

    public AttributeResetAllAttributes(ByteBuffer buffer) {
        this.reset = buffer.get();
    }

    public byte getReset() {
        return reset;
    }

}
