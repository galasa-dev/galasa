/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderStartFieldExtended extends AbstractOrder {

    public static final byte            ID         = 0x29;

    private OrderStartField               orderStartField               = null;
    @SuppressWarnings("unused")
    private AttributeFieldValidation      attributeFieldValidation      = null;
    @SuppressWarnings("unused")
    private AttributeFieldOutlining       attributeFieldOutlining       = null;
    private AttributeExtendedHighlighting attributeExtendedHighlighting = null;
    @SuppressWarnings("unused")
    private AttributeCharacterSet         attributeCharacterSet         = null;
    private AttributeForegroundColour     attributeForegroundColour     = null;
    private AttributeBackgroundColour     attributeBackgroundColour     = null;
    @SuppressWarnings("unused")
    private AttributeTransparency         attributeTransparency         = null;

    public OrderStartFieldExtended(ByteBuffer buffer) throws DatastreamException {
        byte[] rep = new byte[4];
        rep[0] = 0;
        rep[1] = 0;
        rep[2] = 0;
        rep[3] = buffer.get();

        ByteBuffer countBuffer = ByteBuffer.wrap(rep);
        int fieldAttributeCount = countBuffer.getInt();

        for (int i = 0; i < fieldAttributeCount; i++) {
            byte attributeId = buffer.get();
            switch (attributeId) {
                case OrderStartField.ATTRIBUTE_ID:
                    orderStartField = new OrderStartField(buffer);
                    break;
                case AttributeFieldValidation.ATTRIBUTE_ID:
                    attributeFieldValidation = new AttributeFieldValidation(buffer);
                    break;
                case AttributeFieldOutlining.ATTRIBUTE_ID:
                    attributeFieldOutlining = new AttributeFieldOutlining(buffer);
                    break;
                case AttributeExtendedHighlighting.ATTRIBUTE_ID:
                    attributeExtendedHighlighting = new AttributeExtendedHighlighting(buffer);
                    break;
                case AttributeForegroundColour.ATTRIBUTE_ID:
                    attributeForegroundColour = new AttributeForegroundColour(buffer);
                    break;
                case AttributeCharacterSet.ATTRIBUTE_ID:
                	attributeCharacterSet = new AttributeCharacterSet(buffer);
                    break;
                case AttributeBackgroundColour.ATTRIBUTE_ID:
                    attributeBackgroundColour = new AttributeBackgroundColour(buffer);
                    break;
                case AttributeTransparency.ATTRIBUTE_ID:
                    attributeTransparency = new AttributeTransparency(buffer);
                    break;
                default:
                    throw new DatastreamException("Unrecognised attribute in SFE, '" + attributeId + "'");
            }
        }
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("Not available yet");
    }

    public OrderStartField getOrderStartField() {
        return orderStartField;
    }

    public AttributeExtendedHighlighting getHighlight() {
        return attributeExtendedHighlighting;
    }

    public AttributeForegroundColour getForegroundColour() {
        return this.attributeForegroundColour;
    }

    public AttributeBackgroundColour getBackgroundColor() {
        return this.attributeBackgroundColour;
    }

}
