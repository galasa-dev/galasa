/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;

/**
 * Pojo to represent a Field on the terminal screen.
 * 
 *  
 *
 */
public class TerminalField {

    private final int                 row;
    private final int                 column;

    private final boolean             unformatted;
    private final boolean             fieldProtected;
    private final boolean             fieldNumeric;
    private final boolean             fieldDisplay;
    private final boolean             fieldIntenseDisplay;
    private final boolean             fieldSelectorPen;
    private final boolean             fieldModifed;
    
    private final Character           foregroundColour;
    private final Character           backgroundColour;
    private final Character           highlight;

    private final List<FieldContents> contents = new ArrayList<>();

    /**
     * Constructor
     * 
     * @param row                 Start Row
     * @param column              Start Column
     * @param unformatted         Field is unformatted
     * @param fieldProtected      Field if protected
     * @param fieldNumeric        Field is Numeric
     * @param fieldDisplay        Field is displayable
     * @param fieldIntenseDisplay Field is Intense
     * @param fieldSelectorPen    Field can be selected
     * @param fieldModifed        Field has been modified
     */
    public TerminalField(int row, int column, boolean unformatted, boolean fieldProtected, boolean fieldNumeric,
            boolean fieldDisplay, boolean fieldIntenseDisplay, boolean fieldSelectorPen, boolean fieldModifed,
            Character foregroundColour, Character backgroundColour, Character highlight) {
        this.row = row;
        this.column = column;
        this.unformatted = unformatted;
        this.fieldProtected = fieldProtected;
        this.fieldNumeric = fieldNumeric;
        this.fieldDisplay = fieldDisplay;
        this.fieldIntenseDisplay = fieldIntenseDisplay;
        this.fieldSelectorPen = fieldSelectorPen;
        this.fieldModifed = fieldModifed;
        this.foregroundColour = foregroundColour;
        this.backgroundColour = backgroundColour;
        this.highlight = highlight;
    }

    /**
     * Fetch Start row
     * 
     * @return Start row
     */
    public int getRow() {
        return row;
    }

    /**
     * Fetch Start Column
     * 
     * @return Start column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Is field unformatted. If so, the first position will not be a StartField
     * 
     * @return Unformatted
     */
    public boolean isUnformatted() {
        return unformatted;
    }

    /**
     * Is field protected
     * 
     * @return Protected
     */
    public boolean isFieldProtected() {
        return fieldProtected;
    }

    /**
     * Is field unformatted. If so, the first position will not be a StartField
     * 
     * @return Unformatted
     */
    public boolean isFieldNumeric() {
        return fieldNumeric;
    }

    /**
     * Is field Displayable
     * 
     * @return displayable
     */
    public boolean isFieldDisplay() {
        return fieldDisplay;
    }

    /**
     * Is field intense
     * 
     * @return Intense
     */
    public boolean isFieldIntenseDisplay() {
        return fieldIntenseDisplay;
    }

    /**
     * Is field selectable
     * 
     * @return selectable
     */
    public boolean isFieldSelectorPen() {
        return fieldSelectorPen;
    }

    /**
     * Has field been modified
     * 
     * @return modified
     */
    public boolean isFieldModifed() {
        return fieldModifed;
    }
    
    public Character getForegroundColour() {
        return this.foregroundColour;
    }

    public Character getBackgroundColour() {
        return this.backgroundColour;
    }

    public Character getHighlight() {
        return this.highlight;
    }

    /**
     * Fetch the field contents
     * 
     * @return Field contents
     */
    public List<FieldContents> getContents() {
        return contents;
    }

}
