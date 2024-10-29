/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.ras;

import java.util.List;

import dev.galasa.framework.spi.IRunResult;

/**
 * An internal bean class representing a page of runs stored in the RAS,
 * which includes a token pointing to the next page of runs for 
 * cursor-based pagination
 */
public class RasRunResultPage {
    
    private List<IRunResult> runs;
    private String nextCursor;

    public RasRunResultPage(List<IRunResult> runs, String nextCursor) {
        this.runs = runs;
        this.nextCursor = nextCursor;
    }

    public List<IRunResult> getRuns() {
        return runs;
    }

    public void setRuns(List<IRunResult> runs) {
        this.runs = runs;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }
}
