/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import dev.galasa.framework.RunRasActionProcessor;
import dev.galasa.framework.TestRunLifecycleStatus;
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

public class InterruptedRunEventProcessorTest {

    private MockRun createMockRun(String runName, String status, String interruptReason) {
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
        return mockRun;
    }

    private MockRunResult createMockRunResult(String rasRunId, String status) {
        Path artifactRoot = null;
        String log = null;

        TestStructure testStructure = new TestStructure();
        testStructure.setStatus(status);

        MockRunResult mockRunResult = new MockRunResult(rasRunId, testStructure, artifactRoot, log);
        return mockRunResult;
    }

    @Test
    public void testEventProcessorMarksRunFinishedOk() throws Exception {
        // Given...
        String runId = "this-is-a-run-id";
        String runName = "RUN1";
        String status = "running";
        String interruptReason = Result.CANCELLED;

        RunRasAction mockRasAction = new RunRasAction(runId, TestRunLifecycleStatus.FINISHED.toString(), interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);


        MockRun mockRun = createMockRun(runName, status, interruptReason);
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, interruptReason);
        eventQueue.add(interruptEvent);

        InterruptedRunEventProcessor processor = new InterruptedRunEventProcessor(eventQueue, mockFrameworkRuns, rasActionProcessor);

        // When...
        processor.run();

        // Then...
        assertThat(eventQueue).isEmpty();
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
        String status = "running";
        String interruptReason = Result.REQUEUED;

        RunRasAction mockRasAction = new RunRasAction(runId, TestRunLifecycleStatus.FINISHED.toString(), interruptReason);
        List<RunRasAction> rasActions = List.of(mockRasAction);

        MockRun mockRun = createMockRun(runName, status, interruptReason);
        List<IRun> mockRuns = List.of(mockRun);
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(mockRuns);

        MockFileSystem mockFileSystem = new MockFileSystem();
        MockIResultArchiveStore mockRas = new MockIResultArchiveStore(runId, mockFileSystem);

        IRunResult mockRunResult = createMockRunResult(runId, status);
        List<IRunResult> runResults = List.of(mockRunResult);
        MockResultArchiveStoreDirectoryService mockDirectoryService = new MockResultArchiveStoreDirectoryService(runResults);
        mockRas.addDirectoryService(mockDirectoryService);

        IRunRasActionProcessor rasActionProcessor = new RunRasActionProcessor(mockRas);

        Queue<RunInterruptEvent> eventQueue = new LinkedBlockingQueue<>();
        RunInterruptEvent interruptEvent = new RunInterruptEvent(rasActions, runName, interruptReason);
        eventQueue.add(interruptEvent);

        InterruptedRunEventProcessor processor = new InterruptedRunEventProcessor(eventQueue, mockFrameworkRuns, rasActionProcessor);

        // When...
        processor.run();

        // Then...
        assertThat(eventQueue).isEmpty();
        assertThat(mockRun.getStatus()).isEqualTo(TestRunLifecycleStatus.QUEUED.toString());

        TestStructure runTestStructure = mockRunResult.getTestStructure();
        assertThat(runTestStructure.getStatus()).isEqualTo(TestRunLifecycleStatus.FINISHED.toString());
        assertThat(runTestStructure.getResult()).isEqualTo(interruptReason);
    }
}
