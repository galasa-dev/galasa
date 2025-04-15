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
import dev.galasa.framework.k8s.controller.mocks.MockSettings;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodStatus;

public class KubernetesEngineFacadeTest {

    private V1Pod createMockTestPod(String runName, String phase) {
        V1Pod mockPod = new V1Pod();

        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem(TestPodScheduler.GALASA_RUN_POD_LABEL, runName);
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
        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, mockSettings);

        // When...
        List<V1Pod> pods = facade.getPods();

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
        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, mockSettings);

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
        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, mockSettings);

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
        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, mockSettings);

        // When...
        facade.deletePod(podToDelete);

        // Then...
        List<V1Pod> remainingPods = facade.getPods();
        assertThat(remainingPods).hasSize(1);
        assertThat(remainingPods).doesNotContain(podToDelete);
    }
}
