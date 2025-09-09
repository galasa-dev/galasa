/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.ISettings;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.utils.ITimeService;


/**
 * This runs as a thread in the engine controller pod and it monitors the DSS
 * for runs with an interrupt reason set.
 *
 * When it detects a run with an interrupt reason, it stops the run's pod (if there is one) and adds a
 * new interrupt event onto the event queue for processing.
 */
public class RunInterruptEventCollector {

    private final Log logger = LogFactory.getLog(getClass());

    private final IFrameworkRuns runs;
    private final KubernetesEngineFacade kubeApi;
    private final ISettings settings ;
    private final ITimeService timeService;
    

    public RunInterruptEventCollector(
        KubernetesEngineFacade kubeApi,
        IFrameworkRuns runs,
        ISettings settings,
        ITimeService timeService
    ) {
        this.runs = runs;
        this.kubeApi = kubeApi;
        this.settings = settings;
        this.timeService = timeService;
    }


    public List<RunInterruptEvent> collectInterruptRunEvents() {

        List<RunInterruptEvent> interruptedRunEvents = new ArrayList<RunInterruptEvent>();
        
        if (!kubeApi.isEtcdAndRasReady()) {
            logger.warn("etcd or RAS pods are not ready, waiting for them to be ready before scanning for interrupted runs");
        } else {
            logger.info("Starting scan for interrupted runs");
    
            try {
                interruptedRunEvents = getInterruptedRunsNeedingCleanupNow();
    
                // Add the interrupt events to set the DSS entries of the interrupted
                // runs to finished and complete all deferred RAS actions
    
                logger.info("Finished scanning for interrupted runs");
            } catch (Exception e) {
                logger.error("Problem with interrupted run scan", e);
            }
        }

        return interruptedRunEvents ;
    }

    /**
     * Gets all the test run interrupt events that are now outside the test run cleanup grace period timing window.
     * 
     * @return the test run interrupt events for test runs that remain after the cleanup grace period has passed
     * @throws FrameworkException if there was an issue getting test runs from the framework
     */
    private List<RunInterruptEvent> getInterruptedRunsNeedingCleanupNow() throws FrameworkException {
        logger.debug("getInterruptedRunsNeedingCleanupNow entered");

        List<RunInterruptEvent> interruptedRunEvents = getInterruptedRunEvents();
        List<RunInterruptEvent> interruptedRunsNeedingCleanup = new ArrayList<>();

        long testRunCleanupGracePeriodSeconds = settings.getInterruptedTestRunCleanupGracePeriodSeconds();
        Instant currentTime = timeService.now();
        for (RunInterruptEvent interruptEvent : interruptedRunEvents) {
            String runName = interruptEvent.getRunName();
            logger.info("Checking if interrupted run " + runName + " needs cleaning up");

            // assume we don't want to cleanup the run described by this event right now.
            boolean isRunNeedingCleanup = false ;

            // Check to see if this run has had long enough waiting around to be cleaned up more forcibly.
            Instant interruptedAt = interruptEvent.getInterruptedAt();
            if (interruptedAt == null) {
                // We don't know when this event's run was interrupted, so consider it timed out
                isRunNeedingCleanup = true ;
                logger.info("No 'interruptedAt' time recorded for run " + runName);
            } else {
                Instant timeToDeletePod = interruptedAt.plusSeconds(testRunCleanupGracePeriodSeconds);
                logger.info("Expected time after which to clean up run " + runName + ": " + timeToDeletePod.toString());

                if (currentTime.isAfter(timeToDeletePod)) {
                    interruptEvent.setPastGracePeriod(true);
                    isRunNeedingCleanup = true ;
                    logger.info(runName + " has exceeded the grace period, it needs cleaning up");
                } 
            }

            // Queued runs should be cleaned up immediately.
            if( TestRunLifecycleStatus.QUEUED == interruptEvent.getTestRunStatus()) {
                // The test run is in queued state
                isRunNeedingCleanup = true ;
                logger.info(runName + " has been interrupted in the 'queued' state, it needs cleaning up");
            } 

            if(isRunNeedingCleanup) {
                interruptedRunsNeedingCleanup.add(interruptEvent);
                logger.info(runName + " added to list of runs to clean up");
            } else {
                logger.info(runName + " does not need to be cleaned up yet");
            }
        }
        logger.debug("getInterruptedRunsNeedingCleanupNow exiting");
        return interruptedRunsNeedingCleanup;
    }




    private List<RunInterruptEvent> getInterruptedRunEvents() throws FrameworkException {
        List<RunInterruptEvent> interruptedRunEvents = new ArrayList<>();
        List<IRun> allRuns = runs.getAllRuns();
        for (IRun run : allRuns) {
            String runName = run.getName();
            String runInterruptReason = run.getInterruptReason();
            Instant runInterruptedAt = run.getInterruptedAt();
            List<RunRasAction> rasActions = run.getRasActions();

            // Create an interrupted run event if the run hasn't finished and has an interrupt reason
            TestRunLifecycleStatus runStatus = TestRunLifecycleStatus.getFromString(run.getStatus());
            if ((runStatus != TestRunLifecycleStatus.FINISHED) && (runInterruptReason != null)) {
                RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, runInterruptReason, runInterruptedAt, runStatus);
                interruptedRunEvents.add(interruptEvent);
            }
        }
        return interruptedRunEvents;
    }





}
