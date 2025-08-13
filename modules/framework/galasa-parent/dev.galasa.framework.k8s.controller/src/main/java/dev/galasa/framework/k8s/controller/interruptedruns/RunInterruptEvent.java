/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import java.time.Instant;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.RunRasAction;

/**
 * RunInterruptEvent represents an event where a run has been interrupted by setting its interrupt reason 
 * in its DSS record. These events are then processed by another thread.
 */
public class RunInterruptEvent {

    private final Log logger = LogFactory.getLog(getClass());

    private final List<RunRasAction> rasActions;
    private final String runName;
    private final String interruptReason;
    private final Instant interruptedAt;
    private final TestRunLifecycleStatus testRunStatus ;
    private boolean isPastGracePeriod;

    public RunInterruptEvent(List<RunRasAction> rasActions, String runName, String interruptReason, Instant interruptedAt, TestRunLifecycleStatus testRunStatus) {
        this.rasActions = rasActions;
        this.runName = runName;
        this.interruptReason = interruptReason;
        this.interruptedAt = interruptedAt;
        this.testRunStatus = testRunStatus;
        this.isPastGracePeriod = false ;

        logger.debug("Created: " + this.toString());
    }

    @Override
    public String toString() {
        return "Interrupt event: runName:"+runName+" interruptReason:"+interruptReason+" status: "+testRunStatus;
    }

    public List<RunRasAction> getRasActions() {
        return this.rasActions;
    }

    public String getRunName() {
        return this.runName;
    }

    public String getInterruptReason() {
        return this.interruptReason;
    }

    public Instant getInterruptedAt() {
        return this.interruptedAt;
    }
    public TestRunLifecycleStatus getTestRunStatus() {
        return this.testRunStatus;
    }


    public void setPastGracePeriod(boolean isPastGracePeriod) {
        this.isPastGracePeriod = isPastGracePeriod ;
    }

    public boolean isPastGracePeriod() {
        return this.isPastGracePeriod;
    }
}
