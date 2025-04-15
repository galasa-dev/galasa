/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

/**
 * Represents the reason why a test run was interrupted.
 */
public enum TestRunInterruptReason {
    CANCELLED("Cancelled"),
    ;

    private String value;

    private TestRunInterruptReason(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
