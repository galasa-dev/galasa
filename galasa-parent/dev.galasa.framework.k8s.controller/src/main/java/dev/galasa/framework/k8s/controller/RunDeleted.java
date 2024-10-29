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

import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.proto.V1.Namespace;

public class RunDeleted implements Runnable {
    private final Log            logger = LogFactory.getLog(getClass());

    private final Settings       settings;
    private final CoreV1Api      api;
    private final ProtoClient    pc;
    private final IFrameworkRuns runs;

    public RunDeleted(Settings settings, CoreV1Api api, ProtoClient pc, IFrameworkRuns runs) {
        this.settings = settings;
        this.api = api;
        this.pc = pc;
        this.runs = runs;
    }

    @Override
    public void run() {
        logger.info("Starting Deleted runs scan");

        try {
            List<V1Pod> pods = TestPodScheduler.getPods(api, settings);
            TestPodScheduler.filterTerminated(pods);

            for (V1Pod pod : pods) {
                Map<String, String> labels = pod.getMetadata().getLabels();
                String runName = labels.get("galasa-run");
                if (runName == null) {
                    continue;
                }

                IRun run = runs.getRun(runName);
                if (run != null) {
                    continue;
                }

                logger.info("Deleting pod " + pod.getMetadata().getName() + " as run has been deleted");
                deletePod(pod);
            }

        } catch (Exception e) {
            logger.error("Problem with Deleted runs scan", e);
        }

    }

    private void deletePod(V1Pod pod) {
        try {
            String podName = pod.getMetadata().getName();
            logger.info("Deleting pod " + podName);
            // *** Have to use the ProtoClient as the deleteNamespacedPod does not work
            pc.delete(Namespace.newBuilder(), "/api/v1/namespaces/" + settings.getNamespace() + "/pods/" + podName);
        } catch (ApiException e) {
            logger.error("Failed to delete engine pod :-\n" + e.getResponseBody(), e);
        } catch (Exception e) {
            logger.error("Failed to delete engine pod", e);
        }

    }

}