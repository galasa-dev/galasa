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
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IResultArchiveStoreDirectoryService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;

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
    private IResultArchiveStore rasStore;

    public InterruptedRunEventProcessor(Queue<RunInterruptEvent> queue, IFrameworkRuns frameworkRuns, IResultArchiveStore rasStore) {
        this.queue = queue;
        this.frameworkRuns = frameworkRuns;
        this.rasStore = rasStore;
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
                    markRunFinishedInDss(interruptEvent);
                    processRasActions(interruptEvent);
                }
            }
            logger.debug("Finished scan of interrupt events to process");
        } catch (Exception ex) {
            logger.warn("Exception caught and ignored in InterruptRunEventProcessor", ex);
        }
    }

    private void markRunFinishedInDss(RunInterruptEvent interruptEvent) throws DynamicStatusStoreException {
        String runName = interruptEvent.getRunName();
        String interruptReason = interruptEvent.getInterruptReason();

        logger.info("Marking run '" + runName + "' as finished in the DSS");
        frameworkRuns.markRunFinished(runName, interruptReason);
        logger.info("Marked run '" + runName + "' as finished in the DSS OK");
    }

    private void processRasActions(RunInterruptEvent interruptEvent) throws ResultArchiveStoreException {
        List<RunRasAction> rasActions = interruptEvent.getRasActions();
        String runName = interruptEvent.getRunName();

        logger.info("Processing RAS actions for run '" + runName + "'");

        for (RunRasAction rasAction : rasActions) {
            String runId = rasAction.getRunId();
            TestStructure testStructure = getRunTestStructure(runId);
            if (testStructure != null) {

                // Set the status and result for the run if it doesn't already have the desired status
                String runStatus = testStructure.getStatus();
                String desiredRunStatus = rasAction.getDesiredRunStatus();
                if (!desiredRunStatus.equals(runStatus)) {
                    testStructure.setStatus(desiredRunStatus);
                    testStructure.setResult(rasAction.getDesiredRunResult());

                    rasStore.updateTestStructure(runId, testStructure);
                } else {
                    logger.info("Run already has status '" + desiredRunStatus + "', will not update its RAS record");
                }
            }
        }
        logger.info("RAS actions for run '" + runName + "' processed OK");
    }

    private TestStructure getRunTestStructure(String runId) throws ResultArchiveStoreException {
        TestStructure testStructure = null;
        for (IResultArchiveStoreDirectoryService directoryService : rasStore.getDirectoryServices()) {
            IRunResult run = directoryService.getRunById(runId);

            if (run != null) {
                testStructure = run.getTestStructure();
                break;
            }
        }
        return testStructure;
    }
}
