/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.RunRasAction;

/**
 * InterruptedRunEventProcessor runs as a thread in the engine controller pod and it maintains a
 * queue of interrupt events.
 * 
 * When the interrupt event queue is not empty, it removes each event from the queue and processes them
 * by marking interrupted runs as finished in the DSS and updating their RAS records as defined by the
 * deferred RAS actions within the interrupt event.
 */
public class InterruptedRunEventProcessor implements Runnable {

    private Log logger = LogFactory.getLog(getClass());

    private final Queue<RunInterruptEvent> queue;
    private IFrameworkRuns frameworkRuns;
    private IRunRasActionProcessor rasActionProcessor;

    public InterruptedRunEventProcessor(
        Queue<RunInterruptEvent> queue,
        IFrameworkRuns frameworkRuns,
        IRunRasActionProcessor rasActionProcessor
    ) {
        this.queue = queue;
        this.frameworkRuns = frameworkRuns;
        this.rasActionProcessor = rasActionProcessor;
    }

    /**
     * Gets called periodically based on the engine controller's scheduling.
     * 
     * Each time this method is invoked, it processes all the events in the event queue and then exits.
     */
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
                    String runName = interruptEvent.getRunName();
                    List<RunRasAction> rasActions = interruptEvent.getRasActions();
                    rasActionProcessor.processRasActions(runName, rasActions);
                    
                    String interruptReason = interruptEvent.getInterruptReason();
                    switch (interruptReason) {
                        case Result.CANCELLED:
                        case Result.HUNG:
                            markRunFinishedInDss(runName, interruptReason);
                            break;
                        case Result.REQUEUED:
                            requeueRunInDss(runName);
                            break;
                        default:
                            logger.warn("Unknown interrupt reason set '" + interruptReason + "', ignoring");
                    }
                }
            }
            logger.debug("Finished scan of interrupt events to process");
        } catch (Exception ex) {
            logger.warn("Exception caught and ignored in InterruptRunEventProcessor", ex);
        }
    }

    private void requeueRunInDss(String runName) throws DynamicStatusStoreException {
        logger.info("Requeuing run '" + runName + "' in the DSS");

        frameworkRuns.reset(runName);

        logger.info("Requeued run '" + runName + "' in the DSS OK");
    }

    private void markRunFinishedInDss(String runName, String interruptReason) throws DynamicStatusStoreException {
        logger.info("Marking run '" + runName + "' as finished in the DSS");

        frameworkRuns.markRunFinished(runName, interruptReason);

        logger.info("Marked run '" + runName + "' as finished in the DSS OK");
    }
}
