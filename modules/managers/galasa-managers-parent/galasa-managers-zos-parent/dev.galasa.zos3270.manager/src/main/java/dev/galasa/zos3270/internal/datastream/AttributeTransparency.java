/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

public class AttributeTransparency implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x46;

    private final byte       transparency;

    public AttributeTransparency(ByteBuffer buffer) {
        this.transparency = buffer.get();
    }

    public byte getTransparency() {
        return transparency;
    }

}
