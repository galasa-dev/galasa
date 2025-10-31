/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagement;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;

public class TestRunInactiveRunCleanup {

    private MockRun createMockRun(String runName, TestRunLifecycleStatus status, boolean isLocalRun) {
        MockRun run = new MockRun(
            "bundle",
            "class",
            runName,
            "teststream",
            "obr",
            "repo",
            "requestor",
            isLocalRun
        );
        run.setStatus(status.toString());
        return run;
    }

    @Test
    public void testCanCleanUpAllocatedRunThatHasTimedOut() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun timedOutRun = createMockRun("run1", TestRunLifecycleStatus.ALLOCATED, false);
        timedOutRun.setAllocatedTimeout(Instant.EPOCH);

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        runs.add(timedOutRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        // The test run should have been set an interrupt reason
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(timedOutRun.getInterruptReason()).isEqualTo(Result.HUNG);
    }

    @Test
    public void testCanCleanUpMultipleAllocatedRunThatHaveTimedOut() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun timedOutRun1 = createMockRun("run1", TestRunLifecycleStatus.ALLOCATED, false);
        MockRun timedOutRun2 = createMockRun("run2", TestRunLifecycleStatus.ALLOCATED, false);
        MockRun timedOutRun3 = createMockRun("run3", TestRunLifecycleStatus.ALLOCATED, false);

        timedOutRun1.setAllocatedTimeout(Instant.EPOCH);
        timedOutRun2.setAllocatedTimeout(Instant.EPOCH);
        timedOutRun3.setAllocatedTimeout(Instant.EPOCH);

        runs.add(timedOutRun1);
        runs.add(timedOutRun2);
        runs.add(timedOutRun3);

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(timedOutRun1.getInterruptReason()).isEqualTo(Result.HUNG);
        assertThat(timedOutRun2.getInterruptReason()).isEqualTo(Result.HUNG);
        assertThat(timedOutRun3.getInterruptReason()).isEqualTo(Result.HUNG);
    }

    @Test
    public void testDoesNotCleanUpRunsThatHaveNotTimedOut() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun timedOutRun = createMockRun("run1", TestRunLifecycleStatus.ALLOCATED, false);
        MockRun newRun = createMockRun("run2", TestRunLifecycleStatus.ALLOCATED, false);

        Instant currentTime = Instant.now();

        timedOutRun.setAllocatedTimeout(Instant.EPOCH);

        // This run has an allocated timeout set in the future
        newRun.setAllocatedTimeout(currentTime.plusSeconds(10));

        runs.add(timedOutRun);

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(timedOutRun.getInterruptReason()).isEqualTo(Result.HUNG);
        
        // The run that hasn't timed out should not have been interrupted
        assertThat(newRun.getInterruptReason()).isNull();
    }

    @Test
    public void testDoesNotCleanUpRunsThatAreNotInTheAllocatedState() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun run1 = createMockRun("run1", TestRunLifecycleStatus.BUILDING, false);
        MockRun run2 = createMockRun("run2", TestRunLifecycleStatus.STARTED, false);

        Instant currentTime = Instant.now();

        run1.setAllocatedTimeout(Instant.EPOCH);
        run2.setAllocatedTimeout(currentTime.plusSeconds(10));

        runs.add(run1);
        runs.add(run2);

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(currentTime);

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        // None of the runs should have been interrupted
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(run1.getInterruptReason()).isNull();
        assertThat(run2.getInterruptReason()).isNull();
    }

    @Test
    public void testCanCleanUpQueuedLocalRunThatHasTimedOut() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun timedOutRun = createMockRun("run1", TestRunLifecycleStatus.QUEUED, true);
        timedOutRun.setQueued(Instant.EPOCH);

        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        runs.add(timedOutRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        // The test run should have been set an interrupt reason
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(timedOutRun.getInterruptReason()).isEqualTo(Result.HUNG);
    }

    @Test
    public void testQueuedLocalRunTimeoutCanBeControlledByCpsProperty() throws Exception {
        // Given...
        List<IRun> runs = new ArrayList<>();
        MockRun timedOutRun = createMockRun("run1", TestRunLifecycleStatus.QUEUED, true);
        Instant queuedTime = Instant.now();
        timedOutRun.setQueued(queuedTime);

        // Make queued local test runs time out after a second of being queued
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        mockCps.setProperty("resource.management.local.queued.timeout", "1");

        runs.add(timedOutRun);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);

        // Set the current time to a few seconds in the future
        MockResourceManagement mockResourceManagement = new MockResourceManagement();
        MockTimeService mockTimeService = new MockTimeService(queuedTime.plusSeconds(10));

        RunInactiveRunCleanup runCleanup = new RunInactiveRunCleanup(mockFrameworkRuns, mockResourceManagement, mockTimeService, mockCps);

        // When...
        runCleanup.run();

        // Then...
        // The test run should have been set an interrupt reason
        assertThat(mockResourceManagement.isSuccessful).isTrue();
        assertThat(timedOutRun.getInterruptReason()).isEqualTo(Result.HUNG);
    }
}
