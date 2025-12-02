/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.scheduling;

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
import dev.galasa.framework.spi.IRun;

public class PrioritySchedulingServiceTest {

    @Test
    public void testOlderRunsHaveHigherPriorityOverNewRuns() throws Exception {
        // Given...
        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun oldRun1 = new MockRun(null, null, "run1", null, null, null, null, false);
        oldRun1.setQueued(Instant.EPOCH);
        oldRun1.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun oldRun2 = new MockRun(null, null, "run2", null, null, null, null, false);
        oldRun2.setQueued(Instant.EPOCH.plusSeconds(10));
        oldRun2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun newRun = new MockRun(null, null, "run3", null, null, null, null, false);
        newRun.setQueued(now);
        newRun.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(newRun);
        runs.add(oldRun1);
        runs.add(oldRun2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockTimeService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(3);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun1);
        assertThat(runsGotBack.get(1)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(2)).isEqualTo(newRun);
    }

    @Test
    public void testRunsQueuedAtTheSameTimeMaintainExistingOrdering() throws Exception {
        // Given...
        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun oldRun1 = new MockRun(null, null, "run1", null, null, null, null, false);
        oldRun1.setQueued(Instant.EPOCH);
        oldRun1.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun oldRun2 = new MockRun(null, null, "run2", null, null, null, null, false);
        oldRun2.setQueued(Instant.EPOCH);
        oldRun2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun newRun = new MockRun(null, null, "run3", null, null, null, null, false);
        newRun.setQueued(now);
        newRun.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(newRun);
        runs.add(oldRun2);
        runs.add(oldRun1);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockTimeService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(3);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(1)).isEqualTo(oldRun1);
        assertThat(runsGotBack.get(2)).isEqualTo(newRun);
    }

    @Test
    public void testActiveRunsAreIgnoredFromScheduling() throws Exception {
        // Given...
        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun oldRun1 = new MockRun(null, null, "run1", null, null, null, null, false);
        oldRun1.setQueued(Instant.EPOCH);
        oldRun1.setStatus(TestRunLifecycleStatus.RUNNING.toString());

        MockRun oldRun2 = new MockRun(null, null, "run2", null, null, null, null, false);
        oldRun2.setQueued(Instant.EPOCH);
        oldRun2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun newRun = new MockRun(null, null, "run3", null, null, null, null, false);
        newRun.setQueued(now);
        newRun.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(newRun);
        runs.add(oldRun2);
        runs.add(oldRun1);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockTimeService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(2);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(1)).isEqualTo(newRun);
    }
}
