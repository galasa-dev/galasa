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
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesPodTestUtils;
import io.kubernetes.client.openapi.models.V1Pod;

public class KubernetesEngineFacadeTest {

    private MockKubernetesPodTestUtils mockKubeTestUtils = new MockKubernetesPodTestUtils();

    @Test
    public void testGetPodsReturnsPodsOk() throws Exception {
        // Given...
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod("RUN1", galasaServiceInstallName, "running"));
        mockPods.add(mockKubeTestUtils.createMockTestPod("RUN2", galasaServiceInstallName, "running"));

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        List<V1Pod> pods = facade.getTestPods();

        // Then...
        assertThat(pods).hasSize(2);
        assertThat(pods).isEqualTo(mockPods);
    }

    @Test
    public void testGetActivePodsReturnsPodsOk() throws Exception {
        // Given...
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod runningPod = mockKubeTestUtils.createMockTestPod("RUN1", galasaServiceInstallName, "running");
        mockPods.add(runningPod);
        mockPods.add(mockKubeTestUtils.createMockTestPod("RUN2", galasaServiceInstallName, "failed"));

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        List<V1Pod> pods = facade.getActivePods(mockPods);

        // Then...
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).isEqualTo(runningPod);
    }

    @Test
    public void testGetTerminatedPodsReturnsPodsOk() throws Exception {
        // Given...
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod("RUN1", galasaServiceInstallName, "running"));

        V1Pod finishedPod = mockKubeTestUtils.createMockTestPod("RUN2", galasaServiceInstallName, "failed");
        mockPods.add(finishedPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        List<V1Pod> pods = facade.getTerminatedPods(mockPods);

        // Then...
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).isEqualTo(finishedPod);
    }

    @Test
    public void testDeletePodRemovesPodOk() throws Exception {
        // Given...
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod("RUN1", galasaServiceInstallName, "running"));

        V1Pod podToDelete = mockKubeTestUtils.createMockTestPod("RUN2", galasaServiceInstallName, "failed");
        mockPods.add(podToDelete);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        facade.deletePod(podToDelete);

        // Then...
        List<V1Pod> remainingPods = facade.getTestPods();
        assertThat(remainingPods).hasSize(1);
        assertThat(remainingPods).doesNotContain(podToDelete);
    }

    @Test
    public void testIsEtcdAndRasReadyReturnsTrueWhenBothAreReady() throws Exception {
        // Given...
        List<V1Pod> mockPods = new ArrayList<>();

        boolean isPodReady = true;
        String galasaServiceInstallName = "myGalasaService";
        V1Pod etcdPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);
        V1Pod rasPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

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
        V1Pod etcdPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-etcd", false);
        V1Pod rasPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-ras", true);

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
        V1Pod etcdPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-etcd", true);
        V1Pod rasPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-ras", false);

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
        V1Pod etcdPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);
        V1Pod rasPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

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
        V1Pod etcdPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-etcd", isPodReady);

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
        V1Pod rasPod = mockKubeTestUtils.createPodWithReadiness(galasaServiceInstallName + "-ras", isPodReady);

        mockPods.add(rasPod);

        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        // When...
        boolean isReadyGotBack = facade.isEtcdAndRasReady();

        // Then...
        assertThat(isReadyGotBack).isFalse();
    }
}
