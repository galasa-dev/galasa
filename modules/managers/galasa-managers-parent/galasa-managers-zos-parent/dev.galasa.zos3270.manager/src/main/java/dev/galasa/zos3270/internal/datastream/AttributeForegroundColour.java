/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.Colour;
import dev.galasa.zos3270.spi.DatastreamException;

public class AttributeForegroundColour implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x42;

    private final Colour       colour;

    public AttributeForegroundColour(ByteBuffer buffer) throws DatastreamException {
        byte code = buffer.get();
        
        this.colour = Colour.getColour(code);
    }

    public Colour getColour() {
        return colour;
    }

}
