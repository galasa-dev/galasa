/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;

public class InterruptedRunEventProcessor implements Runnable {

    private Log logger = LogFactory.getLog(getClass());

    private final Queue<RunInterruptEvent> queue;
    private IFrameworkRuns frameworkRuns;

    public InterruptedRunEventProcessor(Queue<RunInterruptEvent> queue, IFrameworkRuns frameworkRuns) {
        this.queue = queue;
        this.frameworkRuns = frameworkRuns;
    }

    @Override
    public void run() {
        try {
            boolean isDone = false;

            logger.debug("Starting scan of interrupt events to process");
            while (!isDone) {

                RunInterruptEvent interruptEvent = queue.poll();
                if (interruptEvent == null) {
                    isDone = true;
                } else {
                    markRunFinishedInDss(interruptEvent);
                }
            }
            logger.debug("Finished scan of interrupt events to process");
        } catch (Exception ex) {
            logger.warn("Exception caught and ignored in WatchEventProcessor: "+ex);
        }
    }

    private void markRunFinishedInDss(RunInterruptEvent interruptEvent) throws DynamicStatusStoreException {
        String runName = interruptEvent.getRunName();
        frameworkRuns.setRunStatus(runName, TestRunLifecycleStatus.FINISHED);
    }
}
