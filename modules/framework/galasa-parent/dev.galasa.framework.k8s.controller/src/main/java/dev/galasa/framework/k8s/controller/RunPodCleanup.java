/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunPodCleanup implements Runnable {

    private final Log            logger = LogFactory.getLog(getClass());

    private final IFrameworkRuns runs;
    private final KubernetesEngineFacade kubeApi;

    public RunPodCleanup(Settings settings, KubernetesEngineFacade kubeApi, IFrameworkRuns runs) {
        this.runs = runs;
        this.kubeApi = kubeApi;
    }

    @Override
    public void run() {
        logger.info("Starting run pod cleanup scan");

        try {
            List<V1Pod> pods = kubeApi.getPods();
            pods = kubeApi.getTerminatedPods(pods);

            deletePodsForCompletedRuns(pods);

            logger.info("Finished run pod cleanup scan");
        } catch (Exception e) {
            logger.error("Problem with run pod cleanup scan", e);
        }
    }

    void deletePodsForCompletedRuns(List<V1Pod> terminatedPods) throws DynamicStatusStoreException {
        for (V1Pod pod : terminatedPods) {
            Map<String, String> labels = pod.getMetadata().getLabels();
            String runName = labels.get(TestPodScheduler.GALASA_RUN_POD_LABEL);

            if (runName != null) {
                IRun run = runs.getRun(runName);
                if (run != null) {

                    // There is a completed pod for a run in the DSS, delete the pod if the run has finished
                    TestRunLifecycleStatus runStatus = TestRunLifecycleStatus.getFromString(run.getStatus());
                    if (runStatus == TestRunLifecycleStatus.FINISHED) {
                        logger.info("Deleting pod " + pod.getMetadata().getName() + " as the run has finished");
                        kubeApi.deletePod(pod);                        
                    }
                } else {

                    // The run for the completed pod no longer exists in the DSS, so just delete the pod
                    logger.info("Deleting pod " + pod.getMetadata().getName() + " as the run has been deleted from the DSS");
                    kubeApi.deletePod(pod);
                }
            }
        }
    }
}
