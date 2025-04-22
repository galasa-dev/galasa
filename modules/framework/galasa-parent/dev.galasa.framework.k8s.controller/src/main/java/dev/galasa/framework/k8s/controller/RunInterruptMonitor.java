/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

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

    public RunInterruptMonitor(KubernetesEngineFacade kubeApi, IFrameworkRuns runs, Queue<RunInterruptEvent> eventQueue) {
        this.runs = runs;
        this.kubeApi = kubeApi;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        logger.info("Starting scan for interrupted runs");

        try {
            List<RunInterruptEvent> interruptedRunEvents = getInterruptedRunEvents();

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

    private void deletePodsForInterruptedRuns(List<String> interruptedRunNames) throws K8sControllerException {
        List<V1Pod> podsToDelete = getPodsForInterruptedRuns(kubeApi.getPods(), interruptedRunNames);
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
            List<RunRasAction> rasActions = run.getRasActions();

            // Create an interrupted run event if the run hasn't finished and has an interrupt reason
            TestRunLifecycleStatus runStatus = TestRunLifecycleStatus.getFromString(run.getStatus());
            if ((runStatus != TestRunLifecycleStatus.FINISHED) && (runInterruptReason != null)) {
                RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, runInterruptReason);
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
