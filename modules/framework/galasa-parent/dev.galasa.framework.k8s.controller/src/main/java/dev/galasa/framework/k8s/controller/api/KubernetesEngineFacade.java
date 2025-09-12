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
import dev.galasa.framework.k8s.controller.TestPodKubeLabels;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;

public class KubernetesEngineFacade {

    private static final String APP_LABEL = "app";
    private static final String ETCD_APP_SUFFIX = "-etcd";
    private static final String RAS_APP_SUFFIX = "-ras";

    private final Log logger = LogFactory.getLog(getClass());

    private IKubernetesApiClient apiClient;

    private String namespace;
    private String galasaServiceInstallName;

    public KubernetesEngineFacade(IKubernetesApiClient apiClient, String namespace, String galasaServiceInstallName) {
        this.apiClient = apiClient;
        this.namespace = namespace;
        this.galasaServiceInstallName = galasaServiceInstallName;
    }

    public @NotNull List<V1Pod> getTestPods() throws K8sControllerException {
        LinkedList<V1Pod> pods = new LinkedList<>();

        try {
            List<V1Pod> podList = apiClient.getPods(namespace, TestPodKubeLabels.GALASA_SERVICE_NAME.toString() + "=" + this.galasaServiceInstallName);
            for (V1Pod pod : podList) {
                pods.add(pod);
            }
        } catch (Exception e) {
            throw new K8sControllerException("Failed retrieving pods", e);
        }

        return pods;
    }

    public V1Pod getTestPod(String runName) throws K8sControllerException {
        List<V1Pod> pods = new LinkedList<>();
        try {
            String runNameLabelSelector = TestPodKubeLabels.GALASA_RUN.toString() + "=" + runName;
            String serviceNameLabelSelector = TestPodKubeLabels.GALASA_SERVICE_NAME.toString() + "=" + this.galasaServiceInstallName;
            String commaSeparatedLabelSelectors = runNameLabelSelector + "," + serviceNameLabelSelector;

            pods = apiClient.getPods(namespace, commaSeparatedLabelSelectors);
        } catch (Exception e) {
            throw new K8sControllerException("Failed retrieving pods", e);
        }

        // There should only be one pod with this name.
        V1Pod pod = null ;
        if (pods != null) {
            logger.info("Found " + pods.size() + " test pod(s) for run " + runName);
            if( !pods.isEmpty() ) {
                pod = pods.get(0);
            }
        } else {
            logger.info("No test pod found for run " + runName);
        }
        return pod;
    }


    public void deletePod(V1Pod pod) {
        try {
            String podName = pod.getMetadata().getName();
            logger.info("Deleting pod " + podName);
            apiClient.deletePod(namespace, podName);
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

    public V1Pod createNamespacedPod(V1Pod newPodDefinition) throws ApiException {
        V1Pod pod = apiClient.createNamespacedPod(namespace, newPodDefinition);
        return pod;
    }

    public V1ConfigMap getConfigMap(String configMapName) throws K8sControllerException {
        V1ConfigMap map ;
        try {
            map = apiClient.readNamespacedConfigMap(configMapName, namespace);
        } catch( ApiException e) {
            throw new K8sControllerException("Failed to read configmap '" + configMapName + "' in namespace '" + namespace + "'", e);
        }        
        return map;
    }

    public boolean isEtcdAndRasReady() {
        logger.info("Checking if etcd and RAS pods are ready");
        String etcdAppLabelSelector = APP_LABEL + "=" + this.galasaServiceInstallName + ETCD_APP_SUFFIX;
        String rasAppLabelSelector = APP_LABEL + "=" + this.galasaServiceInstallName + RAS_APP_SUFFIX;
        return isEachPodWithLabelReady(etcdAppLabelSelector) && isEachPodWithLabelReady(rasAppLabelSelector);
    }

    private boolean isEachPodWithLabelReady(String labelSelector) {
        boolean isReady = false;

        try {
            List<V1Pod> podsToCheck = apiClient.getPods(namespace, labelSelector);
            for (V1Pod pod : podsToCheck) {
                V1PodStatus podStatus = pod.getStatus();
                if (!isEachContainerInPodReady(podStatus)) {
                    isReady = false;
                    break;
                } else {
                    isReady = true;
                }
            }
        } catch (ApiException e) {
            logger.warn("Kubernetes API returned an exception when checking for pod readiness. Assuming pods are not ready", e);
        }
        return isReady;
    }

    private boolean isEachContainerInPodReady(V1PodStatus podStatus) {
        boolean isReady = false;

        if (podStatus != null) {
            for (V1ContainerStatus containerStatus : podStatus.getContainerStatuses()) {
                if (!containerStatus.getReady()) {
                    isReady = false;
                    break;
                } else {
                    isReady = true;
                }
            }
        }
        return isReady;
    }

    public String getGalasaServiceInstallName() {
        return this.galasaServiceInstallName;
    }

}
