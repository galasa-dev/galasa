/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.k8s.controller.api.IKubernetesApiClient;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;

public class KubernetesEngineFacadeTest {

    private static final String ENGINE_LABEL = "engine";

    private V1Pod createPodWithReadiness(String appLabel, boolean isReady) {
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

    private V1Pod createMockTestPod(String runName, String phase) {
        V1Pod mockPod = new V1Pod();

        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem(TestPodScheduler.GALASA_RUN_POD_LABEL, runName);
        podMetadata.putLabelsItem(KubernetesEngineFacade.ENGINE_CONTROLLER_LABEL_KEY, ENGINE_LABEL);
        podMetadata.setName(runName);

        V1PodStatus podStatus = new V1PodStatus();
        podStatus.setPhase(phase);
        mockPod.setStatus(podStatus);

        mockPod.setMetadata(podMetadata);
        return mockPod;
    }

    @Test
    public void testGetPodsReturnsPodsOk() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(createMockTestPod("RUN1", "running"));
        mockPods.add(createMockTestPod("RUN2", "running"));

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        // When...
        List<V1Pod> pods = facade.getTestPods(ENGINE_LABEL);

        // Then...
        assertThat(pods).hasSize(2);
        assertThat(pods).isEqualTo(mockPods);
    }

    @Test
    public void testGetActivePodsReturnsPodsOk() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod runningPod = createMockTestPod("RUN1", "running");
        mockPods.add(runningPod);
        mockPods.add(createMockTestPod("RUN2", "failed"));

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        // When...
        List<V1Pod> pods = facade.getActivePods(mockPods);

        // Then...
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).isEqualTo(runningPod);
    }

    @Test
    public void testGetTerminatedPodsReturnsPodsOk() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(createMockTestPod("RUN1", "running"));

        V1Pod finishedPod = createMockTestPod("RUN2", "failed");
        mockPods.add(finishedPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        // When...
        List<V1Pod> pods = facade.getTerminatedPods(mockPods);

        // Then...
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).isEqualTo(finishedPod);
    }

    @Test
    public void testDeletePodRemovesPodOk() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(createMockTestPod("RUN1", "running"));

        V1Pod podToDelete = createMockTestPod("RUN2", "failed");
        mockPods.add(podToDelete);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        // When...
        facade.deletePod(podToDelete);

        // Then...
        List<V1Pod> remainingPods = facade.getTestPods(ENGINE_LABEL);
        assertThat(remainingPods).hasSize(1);
        assertThat(remainingPods).doesNotContain(podToDelete);
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsTrueWhenBothAreReady() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        boolean isPodReady = true;
        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

        mockPods.add(etcdPod);
        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isTrue();
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsFalseWhenEtcdIsNotReady() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", false);
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", true);

        mockPods.add(etcdPod);
        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsFalseWhenRasIsNotReady() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", true);
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", false);

        mockPods.add(etcdPod);
        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsFalseWhenNeitherIsReady() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        boolean isPodReady = false;
        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

        mockPods.add(etcdPod);
        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsFalseWhenRasDoesNotExist() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        // Simulate a situation where the RAS pod has been scaled down to 0 and is not running...
        boolean isPodReady = true;
        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);

        mockPods.add(etcdPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsFalseWhenEtcdDoesNotExist() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        // Simulate a situation where the etcd pod has been scaled down to 0 and is not running...
        boolean isPodReady = true;
        String galasaServiceInstallName = "myGalasaService";
        V1Pod rasPod = createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }
}
