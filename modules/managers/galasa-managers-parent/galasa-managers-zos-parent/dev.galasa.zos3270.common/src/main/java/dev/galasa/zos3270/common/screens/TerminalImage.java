/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Pojo to represent a screen image as received or sent from the client. Inbound
 * means received by the client.
 * 
 *  
 *
 */
public class TerminalImage {

    private final int                 sequence;
    private final String              id;
    private final boolean             inbound;                   // *** Inbound means from server -> client
    private final String              type;

    private final TerminalSize        imageSize;

    private final int                 cursorColumn;
    private final int                 cursorRow;

    private final String              aid;
    private final List<TerminalField> fields = new ArrayList<>();

    /**
     * Constructor
     * 
     * @param sequence     The sequence of the image, for the whole terminal
     *                     interaction
     * @param id           Id of this image
     * @param inbound      inbound = true if received by the client
     * @param type         Type of interaction, interactions can be labels so can be
     *                     filtered
     * @param aid          The AttentionID for outbound messages
     * @param imageSize    The size of the image if different to the default
     * @param cursorColumn The position of the cursor on the screen
     * @param cursorRow    The position of the cursor on the screen
     */
    public TerminalImage(int sequence, @NotNull String id, boolean inbound, String type, String aid,
            TerminalSize imageSize, int cursorColumn, int cursorRow) {
        this.sequence = sequence;
        this.id = id;
        this.inbound = inbound;
        this.type = type;
        this.aid = aid;
        this.imageSize = imageSize;
        this.cursorColumn = cursorColumn;
        this.cursorRow = cursorRow;
    }

    /**
     * Fetch the sequence number
     * 
     * @return sequence number
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Fetch the id
     * 
     * @return the id
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Is inbound to the client. false means being sent from the client
     * 
     * @return inbound
     */
    public boolean isInbound() {
        return inbound;
    }

    /**
     * Return the image size, if null, then it is the default size
     * 
     * @return the terminal size if not default
     */
    public TerminalSize getImageSize() {
        return imageSize;
    }

    /**
     * Fetch the fields
     * 
     * @return Fields
     */
    public List<TerminalField> getFields() {
        return fields;
    }

    /**
     * Fetch the AID if outbound
     * 
     * @return the aid, only valid for outbound images
     */
    public String getAid() {
        return aid;
    }

    /**
     * Get the filtering type of the image
     * 
     * @return filtering type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the column where the cursor is positioned
     */
    public int getCursorColumn() {
        return cursorColumn;
    }

    /**
     * @return the row where the cursor is positioned
     */
    public int getCursorRow() {
        return cursorRow;
    }

}
