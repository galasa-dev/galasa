/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class OrderStartField extends AbstractOrder implements IAttribute {

    public static final byte ID           = 0x1d;
    public static final byte ATTRIBUTE_ID = -64;

    private final boolean    fieldProtected;
    private final boolean    fieldNumeric;
    private final boolean    fieldDisplay;
    private final boolean    fieldIntenseDisplay;
    private final boolean    fieldSelectorPen;
    private final boolean    fieldModifed;

    public OrderStartField(ByteBuffer buffer) {
        byte attributes = buffer.get();
        this.fieldProtected = ((attributes & 0x20) == 0x20);
        this.fieldNumeric = ((attributes & 0x10) == 0x10);
        this.fieldDisplay = ((attributes & 0x08) == 0x00);
        this.fieldIntenseDisplay = ((attributes & 0x0c) == 0x08);
        this.fieldSelectorPen = (((attributes & 0x0c) == 0x04) || ((attributes & 0x0c) == 0x08));
        this.fieldModifed = ((attributes & 0x01) == 0x01);
    }

    public OrderStartField(boolean fieldProtected, boolean fieldNumeric, boolean fieldDisplay,
            boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed) {
        this.fieldProtected = fieldProtected;
        this.fieldNumeric = fieldNumeric;
        this.fieldDisplay = fieldDisplay;
        this.fieldIntenseDisplay = fieldIntenseDisplay;
        this.fieldSelectorPen = fieldSelectorPen;
        this.fieldModifed = fieldModifed;
    }

    public boolean isFieldProtected() {
        return fieldProtected;
    }

    public boolean isFieldNumeric() {
        return fieldNumeric;
    }

    public boolean isFieldDisplay() {
        return fieldDisplay;
    }

    public boolean isFieldIntenseDisplay() {
        return fieldIntenseDisplay;
    }

    public boolean isFieldSelectorPen() {
        return fieldSelectorPen;
    }

    public boolean isFieldModifed() {
        return fieldModifed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SF(");
        if (this.fieldProtected) {
            sb.append("Protected ");
        } else {
            sb.append("Unprotected ");
        }
        if (this.fieldNumeric) {
            sb.append("Numeric ");
        } else {
            sb.append("Alphanumeric ");
        }

        if (this.fieldDisplay) {
            sb.append("Display ");
        }
        if (this.fieldIntenseDisplay) {
            sb.append("Intense ");
        }
        if (!this.fieldDisplay && !this.fieldIntenseDisplay) {
            sb.append("Nondisplay ");
        }
        if (this.fieldSelectorPen) {
            sb.append("SelectorPen ");
        }

        if (this.fieldModifed) {
            sb.append("Modified");
        } else {
            sb.append("Unmodified");
        }
        sb.append(")");

        return sb.toString();
    }

    @Override
    public byte[] getBytes() {

        byte[] buffer = new byte[2];
        buffer[0] = ID;

        BitSet bitSet = new BitSet(8);
        bitSet.set(6, false);
        bitSet.set(7, false);
        bitSet.set(5, fieldProtected);
        bitSet.set(4, fieldNumeric);

        if (!fieldDisplay) {
            bitSet.set(3, true);
            bitSet.set(2, true);
        } else if (fieldIntenseDisplay) {
            bitSet.set(3, true);
            bitSet.set(2, false);
        } else if (fieldSelectorPen) {
            bitSet.set(3, false);
            bitSet.set(2, true);
        }
        bitSet.set(1, false);
        bitSet.set(0, fieldModifed);

        if (bitSet.isEmpty()) {
            buffer[1] = 0;
        } else { 
            int preConverted = bitSet.toByteArray()[0];
            buffer[1] = BufferAddress.chars[preConverted];
        }
        return buffer;
    }

}
