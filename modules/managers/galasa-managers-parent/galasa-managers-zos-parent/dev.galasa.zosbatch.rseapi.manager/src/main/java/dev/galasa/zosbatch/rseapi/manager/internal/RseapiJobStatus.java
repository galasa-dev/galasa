/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.rseapi.manager.internal;

/**
 * An enum to hold the statuses of a batch job that can be returned from the RSE
 * API.
 */
public enum RseapiJobStatus {
    HOLD("HOLD"),
    ACTIVE("ACTIVE"),
    ABEND("ABEND", true),
    COMPLETED("COMPLETED", true),
    COMPLETION("COMPLETION", true),
    NOTFOUND("NOT_FOUND", true),
    UNKNOWN("UNKNOWN");

    private String value;
    private boolean isCompleteStatus;

    private RseapiJobStatus(String value) {
        this.value = value;
        this.isCompleteStatus = false;
    }

    private RseapiJobStatus(String value, boolean isCompleteStatus) {
        this.value = value;
        this.isCompleteStatus = isCompleteStatus;
    }

    /**
     * Converts a given string into an RseapiJobStatus enum value. If no enum value
     * matches, then the UNKNOWN value will be assigned.
     *
     * @param jobStatus the string to convert
     * @return an RseapiJobStatus enum value
     */
    public static RseapiJobStatus getJobStatusFromString(String jobStatus) {
        for (RseapiJobStatus status : values()) {
            if (status.toString().equals(jobStatus)) {
                return status;
            }
        }
        return RseapiJobStatus.UNKNOWN;
    }

    public boolean isComplete() {
        return this.isCompleteStatus;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
