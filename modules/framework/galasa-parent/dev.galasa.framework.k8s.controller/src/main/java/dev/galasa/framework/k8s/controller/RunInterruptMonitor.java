/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.utils.ITimeService;
import io.kubernetes.client.openapi.models.V1Pod;

/**
 * This runs as a thread in the engine controller pod and it monitors the DSS
 * for runs with an interrupt reason set.
 *
 * When it detects a run with an interrupt reason, it stops the run's pod (if there is one) and adds a
 * new interrupt event onto the event queue for processing by another thread.
 */
public class RunInterruptMonitor implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    private final IFrameworkRuns runs;
    private final KubernetesEngineFacade kubeApi;
    private final Queue<RunInterruptEvent> eventQueue;
    private final ISettings settings ;
    private final ITimeService timeService;

    public RunInterruptMonitor(
        KubernetesEngineFacade kubeApi,
        IFrameworkRuns runs,
        Queue<RunInterruptEvent> eventQueue,
        ISettings settings,
        ITimeService timeService
    ) {
        this.runs = runs;
        this.kubeApi = kubeApi;
        this.eventQueue = eventQueue;
        this.settings = settings;
        this.timeService = timeService;
    }

    @Override
    public void run() {
        if (!kubeApi.isEtcdAndRasReady()) {
            logger.warn("etcd or RAS pods are not ready, waiting for them to be ready before scanning for interrupted runs");
        } else {
            logger.info("Starting scan for interrupted runs");
    
            try {
                List<RunInterruptEvent> interruptedRunEvents = getInterruptedRunEventsPastCleanupGracePeriod();

                List<String> interruptedRunNames = getInterruptedRunNames(interruptedRunEvents);
                deletePodsForInterruptedRuns(interruptedRunNames);
    
                // Add the interrupt events to set the DSS entries of the interrupted
                // runs to finished and complete all deferred RAS actions
                eventQueue.addAll(interruptedRunEvents);
    
                logger.info("Finished scanning for interrupted runs");
            } catch (Exception e) {
                logger.error("Problem with interrupted run scan", e);
            }
        }
    }

    /**
     * Gets all the test run interrupt events that are now outside the test run cleanup grace period timing window.
     * 
     * @return the test run interrupt events for test runs that remain after the cleanup grace period has passed
     * @throws FrameworkException if there was an issue getting test runs from the framework
     */
    private List<RunInterruptEvent> getInterruptedRunEventsPastCleanupGracePeriod() throws FrameworkException {
        List<RunInterruptEvent> interruptedRunEvents = getInterruptedRunEvents();
        List<RunInterruptEvent> interruptedRunEventsPastGracePeriod = new ArrayList<>();

        long testRunCleanupGracePeriodSeconds = settings.getInterruptedTestRunCleanupGracePeriodSeconds();
        Instant currentTime = timeService.now();
        for (RunInterruptEvent interruptEvent : interruptedRunEvents) {

            Instant interruptedAt = interruptEvent.getInterruptedAt();
            if (interruptedAt == null) {
                // We don't know when this event's run was interrupted, so consider it timed out
                interruptedRunEventsPastGracePeriod.add(interruptEvent);
            } else {
                Instant timeToDeletePod = interruptedAt.plusSeconds(testRunCleanupGracePeriodSeconds);
                if (currentTime.isAfter(timeToDeletePod)) {
                    interruptedRunEventsPastGracePeriod.add(interruptEvent);
                }
            }
        }
        return interruptedRunEventsPastGracePeriod;
    }

    private void deletePodsForInterruptedRuns(List<String> interruptedRunNames) throws K8sControllerException {
        List<V1Pod> podsToDelete = getPodsForInterruptedRuns(kubeApi.getTestPods(settings.getEngineLabel()), interruptedRunNames);
        for (V1Pod pod : podsToDelete) {
            String podName = pod.getMetadata().getName();
            logger.info("Deleting pod " + podName + " as the run has been interrupted");

            kubeApi.deletePod(pod);

            logger.info("Deleted pod " + podName + "OK");
        }
    }

    private List<String> getInterruptedRunNames(List<RunInterruptEvent> interruptedRunEvents) {
        List<String> interruptedRunNames = new ArrayList<>();
        for (RunInterruptEvent interruptEvent : interruptedRunEvents) {
            interruptedRunNames.add(interruptEvent.getRunName());
        }
        return interruptedRunNames;
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
                RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, runInterruptReason, runInterruptedAt);
                interruptedRunEvents.add(interruptEvent);
            }
        }
        return interruptedRunEvents;
    }

    private List<V1Pod> getPodsForInterruptedRuns(List<V1Pod> unfilteredPods, List<String> interruptedRunNames) {
        List<V1Pod> podsToInterrupt = new ArrayList<>();
        for (V1Pod pod : unfilteredPods) {
            Map<String, String> labels = pod.getMetadata().getLabels();
            String runName = labels.get(TestPodScheduler.GALASA_RUN_POD_LABEL);

            if (runName != null && interruptedRunNames.contains(runName)) {
                podsToInterrupt.add(pod);
            }
        }
        return podsToInterrupt;
    }
}
