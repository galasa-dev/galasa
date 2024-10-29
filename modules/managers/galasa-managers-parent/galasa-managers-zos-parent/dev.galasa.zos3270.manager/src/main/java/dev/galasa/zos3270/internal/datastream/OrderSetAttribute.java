/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderSetAttribute extends AbstractOrder {

    public static final byte ID = 0x28;

    private final IAttribute attribute;

    public OrderSetAttribute(ByteBuffer buffer) throws DatastreamException {

        byte attributeId = buffer.get();
        switch (attributeId) {
            case AttributeResetAllAttributes.ATTRIBUTE_ID:
                attribute = new AttributeResetAllAttributes(buffer);
                break;
            case AttributeExtendedHighlighting.ATTRIBUTE_ID:
                attribute = new AttributeExtendedHighlighting(buffer);
                break;
            case AttributeForegroundColour.ATTRIBUTE_ID:
                attribute = new AttributeForegroundColour(buffer);
                break;
            case AttributeCharacterSet.ATTRIBUTE_ID:
                attribute = new AttributeCharacterSet(buffer);
                break;
            case AttributeBackgroundColour.ATTRIBUTE_ID:
                attribute = new AttributeBackgroundColour(buffer);
                break;
            case AttributeTransparency.ATTRIBUTE_ID:
                attribute = new AttributeTransparency(buffer);
                break;
            default:
                throw new DatastreamException("Unrecognised attribute in SA, '" + attributeId + "'");
        }

    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("Not available yet");
    }

    public IAttribute getAttribute() {
        return attribute;
    }

}
