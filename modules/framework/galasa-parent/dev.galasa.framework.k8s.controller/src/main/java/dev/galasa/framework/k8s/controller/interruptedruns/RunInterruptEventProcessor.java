/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;

/**
 * InterruptedRunEventProcessor runs in the engine controller pod and it consumes a queue of 
 * of interrupt events which are eligible for processing now.
 * 
 * For each interrupt event on the queue it processes them
 * by marking interrupted runs as finished in the DSS and updating their RAS records as defined by the
 * deferred RAS actions within the interrupt event, killing pods if necessary.
 */
public class RunInterruptEventProcessor {

    private Log logger = LogFactory.getLog(getClass());

    private IFrameworkRuns frameworkRuns;
    private IRunRasActionProcessor rasActionProcessor;
    private KubernetesEngineFacade kubeFacade;
    private IResultArchiveStore rasStore;
    private PodDeleter podDeleter;

    public RunInterruptEventProcessor(
        IFrameworkRuns frameworkRuns,
        IRunRasActionProcessor rasActionProcessor,
        KubernetesEngineFacade kubeFacade,
        IResultArchiveStore rasStore,
        PodDeleter podDeleter
    ) {
        this.frameworkRuns = frameworkRuns;
        this.rasActionProcessor = rasActionProcessor;
        this.kubeFacade = kubeFacade;
        this.rasStore = rasStore;
        this.podDeleter = podDeleter;
    }

    /**
     * Gets called when there may be items on teh queue to process
     * 
     * Each time this method is invoked, it processes all the events in the event queue and then exits.
     * @param interruptedRunEvents 
     */
    public void processEvents(List<RunInterruptEvent> interruptedRunEvents) {
        if (!kubeFacade.isEtcdAndRasReady()) {
            logger.warn("etcd or RAS pods are not ready, waiting for them to be ready before processing interrupt events");
        } else {
            processInterruptEvents(interruptedRunEvents);
        }
    }

    private void processInterruptEvents(List<RunInterruptEvent> events ) {
        try {

            logger.debug("Starting scan of interrupt events to process");
            for( RunInterruptEvent interruptEvent : events ) {

                String runName = interruptEvent.getRunName();

                // If the test run is in queued state mark it as Cancelling, because we don't want any 
                // instance of the engine controller to get hold of it and start the test by moving it
                // to allocated. This is a putSwap operation, so can fail if the pod is already moved
                // to allocated/started...etc. 
                TestRunLifecycleStatus runStatus = interruptEvent.getTestRunStatus();
                if (TestRunLifecycleStatus.QUEUED == runStatus) {
                    logger.info("Marking run " + runName + " as 'cancelling' as it was interrupted in the 'queued' state");
                    boolean isMarkedAsCancelling = markRunCancellingInDss(runName, runStatus);
                    if(!isMarkedAsCancelling) {
                        // The attempt to wrest ownership of this test pod failed.
                        // Another instance of the engine controller got there first, and is trying to launch this test pod.
                        // then abandon attempts at cancelling for now.
                        // We will re-visit this pod when we next poll for things to cancel.
                        // The pod won't get far once it gets scheduled before it notices it needs to cancel.
                        logger.info("Run "+runName+" was scheduled just before it was cancelled. Will cancel it shortly...");
                        break;
                    } else {
                        logger.info("Marked run " + runName + " as 'cancelling'");
                    }
                }

                if (runStatus!=null) {
                    switch(runStatus) {
                        case QUEUED:
                        case CANCELLING:
                            // There is no pod to cancel.
                            logger.info("There is no pod to cancel as run " + runName + " is in the state " + runStatus);
                            break;
                        default:
                            // There may be a pod to cancel.
                            logger.info("Cancelling pod for run " + runName);
                            podDeleter.deletePod(runName);
                    }
                }

                List<RunRasAction> rasActions = interruptEvent.getRasActions();
                if (rasActions != null && !rasActions.isEmpty()) {
                    logger.info("Processing RAS actions for run " + runName);
                    rasActionProcessor.processRasActions(runName, rasActions);
                } else {
                    logger.info("No RAS actions to process for run " + runName);
                }
                
                String interruptReason = interruptEvent.getInterruptReason();
                switch (interruptReason) {
                    case Result.CANCELLED:
                    case Result.HUNG:
                        logger.info(runName + " has an interrupt reason of " + interruptReason + ", so marking the run as 'finished' in the DSS");
                        markRunFinishedInDss(runName, interruptReason);
                        break;
                    case Result.REQUEUED:
                        requeueRun(runName);
                        break;
                    default:
                        logger.warn("Unknown interrupt reason set '" + interruptReason + "', ignoring");
                }
            }
            logger.debug("Finished scan of interrupt events to process");
        } catch (Exception ex) {
            logger.warn("Exception caught and ignored in InterruptRunEventProcessor", ex);
        }
    }

    private void requeueRun(String runName) throws DynamicStatusStoreException, ResultArchiveStoreException {
        logger.info("Requeuing run '" + runName + "' in the DSS");

        frameworkRuns.reset(runName);

        logger.info("Requeued run '" + runName + "' in the DSS OK");

        IRun resetRun = frameworkRuns.getRun(runName);
        String runId = resetRun.getRasRunId();
        if (runId != null) {
            logger.info("Creating new RAS record for requeued run " + runName);

            TestStructure newTestStructure = resetRun.toTestStructure();

            rasStore.createTestStructure(runId, newTestStructure);

            logger.info("Created new RAS record for requeued run " + runName + " OK");
        }
    }

    private void markRunFinishedInDss(String runName, String interruptReason) throws DynamicStatusStoreException {
        logger.info("Marking run '" + runName + "' as finished in the DSS");

        frameworkRuns.markRunFinished(runName, interruptReason);

        logger.info("Marked run '" + runName + "' as finished in the DSS OK");
    }

    private boolean markRunCancellingInDss(String runName, TestRunLifecycleStatus currentRunStatus ) throws DynamicStatusStoreException {
        boolean isMarkedCancelling = frameworkRuns.markRunCancelling(runName, currentRunStatus);
        return isMarkedCancelling ;
    }
}
