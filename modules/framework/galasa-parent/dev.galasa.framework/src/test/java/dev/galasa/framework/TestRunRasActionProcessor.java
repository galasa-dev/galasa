/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.mocks.MockRunResult;
import dev.galasa.framework.spi.IRunRasActionProcessor;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.RunRasAction;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestRunRasActionProcessor {

    private MockRunResult createMockRunResult(String rasRunId, String status) {
        Path artifactRoot = null;
        String log = null;

        TestStructure testStructure = new TestStructure();
        testStructure.setStatus(status);

        MockRunResult mockRunResult = new MockRunResult(rasRunId, testStructure, artifactRoot, log);
        return mockRunResult;
    }

    @Test
    public void testProcessRasActionsWithNoRunsDoesNotThrowError() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        String newStatus = TestRunLifecycleStatus.FINISHED.toString();
        String interruptReason = Result.REQUEUED;

        RunRasAction mockRasAction = new RunRasAction(runId, newStatus, interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        List<IRunResult> runResults = List.of();
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        // When...
        rasActionProcessor.processRasActions(runName, rasActions);

        // Then...
        // No error should have been thrown
        assertThat(mockRas.getTestStructureHistory()).isEmpty();
    }

    @Test
    public void testProcessOneRasActionUpdatesRasOk() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        String status = TestRunLifecycleStatus.RUNNING.toString();
        String newStatus = TestRunLifecycleStatus.FINISHED.toString();
        String interruptReason = Result.REQUEUED;

        RunRasAction mockRasAction = new RunRasAction(runId, newStatus, interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        // When...
        rasActionProcessor.processRasActions(runName, rasActions);

        // Then...
        List<TestStructure> testStructureHistory = mockRas.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(1);

        TestStructure latestTestStructure = testStructureHistory.get(0);
        assertThat(latestTestStructure.getStatus()).isEqualTo(newStatus);
        assertThat(latestTestStructure.getResult()).isEqualTo(interruptReason);
    }

    @Test
    public void testProcessRasActionWithSameStatusDoesNotUpdateRas() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        String status = TestRunLifecycleStatus.FINISHED.toString();
        String newStatus = TestRunLifecycleStatus.FINISHED.toString();
        String interruptReason = Result.REQUEUED;

        RunRasAction mockRasAction = new RunRasAction(runId, newStatus, interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        // When...
        rasActionProcessor.processRasActions(runName, rasActions);

        // Then...
        // No updates to the RAS should have been made
        List<TestStructure> testStructureHistory = mockRas.getTestStructureHistory();
        assertThat(testStructureHistory).isEmpty();
    }

    @Test
    public void testProcessMultipleRasActionsUpdatesRasOk() throws Exception {
        // Given...
        String runId1 = "this-is-a-run-id";
        String runName = "RUN1";
        String status1 = TestRunLifecycleStatus.RUNNING.toString();
        String newStatus = TestRunLifecycleStatus.FINISHED.toString();
        String interruptReason1 = Result.REQUEUED;

        String runId2 = "this-is-another-run-id";
        String status2 = TestRunLifecycleStatus.GENERATING.toString();
        String interruptReason2 = Result.CANCELLED;

        RunRasAction mockRasAction1 = new RunRasAction(runId1, newStatus, interruptReason1);
        RunRasAction mockRasAction2 = new RunRasAction(runId2, newStatus, interruptReason2);
        List<RunRasAction> rasActions = List.of(mockRasAction1, mockRasAction2);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId1, mockFileSystem);

        List<IRunResult> runResults = List.of(
            createMockRunResult(runId1, status1),
            createMockRunResult(runId2, status2)
        );

        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        // When...
        rasActionProcessor.processRasActions(runName, rasActions);

        // Then...
        List<TestStructure> testStructureHistory = mockRas.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(2);

        TestStructure testStructure1 = testStructureHistory.get(0);
        assertThat(testStructure1.getStatus()).isEqualTo(newStatus);
        assertThat(testStructure1.getResult()).isEqualTo(interruptReason1);

        TestStructure testStructure2 = testStructureHistory.get(1);
        assertThat(testStructure2.getStatus()).isEqualTo(newStatus);
        assertThat(testStructure2.getResult()).isEqualTo(interruptReason2);
    }

    @Test
    public void testProcessRasActionsWithNullRunIdIgnoresRun() throws Exception {
        // Given...
        String runId1 = "this-is-a-run-id";
        String runName = "RUN1";
        String status1 = TestRunLifecycleStatus.RUNNING.toString();
        String newStatus = TestRunLifecycleStatus.FINISHED.toString();
        String interruptReason1 = Result.REQUEUED;

        // Set a null run ID - this run should be ignored
        String runId2 = null;
        String status2 = TestRunLifecycleStatus.GENERATING.toString();
        String interruptReason2 = Result.CANCELLED;

        RunRasAction mockRasAction1 = new RunRasAction(runId1, newStatus, interruptReason1);
        RunRasAction mockRasAction2 = new RunRasAction(runId2, newStatus, interruptReason2);
        List<RunRasAction> rasActions = List.of(mockRasAction1, mockRasAction2);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId1, mockFileSystem);

        List<IRunResult> runResults = List.of(
            createMockRunResult(runId1, status1),
            createMockRunResult(runId2, status2)
        );

        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        // When...
        rasActionProcessor.processRasActions(runName, rasActions);

        // Then...
        List<TestStructure> testStructureHistory = mockRas.getTestStructureHistory();
        assertThat(testStructureHistory).hasSize(1);

        TestStructure testStructure1 = testStructureHistory.get(0);
        assertThat(testStructure1.getStatus()).isEqualTo(newStatus);
        assertThat(testStructure1.getResult()).isEqualTo(interruptReason1);
    }
}
