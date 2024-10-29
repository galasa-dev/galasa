/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;
import dev.galasa.zos3270.spi.Highlight;

public class AttributeExtendedHighlighting implements IAttribute {

    public static final byte ATTRIBUTE_ID = 0x41;

    private final Highlight highlight;

    public AttributeExtendedHighlighting(ByteBuffer buffer) throws DatastreamException {
        byte code = buffer.get();
        
        this.highlight = Highlight.getHighlight(code);
    }

    public Highlight getHighlight() {
        return this.highlight;
    }

}
