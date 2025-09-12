/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockISettings;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesPodTestUtils;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.utils.ITimeService;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunInterruptEventCollectorTest {

    private MockKubernetesPodTestUtils mockKubeTestUtils = new MockKubernetesPodTestUtils();

    private MockRun createMockRun(
        String runIdToMarkFinished,
        String runName,
        String status,
        String interruptReason,
        Instant interruptedAt
    ) {
        // We only care about the run's name, status, and interrupt reason
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

        mockRun.setInterruptReason(interruptReason);
        mockRun.setInterruptedAt(interruptedAt);
        mockRun.setStatus(status);

        if (runIdToMarkFinished != null) {
            RunRasAction rasAction = new RunRasAction(runIdToMarkFinished, status, interruptReason);
            mockRun.setRasActions(List.of(rasAction));
        }
        return mockRun;
    }

    @Test
    public void testPodForAnInterruptedRunIsNotDeletedBeforeInterruptTimeout() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";
        
        Instant currentTime = Instant.now();
        ITimeService timeService = new MockTimeService(currentTime);

        String interruptReason = "cancelled";
        String galasaServiceInstallName = "myGalasaService";

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName));

        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3, galasaServiceInstallName);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, currentTime));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
  

        MockISettings settings = new MockISettings();
        RunInterruptEventCollector runPodInterrupt = new RunInterruptEventCollector(kube, mockFrameworkRuns, settings, timeService);

        // When...
        List<RunInterruptEvent> events = runPodInterrupt.collectInterruptRunEvents();

        // Then...
        assertThat(kube.getTestPods()).hasSize(3);
        assertThat(mockPods).contains(cancelledPod);

        assertThat(events).as("should not have collected any pods to delete as they haven't timed out yet.").hasSize(0);
    }

    @Test
    public void testPodWithNoRunNameShouldNotBeDeleted() throws Exception {
        // Given...
        ITimeService timeService = new MockTimeService(Instant.now());

        // Simulate a situation where the current kubernetes namespace has a pod that may
        // not be a Galasa-related pod, so it doesn't have a "galasa-run" label with a run name.
        String galasaServiceInstallName = "myGalasaService";
        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod podWithNoRunName = mockKubeTestUtils.createMockTestPod(null, galasaServiceInstallName);
        mockPods.add(podWithNoRunName);

        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        MockISettings settings = new MockISettings();
        RunInterruptEventCollector runPodInterrupt = new RunInterruptEventCollector(kube, mockFrameworkRuns, settings, timeService);

        // When...
        List<RunInterruptEvent> events = runPodInterrupt.collectInterruptRunEvents();

        // Then...
        List<V1Pod> pods = mockApiClient.getMockPods();
        assertThat(pods).hasSize(3);
        assertThat(pods.get(0)).usingRecursiveComparison().isEqualTo(podWithNoRunName);

        assertThat(events).as("No runs should be identified for deletion even if they have no runname.").isEmpty();
    }

    @Test
    public void testInterruptedRunIsNotDeletedIfEtcdAndRasAreDown() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";

        String galasaServiceInstallName = "myGalasaService";
        String interruptReason = "cancelled";

        Instant interruptedAt = Instant.EPOCH;
        ITimeService timeService = new MockTimeService(Instant.now());

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName));


        // Simulate a situation where the etcd and RAS pods are not ready
        boolean isReady = false;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3, galasaServiceInstallName);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        

        MockISettings settings = new MockISettings();
        RunInterruptEventCollector runPodInterrupt = new RunInterruptEventCollector(kube, mockFrameworkRuns, settings, timeService);

        // Make sure that all 3 test pods exist before processing
        assertThat(kube.getTestPods()).hasSize(3);

        // When...
        List<RunInterruptEvent> events = runPodInterrupt.collectInterruptRunEvents();

        // Then...
        // Make sure that all 3 test pods still exist after processing
        assertThat(kube.getTestPods()).hasSize(3);

        // No events should have been added yet
        assertThat(events).isEmpty();
    }



    @Test
    public void testQueuedRunsGetCollectedForDisposal() throws Exception {
        // Given...
        String runNameBeingCancelled = "run3";
        String runIdToMarkFinished = "run3-id";

        String interruptReason = "cancelled";

        // The queued run has only just been interrupted.
        Instant interruptedAt = Instant.EPOCH;
        ITimeService timeService = new MockTimeService(Instant.EPOCH);

        List<V1Pod> mockPods = new ArrayList<>();

        String galasaServiceInstallName = "myGalasaService";

        // etcd and RAS pods are ready
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        // The run we are cancelling has no pod associated yet.

        // Create runs being scanned
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runIdToMarkFinished, runNameBeingCancelled, TestRunLifecycleStatus.QUEUED.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        MockISettings settings = new MockISettings();
        RunInterruptEventCollector runPodInterrupt = new RunInterruptEventCollector(kube, mockFrameworkRuns, settings, timeService);

        // When...
        List<RunInterruptEvent> events = runPodInterrupt.collectInterruptRunEvents();

        // Then...

        // The queued run should have been added for cleanup, even though it's not been cancelled a long time ago.
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getRunName()).isEqualTo(runNameBeingCancelled);
    }
}
