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
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import io.kubernetes.client.openapi.models.V1Pod;

/**
 * RunPodInterrupt runs as a thread in the engine controller pod and it monitors the DSS
 * for runs with an interrupt reason set.
 * 
 * When it detects a run with an interrupt reason, it stops the run's pod and adds a new
 * interrupt event onto the given event queue for processing by the InterruptedRunEventProcessor.
 */
public class RunPodInterrupt implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    private final IFrameworkRuns runs;
    private final KubernetesEngineFacade kubeApi;
    private final Queue<RunInterruptEvent> eventQueue;

    public RunPodInterrupt(KubernetesEngineFacade kubeApi, IFrameworkRuns runs, Queue<RunInterruptEvent> eventQueue) {
        this.runs = runs;
        this.kubeApi = kubeApi;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        logger.info("Starting scan for interrupted runs");

        try {
            List<RunInterruptEvent> interruptedRunEvents = getInterruptedRunEvents(kubeApi.getPods());

            for (RunInterruptEvent interruptEvent : interruptedRunEvents) {
                V1Pod pod = interruptEvent.getInterruptedPod();

                logger.info("Deleting pod " + pod.getMetadata().getName() + " as the run has been interrupted");
                kubeApi.deletePod(pod);

                // Add the interrupt event to set the run's DSS entry to finished and complete all deferred RAS actions
                eventQueue.add(interruptEvent);
            }

            logger.info("Finished scanning for interrupted runs");
        } catch (Exception e) {
            logger.error("Problem with intterupted run scan", e);
        }
    }

    private List<RunInterruptEvent> getInterruptedRunEvents(List<V1Pod> pods) throws DynamicStatusStoreException {
        List<RunInterruptEvent> interruptedRunEvents = new ArrayList<>();
        for (V1Pod pod : pods) {
            Map<String, String> labels = pod.getMetadata().getLabels();
            String runName = labels.get(TestPodScheduler.GALASA_RUN_POD_LABEL);

            if (runName != null) {
                IRun run = runs.getRun(runName);
                if (run != null) {
                    TestRunLifecycleStatus runStatus = TestRunLifecycleStatus.getFromString(run.getStatus());
                    String runInterruptReason = run.getInterruptReason();
                    List<RunRasAction> rasActions = run.getRasActions();

                    // Create an interrupted run event if the run hasn't finished and has an interrupt reason
                    if ((runStatus != TestRunLifecycleStatus.FINISHED) && (runInterruptReason != null)) {
                        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, runInterruptReason, pod);
                        interruptedRunEvents.add(interruptEvent);
                    }
                }
            }
        }
        return interruptedRunEvents;
    }
}