/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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

public class RunInterruptMonitorTest {

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

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2));

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, currentTime));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).hasSize(3);
        assertThat(mockPods).contains(cancelledPod);

        // No events should have been added to the event queue yet
        assertThat(eventQueue).isEmpty();
    }

    @Test
    public void testPodsForMultipleInterruptedRunsAreOnlyDeletedWhenTimedOut() throws Exception {
        // Given...
        String runIdToMarkFinished1 = "run1-id";
        String runIdToMarkFinished2 = "run2-id";

        String runName1 = "run1";
        String runName2 = "run2";

        Instant timedOutInterruptedAt = Instant.EPOCH;
        Instant currentTime = Instant.now();
        String interruptReason = "cancelled";

        ITimeService timeService = new MockTimeService(currentTime);

        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod cancelledPod1 = mockKubeTestUtils.createMockTestPod(runName1);
        mockPods.add(cancelledPod1);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod2 = mockKubeTestUtils.createMockTestPod(runName2);
        mockPods.add(cancelledPod2);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runIdToMarkFinished1, runName1, TestRunLifecycleStatus.STARTED.toString(), interruptReason, timedOutInterruptedAt));
        mockRuns.add(createMockRun(runIdToMarkFinished2, runName2, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, currentTime));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).doesNotContain(cancelledPod1);

        // One event should have been added
        assertThat(eventQueue).hasSize(1);

        RunInterruptEvent interruptEvent1 = eventQueue.poll();
        assertThat(interruptEvent1.getRunName()).isEqualTo(runName1);

        List<RunRasAction> rasActions = interruptEvent1.getRasActions();
        assertThat(rasActions).hasSize(1);
        assertThat(rasActions.get(0).getRunId()).isEqualTo(runIdToMarkFinished1);

        // No runs should have been deleted, only one pod
        assertThat(mockFrameworkRuns.getAllRuns()).hasSize(2);
    }

    @Test
    public void testPodForAnInterruptedRunIsDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";
        
        Instant interruptedAt = Instant.EPOCH;
        ITimeService timeService = new MockTimeService(Instant.now());

        String interruptReason = "cancelled";

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2));

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).hasSize(2);
        assertThat(mockPods).doesNotContain(cancelledPod);

        // One event should have been added
        assertThat(eventQueue).hasSize(1);

        RunInterruptEvent interruptEvent = eventQueue.peek();
        assertThat(interruptEvent.getRunName()).isEqualTo(runName3);
        assertThat(interruptEvent.getInterruptReason()).isEqualTo(interruptReason);
        assertThat(interruptEvent.getInterruptedAt()).isEqualTo(interruptedAt);

        List<RunRasAction> rasActions = interruptEvent.getRasActions();
        assertThat(rasActions).hasSize(1);
        assertThat(rasActions.get(0).getRunId()).isEqualTo(runIdToMarkFinished);

        // No runs should have been deleted, only their pods
        assertThat(mockFrameworkRuns.getAllRuns()).hasSize(3);
    }

    @Test
    public void testPodForAnInterruptedRunWithNoInterruptedAtIsDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";

        ITimeService timeService = new MockTimeService(Instant.now());

        Instant interruptedAt = null;
        String interruptReason = "cancelled";

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2));

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).hasSize(2);
        assertThat(mockPods).doesNotContain(cancelledPod);

        // One event should have been added
        assertThat(eventQueue).hasSize(1);

        RunInterruptEvent interruptEvent = eventQueue.peek();
        assertThat(interruptEvent.getRunName()).isEqualTo(runName3);
        assertThat(interruptEvent.getInterruptReason()).isEqualTo(interruptReason);
        assertThat(interruptEvent.getInterruptedAt()).isEqualTo(interruptedAt);

        List<RunRasAction> rasActions = interruptEvent.getRasActions();
        assertThat(rasActions).hasSize(1);
        assertThat(rasActions.get(0).getRunId()).isEqualTo(runIdToMarkFinished);

        // No runs should have been deleted, only their pods
        assertThat(mockFrameworkRuns.getAllRuns()).hasSize(3);
    }

    @Test
    public void testPodsForMultipleInterruptedRunsAreDeletedOk() throws Exception {
        // Given...
        String runIdToMarkFinished1 = "run1-id";
        String runIdToMarkFinished2 = "run2-id";

        String runName1 = "run1";
        String runName2 = "run2";

        Instant interruptedAt = Instant.EPOCH;
        String interruptReason = "cancelled";

        ITimeService timeService = new MockTimeService(Instant.now());

        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod cancelledPod1 = mockKubeTestUtils.createMockTestPod(runName1);
        mockPods.add(cancelledPod1);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod2 = mockKubeTestUtils.createMockTestPod(runName2);
        mockPods.add(cancelledPod2);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runIdToMarkFinished1, runName1, TestRunLifecycleStatus.STARTED.toString(), interruptReason, interruptedAt));
        mockRuns.add(createMockRun(runIdToMarkFinished2, runName2, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).isEmpty();

        // Two events should have been added
        assertThat(eventQueue).hasSize(2);

        RunInterruptEvent interruptEvent1 = eventQueue.poll();
        assertThat(interruptEvent1.getRunName()).isEqualTo(runName1);

        List<RunRasAction> rasActions = interruptEvent1.getRasActions();
        assertThat(rasActions).hasSize(1);
        assertThat(rasActions.get(0).getRunId()).isEqualTo(runIdToMarkFinished1);

        RunInterruptEvent interruptEvent2 = eventQueue.poll();
        assertThat(interruptEvent2.getRunName()).isEqualTo(runName2);

        rasActions = interruptEvent2.getRasActions();
        assertThat(rasActions).hasSize(1);
        assertThat(rasActions.get(0).getRunId()).isEqualTo(runIdToMarkFinished2);

        // No runs should have been deleted, only their pods
        assertThat(mockFrameworkRuns.getAllRuns()).hasSize(2);
    }

    @Test
    public void testPodWithNoRunNameShouldNotBeDeleted() throws Exception {
        // Given...
        ITimeService timeService = new MockTimeService(Instant.now());

        // Simulate a situation where the current kubernetes namespace has a pod that may
        // not be a Galasa-related pod, so it doesn't have a "galasa-run" label with a run name.
        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod podWithNoRunName = mockKubeTestUtils.createMockTestPod(null);
        mockPods.add(podWithNoRunName);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // When...
        runPodInterrupt.run();

        // Then...
        List<V1Pod> pods = mockApiClient.getMockPods();
        assertThat(pods).hasSize(3);
        assertThat(pods.get(0)).usingRecursiveComparison().isEqualTo(podWithNoRunName);
    }

    @Test
    public void testInterruptedRunIsNotDeletedIfEtcdAndRasAreDown() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";

        String interruptReason = "cancelled";

        Instant interruptedAt = Instant.EPOCH;
        ITimeService timeService = new MockTimeService(Instant.now());

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2));

        String galasaServiceInstallName = "myGalasaService";

        // Simulate a situation where the etcd and RAS pods are not ready
        boolean isReady = false;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        MockISettings settings = new MockISettings();
        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kube, mockFrameworkRuns, eventQueue, settings, timeService);

        // Make sure that all 3 test pods exist before processing
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).hasSize(3);

        // When...
        runPodInterrupt.run();

        // Then...
        // Make sure that all 3 test pods still exist after processing
        assertThat(kube.getTestPods(MockISettings.ENGINE_LABEL)).hasSize(3);

        // No events should have been added yet
        assertThat(eventQueue).isEmpty();
    }
}
