/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.k8s.controller.TestPodKubeLabels;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;

public class MockKubernetesPodTestUtils {

    public List<V1Pod> createEtcdAndRasPods(String galasaServiceInstallName, boolean isReady) {
        List<V1Pod> pods = new ArrayList<>();
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", isReady);
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", isReady);

        pods.add(etcdPod);
        pods.add(rasPod);
        return pods;
    }

    public V1Pod createPodWithReadiness(String appLabel, boolean isReady) {
        V1Pod pod = new V1Pod();
        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem("app", appLabel);

        V1ContainerStatus readyContainerStatus = new V1ContainerStatus().ready(isReady);
        List<V1ContainerStatus> containerStatuses = new ArrayList<>();
        containerStatuses.add(readyContainerStatus);

        V1PodStatus podStatus = new V1PodStatus();
        podStatus.setContainerStatuses(containerStatuses);

        pod.setMetadata(podMetadata);
        pod.setStatus(podStatus);
        return pod;
    }

    public V1Pod createMockTestPod(String runName, String galasaServiceName, String phase) {
        V1Pod mockPod = new V1Pod();

        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem(TestPodKubeLabels.GALASA_RUN.toString(), runName);
        podMetadata.putLabelsItem(TestPodKubeLabels.ENGINE_CONTROLLER.toString(), MockISettings.ENGINE_LABEL);
        podMetadata.putLabelsItem(TestPodKubeLabels.GALASA_SERVICE_NAME.toString(), galasaServiceName);
        podMetadata.setName(runName);

        V1PodStatus podStatus = new V1PodStatus();
        podStatus.setPhase(phase);
        mockPod.setStatus(podStatus);

        mockPod.setMetadata(podMetadata);
        return mockPod;
    }

    public V1Pod createMockTestPod(String runName, String galasaServiceName) {
        return createMockTestPod(runName, galasaServiceName, "running");
    }
}
