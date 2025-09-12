/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.interruptedruns;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.RunRasActionProcessor;
import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.k8s.controller.api.IKubernetesApiClient;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesPodTestUtils;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockRunResult;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;
import io.kubernetes.client.openapi.models.V1Pod;

public class RunInterruptedEventProcessorTest {

    private MockKubernetesPodTestUtils mockKubeTestUtils = new MockKubernetesPodTestUtils();

    private MockRun createMockRun(
        String runIdToMarkFinished,
        String runName,
        TestRunLifecycleStatus status,
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
        mockRun.setStatus(status.toString());

        if (runIdToMarkFinished != null) {
            RunRasAction rasAction = new RunRasAction(runIdToMarkFinished, status.toString(), interruptReason);
            mockRun.setRasActions(List.of(rasAction));
        }
        return mockRun;
    }

    private MockRunResult createMockRunResult(String rasRunId, TestRunLifecycleStatus status) {
        Path artifactRoot = null;
        String log = null;

        TestStructure testStructure = new TestStructure();
        testStructure.setStatus(status.toString());

        MockRunResult mockRunResult = new MockRunResult(rasRunId, testStructure, artifactRoot, log);
        return mockRunResult;
    }

    @Test
    public void testEventProcessorMarksRunFinishedOk() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        TestRunLifecycleStatus status = TestRunLifecycleStatus.RUNNING;
        String interruptReason = Result.CANCELLED;
        Instant interruptedAt = Instant.now();

        RunRasAction mockRasAction = new RunRasAction(runId, TestRunLifecycleStatus.FINISHED.toString(), interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockRun mockRun = createMockRun(runId, runName, status, interruptReason, interruptedAt );
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        List<V1Pod> etcdAndRasPods = mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady);
        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(etcdAndRasPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        List<RunInterruptEvent> events = new ArrayList<RunInterruptEvent>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, interruptReason, interruptedAt, status);
        events.add(interruptEvent);

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor(mockFrameworkRuns, rasActionProcessor, facade, mockRas, new PodDeleter(facade));

        // When...
        processor.processEvents(events);

        // Then...
        assertThat(mockRun.getStatus()).isEqualTo(TestRunLifecycleStatus.FINISHED.toString());
        assertThat(mockRun.getResult()).isEqualTo(interruptReason);

        TestStructure runTestStructure = mockRunResult.getTestStructure();
        assertThat(runTestStructure.getStatus()).isEqualTo(TestRunLifecycleStatus.FINISHED.toString());
        assertThat(runTestStructure.getResult()).isEqualTo(interruptReason);
    }

    @Test
    public void testEventProcessorMarksRunRequeuedOk() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        TestRunLifecycleStatus status = TestRunLifecycleStatus.RUNNING;
        String interruptReason = Result.REQUEUED;
        Instant interruptedAt = Instant.now();

        RunRasAction mockRasAction = new RunRasAction(runId, TestRunLifecycleStatus.FINISHED.toString(), interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockRun mockRun = createMockRun(runId, runName, status, interruptReason, interruptedAt);
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        String galasaServiceInstallName = "myGalasaService";
        boolean isReady = true;
        List<V1Pod> etcdAndRasPods = mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady);
        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(etcdAndRasPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        List<RunInterruptEvent> events = new ArrayList<>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, interruptReason, interruptedAt, status);
        events.add(interruptEvent);

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor( mockFrameworkRuns, rasActionProcessor, facade, mockRas, new PodDeleter(facade));

        // When...
        processor.processEvents(events);

        // Then...
        assertThat(mockRun.getStatus()).isEqualTo(TestRunLifecycleStatus.QUEUED.toString());

        TestStructure runTestStructure = mockRunResult.getTestStructure();
        assertThat(runTestStructure.getStatus()).isEqualTo(TestRunLifecycleStatus.FINISHED.toString());
        assertThat(runTestStructure.getResult()).isEqualTo(interruptReason);
    }

    @Test
    public void testEventProcessorDoesNothingIfRasOrEtcdAreNotReady() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        TestRunLifecycleStatus status = TestRunLifecycleStatus.RUNNING;
        String interruptReason = Result.CANCELLED;
        Instant interruptedAt = Instant.now();

        RunRasAction mockRasAction = new RunRasAction(runId, TestRunLifecycleStatus.FINISHED.toString(), interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockRun mockRun = createMockRun(runId, runName, status, interruptReason, interruptedAt);
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        String galasaServiceInstallName = "myGalasaService";

        // Simulate a situation where the etcd and RAS are not ready
        boolean isReady = false;
        List<V1Pod> etcdAndRasPods = mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady);
        IKubernetesApiClient mockApiClient = new MockKubernetesApiClient(etcdAndRasPods);
        KubernetesEngineFacade facade = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        List<RunInterruptEvent> events = new ArrayList<>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, interruptReason, interruptedAt, status);
        events.add(interruptEvent);

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor(mockFrameworkRuns, rasActionProcessor, facade, mockRas, new PodDeleter(facade));

        // Check that we have one element in the event queue before processing
        assertThat(events.get(0)).usingRecursiveComparison().isEqualTo(interruptEvent);

        // When...
        processor.processEvents(events);

        // Then...
        // The event should not have been processed yet
        assertThat(events.get(0)).usingRecursiveComparison().isEqualTo(interruptEvent);
    }


    
    @Test
    public void testPodForAnInterruptedRunIsDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";
        
        Instant interruptedAt = Instant.EPOCH;

        String interruptReason = "cancelled";
        String galasaServiceInstallName = "myGalasaService";

        // Create 2 pods we won't touch
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName));

        // Create the etcd and couchb pods that makes 4 pods total
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        // Create a pod we can delete to match the run we are cancelling. That makes 5 pods.
        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3, galasaServiceInstallName);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED, null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED, null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING, interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);


        List<RunInterruptEvent> events = new ArrayList<>();

        // Pretend the pod is stuck in PROVSTART state
        IRunResult mockRunResult = createMockRunResult(runIdToMarkFinished, TestRunLifecycleStatus.PROVSTART);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runIdToMarkFinished, mockFileSystem);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);
        
        events.add( new RunInterruptEvent(null, runName3, interruptReason, interruptedAt, TestRunLifecycleStatus.PROVSTART ));

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor(mockFrameworkRuns, rasActionProcessor, kube, mockRas, new PodDeleter(kube));

        // When...
        processor.processEvents(events);

        // Then...
        assertThat(kube.getTestPods()).as("One of the 3 test engine pods should have been deleted").hasSize(2);
        assertThat(mockPods).doesNotContain(cancelledPod);

    }

    @Test
    public void testPodForAnInterruptedRunWithNoInterruptedAtIsDeletedOk() throws Exception {
        // Given...
        String runName1 = "run1";
        String runName2 = "run2";
        String runName3 = "run3";
        String runIdToMarkFinished = "run3-id";

        Instant interruptedAt = null;
        String interruptReason = "cancelled";
        String galasaServiceInstallName = "myGalasaService";

        // 2 pods we will leave alone.
        List<V1Pod> mockPods = new ArrayList<>();
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName));
        mockPods.add(mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName));

        // a couchdb and etcd pod
        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        // A pod we intend to cancel
        V1Pod cancelledPod = mockKubeTestUtils.createMockTestPod(runName3, galasaServiceInstallName);
        mockPods.add(cancelledPod);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(null, runName1, TestRunLifecycleStatus.FINISHED, null, null));
        mockRuns.add(createMockRun(null, runName2, TestRunLifecycleStatus.FINISHED, null, null));
        mockRuns.add(createMockRun(runIdToMarkFinished, runName3, TestRunLifecycleStatus.RUNNING, interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        List<RunInterruptEvent> events = new ArrayList<>();

        // Pretend the pod is stuck in PROVSTART state
        IRunResult mockRunResult = createMockRunResult(runIdToMarkFinished, TestRunLifecycleStatus.PROVSTART);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runIdToMarkFinished, mockFileSystem);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);
        
        events.add( new RunInterruptEvent(null, runName3, interruptReason, null, TestRunLifecycleStatus.PROVSTART ));

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor(mockFrameworkRuns, rasActionProcessor, kube, mockRas, new PodDeleter(kube));

        // When...
        processor.processEvents(events);

        // Then...
        assertThat(kube.getTestPods()).hasSize(2);
        assertThat(mockPods).doesNotContain(cancelledPod);

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
        String galasaServiceInstallName = "myGalasaService";

        List<V1Pod> mockPods = new ArrayList<>();
        V1Pod cancelledPod1 = mockKubeTestUtils.createMockTestPod(runName1, galasaServiceInstallName);
        mockPods.add(cancelledPod1);

        boolean isReady = true;
        mockPods.addAll(mockKubeTestUtils.createEtcdAndRasPods(galasaServiceInstallName, isReady));

        V1Pod cancelledPod2 = mockKubeTestUtils.createMockTestPod(runName2, galasaServiceInstallName);
        mockPods.add(cancelledPod2);

        // Create runs associated with the pods
        List<IRun> mockRuns = new ArrayList<>();
        mockRuns.add(createMockRun(runIdToMarkFinished1, runName1, TestRunLifecycleStatus.STARTED, interruptReason, interruptedAt));
        mockRuns.add(createMockRun(runIdToMarkFinished2, runName2, TestRunLifecycleStatus.RUNNING, interruptReason, interruptedAt));

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient(mockPods);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        KubernetesEngineFacade kube = new KubernetesEngineFacade(mockApiClient, "myNamespace", galasaServiceInstallName);

        List<RunInterruptEvent> events = new ArrayList<>();

        // Pretend the pod is stuck in PROVSTART state
        IRunResult mockRunResult = createMockRunResult(runIdToMarkFinished1, TestRunLifecycleStatus.PROVSTART);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runIdToMarkFinished2, mockFileSystem);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);
        
        events.add( new RunInterruptEvent(null, runName1, interruptReason, null, TestRunLifecycleStatus.PROVSTART ));
        events.add( new RunInterruptEvent(null, runName2, interruptReason, null, TestRunLifecycleStatus.PROVSTART ));

        RunInterruptEventProcessor processor = new RunInterruptEventProcessor(mockFrameworkRuns, rasActionProcessor, kube, mockRas, new PodDeleter(kube));

        // When...
        processor.processEvents(events);

        // Then...
        assertThat(kube.getTestPods()).isEmpty();
    }
}
