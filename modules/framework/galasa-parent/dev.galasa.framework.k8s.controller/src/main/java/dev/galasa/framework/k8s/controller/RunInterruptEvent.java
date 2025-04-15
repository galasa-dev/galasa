/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import io.kubernetes.client.openapi.models.V1Pod;

public class RunInterruptEvent {

    private final String runName;
    private final String interruptReason;
    private final V1Pod interruptedPod;

    public RunInterruptEvent(String runName, String interruptReason, V1Pod interruptedPod) {
        this.runName = runName;
        this.interruptReason = interruptReason;
        this.interruptedPod = interruptedPod;
    }

    public String getRunName() {
        return this.runName;
    }

    public String getInterruptReason() {
        return this.interruptReason;
    }

    public V1Pod getInterruptedPod() {
        return this.interruptedPod;
    }
}
