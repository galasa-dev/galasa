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
 * Pojo to contain a set of terminal images. may not necessary contain all the
 * terminal images for a session.
 * 
 * These pojos are stored in a local directory for inflight tests, and in the
 * RAS.
 * 
 *  
 *
 */
public class Terminal {

    private final String              id;
    private final String              runId;
    private final int                 sequence;
    private final List<TerminalImage> images = new ArrayList<>();
    private final TerminalSize        defaultSize;

    /**
     * Constructor
     * 
     * @param id          Terminal ID
     * @param sequence    Sequence number of this pojo for this terminal
     * @param defaultSize Default size of the terminal
     */
    public Terminal(@NotNull String id, @NotNull String runId, int sequence, @NotNull TerminalSize defaultSize) {
        this.id = id;
        this.runId = runId;
        this.sequence = sequence;
        this.defaultSize = defaultSize;
    }

    /**
     * Fetch the ID
     * 
     * @return id
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Fetch the Images
     * 
     * @return images
     */
    public @NotNull List<TerminalImage> getImages() {
        return images;
    }

    /**
     * Add a new image to the terminal
     * 
     * @param image new image
     */
    public void addImage(@NotNull TerminalImage image) {
        getImages().add(image);
    }

    /**
     * Fetch the default size
     * 
     * @return Default size
     */
    public @NotNull TerminalSize getDefaultSize() {
        return this.defaultSize;
    }

    /**
     * Fetch the sequence number
     * 
     * @return sequence
     */
    public int getSequence() {
        return sequence;
    }

    public String getRunId() {
        return runId;
    }

}
