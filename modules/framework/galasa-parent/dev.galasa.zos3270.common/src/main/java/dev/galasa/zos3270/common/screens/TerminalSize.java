/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens;

/**
 * Represents the terminal screen size
 * 
 *  
 *
 */
public class TerminalSize {

    private int columns;
    private int rows;

    /**
     * Constructor
     * 
     * @param columns no of columns on the screen
     * @param rows    no of rows on the screen
     */
    public TerminalSize(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * Fetch the columns
     * 
     * @return Columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Fetch the rows
     * 
     * @return Rows
     */
    public int getRows() {
        return rows;
    }

}
