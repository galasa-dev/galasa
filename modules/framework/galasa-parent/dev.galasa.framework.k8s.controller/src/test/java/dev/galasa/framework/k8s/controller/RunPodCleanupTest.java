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

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.IRun;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunPodCleanupTest {

    private V1Pod createMockTestPod(String runName) {
        V1Pod mockPod = new V1Pod();

        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem(TestPodScheduler.GALASA_RUN_POD_LABEL, runName);
        podMetadata.setName(runName);

        mockPod.setMetadata(podMetadata);
        return mockPod;
    }

    private MockRun createMockRun(String runName, String status) {
        // We only care about the run's name and status
        MockRun mockRun = new MockRun(
            "bundle",
            "testclass",
            runName,
            "testStream",
            "testStreamOBR",
            "testStreamMavenRepo",
            "requestor",
            false
        );

        mockRun.setStatus(status);
        return mockRun;
    }

    @Test
    public void testPodsForFinishedRunsAreDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";

        // Create terminated pods
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        mockTerminatedPods.add(createMockTestPod(runName1));
        mockTerminatedPods.add(createMockTestPod(runName2));

        // Create a list of all pods to also simulate running pods
        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);
        V1Pod runningPod = createMockTestPod(runName3);
        mockPods.add(runningPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runName1, TestRunLifecycleStatus.FINISHED.toString()));
        mockRuns.add(createMockRun(runName2, TestRunLifecycleStatus.FINISHED.toString()));
        mockRuns.add(createMockRun(runName3, TestRunLifecycleStatus.RUNNING.toString()));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        Settings settings = new Settings(null,kubeEngineFacade , "myPodName" , "myConfigMapName");
        RunPodCleanup runPodCleanup = new RunPodCleanup(settings, kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.deletePodsForCompletedRuns(mockTerminatedPods);

        // Then...
        List<V1Pod> remainingPods = mockApiClient.getMockPods();
        assertThat(remainingPods).hasSize(1);
        assertThat(remainingPods.get(0)).usingRecursiveComparison().isEqualTo(runningPod);

        // No runs should have been deleted, only their pods
        assertThat(mockFrameworkRuns.getAllRuns()).hasSize(3);
    }

    @Test
    public void testPodsForTerminatedRunsAreDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";

        // Create terminated pods
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        mockTerminatedPods.add(createMockTestPod(runName1));
        mockTerminatedPods.add(createMockTestPod(runName2));

        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);

        // Simulate a situation where the runs have been deleted from the DSS but the pods still exist,
        // so the pods should get deleted
        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        Settings settings = new Settings(null,kubeEngineFacade , "myPodName" , "myConfigMapName");
        RunPodCleanup runPodCleanup = new RunPodCleanup(settings, kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.deletePodsForCompletedRuns(mockTerminatedPods);

        // Then...
        assertThat(mockApiClient.getMockPods()).isEmpty();
    }

    @Test
    public void testPodWithNoRunNameShouldNotBeDeleted() throws Exception {
        // Given...
        // Simulate a situation where the current kubernetes namespace has a terminated pod, which may
        // not be a Galasa-related pod, so it doesn't have a "galasa-run" label with a run name.
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        V1Pod podWithNoRunName = createMockTestPod(null);
        mockTerminatedPods.add(podWithNoRunName);

        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);

        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", "myGalasaService");

        Settings settings = new Settings(null,kubeEngineFacade , "myPodName" , "myConfigMapName");
        RunPodCleanup runPodCleanup = new RunPodCleanup(settings, kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.deletePodsForCompletedRuns(mockTerminatedPods);

        // Then...
        List<V1Pod> pods = mockApiClient.getMockPods();
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).usingRecursiveComparison().isEqualTo(podWithNoRunName);
    }
}
