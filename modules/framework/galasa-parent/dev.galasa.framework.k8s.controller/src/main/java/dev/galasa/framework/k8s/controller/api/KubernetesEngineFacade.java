/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.api;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.k8s.controller.K8sControllerException;
import dev.galasa.framework.k8s.controller.Settings;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;

public class KubernetesEngineFacade {

    private static final String ENGINE_CONTROLLER_LABEL_PREFIX = "galasa-engine-controller=";

    private final Log logger = LogFactory.getLog(getClass());

    private IKubernetesApiClient apiClient;
    private Settings settings;

    public KubernetesEngineFacade(IKubernetesApiClient apiClient, Settings settings) {
        this.apiClient = apiClient;
        this.settings = settings;
    }

    public @NotNull List<V1Pod> getPods() throws K8sControllerException {
        LinkedList<V1Pod> pods = new LinkedList<>();

        try {
            List<V1Pod> podList = apiClient.getPods(settings.getNamespace(), ENGINE_CONTROLLER_LABEL_PREFIX + settings.getEngineLabel());
            for (V1Pod pod : podList) {
                pods.add(pod);
            }
        } catch (Exception e) {
            throw new K8sControllerException("Failed retrieving pods", e);
        }

        return pods;
    }

    public void deletePod(V1Pod pod) {
        try {
            String podName = pod.getMetadata().getName();
            logger.info("Deleting pod " + podName);
            apiClient.deletePod(settings.getNamespace(), podName);
        } catch (ApiException e) {
            logger.error("Failed to delete engine pod :-\n" + e.getResponseBody(), e);
        } catch (Exception e) {
            logger.error("Failed to delete engine pod", e);
        }
    }

    public @NotNull List<V1Pod> getActivePods(@NotNull List<V1Pod> pods) {
        List<V1Pod> activePods = new ArrayList<>();

        for (V1Pod pod : pods) {
            if (isPodActive(pod)) {
                activePods.add(pod);
            }
        }
        return activePods;
    }

    public @NotNull List<V1Pod> getTerminatedPods(@NotNull List<V1Pod> pods) {
        List<V1Pod> terminatedPods = new ArrayList<>();

        for (V1Pod pod : pods) {
            if (!isPodActive(pod)) {
                terminatedPods.add(pod);
            }
        }
        return terminatedPods;
    }

    private boolean isPodActive(V1Pod pod) {
        boolean isActive = false;
        V1PodStatus status = pod.getStatus();

        if (status != null) {
            String phase = status.getPhase();
            if ("failed".equalsIgnoreCase(phase) || "succeeded".equalsIgnoreCase(phase)) {
                isActive = false;
            } else {
                isActive = true;
            }
        }
        return isActive;
    }
}
