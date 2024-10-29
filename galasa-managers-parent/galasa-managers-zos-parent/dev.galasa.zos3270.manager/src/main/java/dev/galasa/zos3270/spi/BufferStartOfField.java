/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import dev.galasa.zos3270.internal.datastream.AttributeBackgroundColour;
import dev.galasa.zos3270.internal.datastream.AttributeCharacterSet;
import dev.galasa.zos3270.internal.datastream.AttributeExtendedHighlighting;
import dev.galasa.zos3270.internal.datastream.AttributeFieldOutlining;
import dev.galasa.zos3270.internal.datastream.AttributeFieldValidation;
import dev.galasa.zos3270.internal.datastream.AttributeForegroundColour;
import dev.galasa.zos3270.internal.datastream.AttributeTransparency;

/**
 * Create a Start of Field position, represents to SF order
 * 
 *  
 *
 */
public class BufferStartOfField implements IBufferHolder {

    private final boolean fieldProtected;
    private final boolean fieldNumeric;
    private final boolean fieldDisplay;
    private final boolean fieldIntenseDisplay;
    private final boolean fieldSelectorPen;
    private boolean       fieldModifed;

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

    /**
     * Create the start of a field
     * 
     * @param position - The buffer position the field is to start
     */
    public BufferStartOfField(int position, boolean fieldProtected, boolean fieldNumeric, boolean fieldDisplay,
            boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed) {
        this.fieldProtected = fieldProtected;
        this.fieldNumeric = fieldNumeric;
        this.fieldDisplay = fieldDisplay;
        this.fieldIntenseDisplay = fieldIntenseDisplay;
        this.fieldSelectorPen = fieldSelectorPen;
        this.fieldModifed = fieldModifed;
    }
    
    public BufferStartOfField(int position, boolean fieldProtected, boolean fieldNumeric, boolean fieldDisplay,
            boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed,
            AttributeExtendedHighlighting extendedHighlighting, AttributeForegroundColour foregroundColour, AttributeBackgroundColour backgroundColour) {
        this(position, fieldProtected, fieldNumeric, fieldDisplay, fieldIntenseDisplay, fieldSelectorPen, fieldModifed);
        
        this.attributeExtendedHighlighting = extendedHighlighting;
        this.attributeForegroundColour     = foregroundColour;
        this.attributeBackgroundColour     = backgroundColour;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.zos3270.internal.terminal.fields.Field#toString()
     */
    @Override
    public String toString() {
        return "StartOfField(" + super.toString() + ")";
    }

    public boolean isProtected() {
        return fieldProtected;
    }

    public boolean isNumeric() {
        return fieldNumeric;
    }

    public boolean isDisplay() {
        return fieldDisplay;
    }

    public boolean isIntenseDisplay() {
        return fieldIntenseDisplay;
    }

    public boolean isSelectorPen() {
        return fieldSelectorPen;
    }

    public boolean isFieldModifed() {
        return fieldModifed;
    }

    public void setFieldModified() {
        this.fieldModifed = true;
    }
    
    public void clearFieldModified() {
        this.fieldModifed = false;
    }
    
    public Highlight getHighlight() {
        if (this.attributeExtendedHighlighting == null) {
            return null;
        }
        return this.attributeExtendedHighlighting.getHighlight();
    }
    
    public Colour getForegroundColour() {
        if (this.attributeForegroundColour == null) {
            return null;
        }
        return this.attributeForegroundColour.getColour();
    }

    public Colour getBackgroundColour() {
        if (this.attributeBackgroundColour == null) {
            return null;
        }
        return this.attributeBackgroundColour.getColour();
    }

    public AttributeExtendedHighlighting getAttributeExtendedHighlighting() {
        return this.attributeExtendedHighlighting;
    }
    
    public AttributeForegroundColour getAttributeForegroundColour() {
        return this.attributeForegroundColour;
    }

    public AttributeBackgroundColour getAttributeBackgroundColour() {
        return this.attributeBackgroundColour;
    }

    @Override
    public String getStringWithoutNulls() {
        return " ";
    }
    
    @Override
    public char getChar() {
        return ' ';
    }


}
