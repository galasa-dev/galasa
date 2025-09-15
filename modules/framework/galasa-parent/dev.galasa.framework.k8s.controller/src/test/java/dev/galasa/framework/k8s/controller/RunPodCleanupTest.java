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
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesPodTestUtils;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.IRun;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunPodCleanupTest {

    private MockKubernetesPodTestUtils mockKubeTestUtils = new MockKubernetesPodTestUtils();

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
        String galasaServiceInstallName = "myGalasaService";

        // Create terminated pods
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName1, "succeeded"));
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName2, "failed"));

        // Create a list of all pods to also simulate running pods
        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);
        V1Pod runningPod = mockKubeTestUtils.createMockTestPod(runName3, galasaServiceInstallName);
        mockPods.add(runningPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runName1, TestRunLifecycleStatus.FINISHED.toString()));
        mockRuns.add(createMockRun(runName2, TestRunLifecycleStatus.FINISHED.toString()));
        mockRuns.add(createMockRun(runName3, TestRunLifecycleStatus.RUNNING.toString()));

        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        RunPodCleanup runPodCleanup = new RunPodCleanup(kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.run();

        // Then...
        List<V1Pod> remainingPods = kubeEngineFacade.getTestPods();
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
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName1, "failed"));
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName2, "succeeded"));

        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        // Simulate a situation where the runs have been deleted from the DSS but the pods still exist,
        // so the pods should get deleted
        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        RunPodCleanup runPodCleanup = new RunPodCleanup(kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.run();

        // Then...
        assertThat(kubeEngineFacade.getTestPods()).isEmpty();
    }

    @Test
    public void testPodWithNoRunNameShouldNotBeDeleted() throws Exception {
        // Given...
        // Simulate a situation where the current kubernetes namespace has a terminated pod, which may
        // not be a Galasa-related pod, so it doesn't have a "galasa-run" label with a run name.
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        V1Pod podWithNoRunName = mockKubeTestUtils.createMockTestPod(null, galasaServiceInstallName);
        mockTerminatedPods.add(podWithNoRunName);

        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);

        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        RunPodCleanup runPodCleanup = new RunPodCleanup(kubeEngineFacade, mockFrameworkRuns);

        // When...
        runPodCleanup.run();

        // Then...
        List<V1Pod> pods = kubeEngineFacade.getTestPods();
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).usingRecursiveComparison().isEqualTo(podWithNoRunName);
    }

    @Test
    public void testPodCleanupDoesNothingIfEtcdAndRasAreDown() throws Exception {
        // Given...
        String galasaServiceInstallName = "myGalasaService";
        String runName1 = "run1";
        String runName2 = "run2";

        // Create terminated pods
        List<V1Pod> mockTerminatedPods = new ArrayList<>();
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName, "failed"));
        mockTerminatedPods.add(mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName, "succeeded"));

        List<V1Pod> mockPods = new ArrayList<>(mockTerminatedPods);


        // Simulate a situation where the etcd and RAS pods are not ready
        boolean isReady = false;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        // Simulate a situation where the runs have been deleted from the DSS but the pods still exist,
        // so the pods should get deleted
        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        RunPodCleanup runPodCleanup = new RunPodCleanup(kubeEngineFacade, mockFrameworkRuns);

        // Check that the test pods exist before processing
        assertThat(kubeEngineFacade.getTestPods()).hasSize(2);

        // When...
        runPodCleanup.run();

        // Then...
        // The test pods should still exist
        assertThat(kubeEngineFacade.getTestPods()).hasSize(2);
    }
}
