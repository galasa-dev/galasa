/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller.scheduling;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    public void testRunsIncreaseInPriorityUsingGrowthRateCPSProperty() throws Exception {
        // Given...
        Instant now = Instant.now();
        long priorityGrowthRatePerMin = 10;

        List<IRun> runs = new ArrayList<>();
        MockRun run1 = new MockRun(null, null, "run1", null, null, null, null, false);

        // Set the queued time to be 2 minutes before now
        run1.setQueued(now.minus(2, ChronoUnit.MINUTES));
        run1.setStatus(TestRunLifecycleStatus.QUEUED.toString());
        
        MockRun run2 = new MockRun(null, null, "run2", null, null, null, null, false);

        // Set the queued time to be 4 minutes before now
        run2.setQueued(now.minus(7, ChronoUnit.MINUTES));
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(run1);
        runs.add(run2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        mockCps.setProperty("runs.priority.growth.rate.per.min", Long.toString(priorityGrowthRatePerMin));

        MockTimeService mockTimeService = new MockTimeService(now);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockTimeService);

        // When...
        double run1Priority = schedulingService.getQueuedRunPriority(run1);
        double run2Priority = schedulingService.getQueuedRunPriority(run2);

        // Then...
        assertThat(run1Priority).isEqualTo(2 * priorityGrowthRatePerMin);
        assertThat(run2Priority).isEqualTo(7 * priorityGrowthRatePerMin);
    }

    @Test
    public void testRunsIncreaseInPriorityUsingDefaultGrowthRateIfCPSPropertyIsBlank() throws Exception {
        // Given...
        Instant now = Instant.now();

        List<IRun> runs = new ArrayList<>();
        MockRun run1 = new MockRun(null, null, "run1", null, null, null, null, false);

        // Set the queued time to be 2 minutes before now
        run1.setQueued(now.minus(2, ChronoUnit.MINUTES));
        run1.setStatus(TestRunLifecycleStatus.QUEUED.toString());
        
        MockRun run2 = new MockRun(null, null, "run2", null, null, null, null, false);

        // Set the queued time to be 4 minutes before now
        run2.setQueued(now.minus(7, ChronoUnit.MINUTES));
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(run1);
        runs.add(run2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        // Set a blank value for the growth rate CPS property
        mockCps.setProperty("runs.priority.growth.rate.per.min", "     ");

        MockTimeService mockTimeService = new MockTimeService(now);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockTimeService);

        // When...
        double run1Priority = schedulingService.getQueuedRunPriority(run1);
        double run2Priority = schedulingService.getQueuedRunPriority(run2);

        // Then...
        assertThat(run1Priority).isEqualTo(2 * PrioritySchedulingService.DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN);
        assertThat(run2Priority).isEqualTo(7 * PrioritySchedulingService.DEFAULT_TEST_RUN_PRIORITY_POINTS_GROWTH_RATE_PER_MIN);
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

    @Test
    public void testLocalRunsAreIgnoredFromScheduling() throws Exception {
        // Given...
        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();

        // Mark this run as a local run, so this should be ignored
        MockRun oldRun1 = new MockRun(null, null, "run1", null, null, null, null, true);
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
        assertThat(runsGotBack).hasSize(2);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(1)).isEqualTo(newRun);
    }
}
