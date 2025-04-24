/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

public class RunRasAction {
    
    // The run ID is the RAS document ID for the CouchDB RAS
    private String runId;
    private String desiredRunStatus;
    private String desiredRunResult;

    public RunRasAction(String runId, String desiredRunStatus, String desiredRunResult) {
        this.runId = runId;
        this.desiredRunStatus = desiredRunStatus;
        this.desiredRunResult = desiredRunResult;
    }

    public String getRunId() {
        return this.runId;
    }

    public String getDesiredRunStatus() {
        return this.desiredRunStatus;
    }

    public String getDesiredRunResult() {
        return this.desiredRunResult;
    }
}
