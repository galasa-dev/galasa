/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.api.runs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.api.run.Run;

public class ScheduleStatus implements Serializable {
    private static final long   serialVersionUID = 1L;

    private boolean             complete;

    private List<Run> runs;

    public ScheduleStatus() {
        runs = new ArrayList<>();
    }

    public boolean isComplete() {
        return this.complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public void setRuns(List<Run> runs) {
        this.runs = runs;
    }
}
