/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import dev.galasa.framework.k8s.controller.mocks.MockSettings;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.RunRasAction;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunInterruptMonitorTest {

    private V1Pod createMockTestPod(String runName) {
        V1Pod mockPod = new V1Pod();

        V1ObjectMeta podMetadata = new V1ObjectMeta();
        podMetadata.putLabelsItem(TestPodScheduler.GALASA_RUN_POD_LABEL, runName);
        podMetadata.setName(runName);

        mockPod.setMetadata(podMetadata);
        return mockPod;
    }

    private MockRun createMockRun(String runIdToMarkFinished, String runName, String status, String interruptReason) {
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
        mockRun.setStatus(status);

        if (runIdToMarkFinished != null) {
            RunRasAction rasAction = new RunRasAction(runIdToMarkFinished, status, interruptReason);
            mockRun.setRasActions(List.of(rasAction));
        }
        return mockRun;
    }

    @Test
    public void testPodForAnInterruptedRunIsDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";

        String interruptReason = "cancelled";

        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(createMockTestPod(runName1));
        mockPods.add(createMockTestPod(runName2));

        V1Pod cancelledPod = createMockTestPod(runName3);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED.toString(), null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED.toString(), null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING.toString(), interruptReason));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, mockSettings);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kubeEngineFacade, mockFrameworkRuns, eventQueue);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(mockPods).hasSize(2);
        assertThat(mockPods).doesNotContain(cancelledPod);

        // One event should have been added
        assertThat(eventQueue).hasSize(1);

        RunInterruptEvent interruptEvent = eventQueue.peek();
        assertThat(interruptEvent.getRunName()).isEqualTo(runName3);
        assertThat(interruptEvent.getInterruptReason()).isEqualTo(interruptReason);

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

        String interruptReason = "cancelled";

        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod cancelledPod1 = createMockTestPod(runName1);
        mockPods.add(cancelledPod1);

        V1Pod cancelledPod2 = createMockTestPod(runName2);
        mockPods.add(cancelledPod2);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runIdToMarkFinished1, runName1, TestRunLifecycleStatus.STARTED.toString(), interruptReason));
        mockRuns.add(createMockRun(runIdToMarkFinished2, runName2, TestRunLifecycleStatus.RUNNING.toString(), interruptReason));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, mockSettings);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kubeEngineFacade, mockFrameworkRuns, eventQueue);

        // When...
        runPodInterrupt.run();

        // Then...
        assertThat(mockPods).isEmpty();

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
        // Simulate a situation where the current kubernetes namespace has a pod that may
        // not be a Galasa-related pod, so it doesn't have a "galasa-run" label with a run name.
        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod podWithNoRunName = createMockTestPod(null);
        mockPods.add(podWithNoRunName);

        List<IRun> mockRuns = new ArrayList<>();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockSettings mockSettings = new MockSettings(null, null, null);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(mockApiClient, mockSettings);
        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();

        RunInterruptMonitor runPodInterrupt = new RunInterruptMonitor(kubeEngineFacade, mockFrameworkRuns, eventQueue);

        // When...
        runPodInterrupt.run();

        // Then...
        List<V1Pod> pods = mockApiClient.getMockPods();
        assertThat(pods).hasSize(1);
        assertThat(pods.get(0)).usingRecursiveComparison().isEqualTo(podWithNoRunName);
    }
}
