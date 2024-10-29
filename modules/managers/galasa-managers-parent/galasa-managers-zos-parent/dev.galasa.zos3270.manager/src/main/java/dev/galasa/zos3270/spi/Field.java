/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Field {

    private static final Charset ebcdic = Charset.forName("Cp037");

    private final int            start;

    private char[]               text;

    private final boolean        fieldProtected;
    private final boolean        fieldNumeric;
    private final boolean        fieldDisplay;
    private final boolean        fieldIntenseDisplay;
    private final boolean        fieldSelectorPen;
    private final boolean        fieldModifed;
    
    private final Colour         foregroundColour;
    private final Colour         backgroundColour;
    private final Highlight      highlight;

    protected Field(int start, BufferStartOfField sf) {
        this.start = start;
        this.fieldProtected = sf.isProtected();
        this.fieldNumeric = sf.isNumeric();
        this.fieldDisplay = sf.isDisplay();
        this.fieldIntenseDisplay = sf.isIntenseDisplay();
        this.fieldSelectorPen = sf.isSelectorPen();
        this.fieldModifed = sf.isFieldModifed();
        this.text = new char[0];
        
        this.foregroundColour = sf.getForegroundColour();
        this.backgroundColour = sf.getBackgroundColour();
        this.highlight        = sf.getHighlight();
    }

    public Field() {
        this.start = -1;
        this.fieldProtected = false;
        this.fieldNumeric = false;
        this.fieldDisplay = true;
        this.fieldIntenseDisplay = false;
        this.fieldSelectorPen = false;
        this.fieldModifed = false;
        this.text = new char[0];
        this.foregroundColour = null;
        this.backgroundColour = null;
        this.highlight = null;
    }

    protected void appendChar(char newChar) {
        char[] newText = Arrays.copyOf(this.text, this.text.length + 1);
        newText[newText.length - 1] = newChar;
        this.text = newText;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Field(pos=");
        sb.append(Integer.toString(start));
        sb.append(",p=");
        sb.append(Boolean.toString(fieldProtected));
        sb.append(",n=");
        sb.append(Boolean.toString(fieldNumeric));
        sb.append(",d=");
        sb.append(Boolean.toString(fieldDisplay));
        sb.append(",i=");
        sb.append(Boolean.toString(fieldIntenseDisplay));
        sb.append(",s=");
        sb.append(Boolean.toString(fieldSelectorPen));
        sb.append(",m=");
        sb.append(Boolean.toString(fieldModifed));
        sb.append(",");
        sb.append(getFieldWithoutNulls());
        sb.append(")");
        return sb.toString();
    }

    public int length() {
        return text.length + 1;
    }

    public boolean containsText(String searchText) {
        return new String(this.text).contains(searchText);
    }

    public int getStart() {
        if (this.start == -1) {
            return 0;
        }
        return this.start;
    }

    public boolean containsPosition(int screenCursor) {
        int end = this.start + this.text.length;
        return this.start <= screenCursor && screenCursor <= end;
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

    public boolean isUnformatted() {
        return (this.start == -1);
    }
    
    public Colour getForegroundColour() {
        return foregroundColour;
    }

    public Colour getBackgroundColour() {
        return backgroundColour;
    }

    public Highlight getHighlight() {
        return highlight;
    }

    public String getFieldWithoutNulls() {
        StringBuilder sb = new StringBuilder();
        for (char c : text) {
            if (c == 0) {
                sb.append(" ");
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public byte[] getFieldWithNulls() {
        String otext = new String(text);
        return otext.getBytes(ebcdic);
    }

    public Character[] getFieldCharsWithNulls() {
        Character[] newChars = new Character[text.length];
        for (int i = 0; i < text.length; i++) {
            if (text[i] == 0) {
                newChars[i] = null;
            } else {
                newChars[i] = text[i];
            }
        }
        return newChars;
    }

    public boolean isDummyField() {
        return this.start == -1;
    }

}
