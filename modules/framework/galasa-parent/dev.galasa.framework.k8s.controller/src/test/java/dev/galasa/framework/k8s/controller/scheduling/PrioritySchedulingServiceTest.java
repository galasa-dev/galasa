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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTagsService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.mocks.MockUser;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.tags.Tag;

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
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(3);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun1);
        assertThat(runsGotBack.get(1)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(2)).isEqualTo(newRun);
    }

    @Test
    public void testRunsWithinTheSameMinuteAreOrderedCorrectly() throws Exception {
        // Given...
        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun oldRun1 = new MockRun(null, null, "run1", null, null, null, null, false);
        oldRun1.setQueued(now.minusSeconds(30));
        oldRun1.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun oldRun2 = new MockRun(null, null, "run2", null, null, null, null, false);
        oldRun2.setQueued(now.minusSeconds(29));
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
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

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

        // Set the queued time to be 7 minutes before now
        run2.setQueued(now.minus(7, ChronoUnit.MINUTES));
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(run1);
        runs.add(run2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        mockCps.setProperty("runs.priority.growth.rate.per.min", Long.toString(priorityGrowthRatePerMin));

        MockTimeService mockTimeService = new MockTimeService(now);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        double run1Priority = schedulingService.getQueuedRunTotalPriorityPoints(run1, new HashMap<>());
        double run2Priority = schedulingService.getQueuedRunTotalPriorityPoints(run2, new HashMap<>());

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

        // Set the queued time to be 7 minutes before now
        run2.setQueued(now.minus(7, ChronoUnit.MINUTES));
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(run1);
        runs.add(run2);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();

        // Set a blank value for the growth rate CPS property
        mockCps.setProperty("runs.priority.growth.rate.per.min", "     ");

        MockTimeService mockTimeService = new MockTimeService(now);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        double run1Priority = schedulingService.getQueuedRunTotalPriorityPoints(run1, new HashMap<>());
        double run2Priority = schedulingService.getQueuedRunTotalPriorityPoints(run2, new HashMap<>());

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
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

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
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

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

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(2);
        assertThat(runsGotBack.get(0)).isEqualTo(oldRun2);
        assertThat(runsGotBack.get(1)).isEqualTo(newRun);
    }

    @Test
    public void testUserPriorityIsAddedToRunPriorityCalculation() throws Exception {
        // Given...
        MockUser user1 = new MockUser();
        user1.setLoginId("user1");
        user1.setPriority(100);

        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun run1 = new MockRun(null, null, "run1", null, null, null, null, false);
        run1.setQueued(now);
        run1.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun run2 = new MockRun(null, null, "run2", null, null, null, null, false);
        run2.setQueued(now);
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        // The only difference here is that run3 is being run by user1 with priority 100, so this one should get scheduled first
        MockRun run3 = new MockRun(null, null, "run3", null, null, null, user1.getLoginId(), false);
        run3.setQueued(now);
        run3.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        runs.add(run1);
        runs.add(run2);
        runs.add(run3);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(user1, BuiltInAction.getActions());
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();

        // Then...
        assertThat(runsGotBack).hasSize(3);
        assertThat(runsGotBack.get(0)).isEqualTo(run3);
        assertThat(runsGotBack.get(1)).isEqualTo(run1);
        assertThat(runsGotBack.get(2)).isEqualTo(run2);
    }

    @Test
    public void testTagPriorityAffectsRunPriorityOrder() throws Exception {
        // Given...
        Map<String, Tag> tags = new HashMap<>();
        Tag tag1 = new Tag("high-priority-tag");
        tag1.setPriority(200);
        tags.put(tag1.getName(), tag1);

        Tag tag2 = new Tag("another-tag");
        tag2.setPriority(20);
        tags.put(tag2.getName(), tag2);

        Map<String, Tag> tagsMap = new HashMap<>();
        tagsMap.put(tag1.getName(), tag1);
        tagsMap.put(tag2.getName(), tag2);

        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun run1 = new MockRun(null, null, "run1", null, null, null, null, false);
        run1.setQueued(now);
        run1.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        MockRun run2 = new MockRun(null, null, "run2", null, null, null, null, false);
        run2.setQueued(now);
        run2.setStatus(TestRunLifecycleStatus.QUEUED.toString());

        // The only difference here is that run3 has tags with priority values set, so this one should get scheduled first
        MockRun run3 = new MockRun(null, null, "run3", null, null, null, null, false);
        run3.setQueued(now);
        run3.setStatus(TestRunLifecycleStatus.QUEUED.toString());
        run3.setTags(Set.of("high-priority-tag", "another-tag"));

        runs.add(run1);
        runs.add(run2);
        runs.add(run3);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService(tags);

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        List<IRun> runsGotBack = schedulingService.getPrioritisedTestRunsToSchedule();
        double run3Priority = schedulingService.getQueuedRunTotalPriorityPoints(run3, tagsMap);

        // Then...
        assertThat(runsGotBack).hasSize(3);
        assertThat(runsGotBack.get(0)).isEqualTo(run3);
        assertThat(runsGotBack.get(1)).isEqualTo(run1);
        assertThat(runsGotBack.get(2)).isEqualTo(run2);

        // Check that both tag priorities have been added to the run's total priority
        assertThat(run3Priority).isEqualTo(220);
    }

    @Test
    public void testTagPriorityIsAddedToRunPriorityCorrectly() throws Exception {
        // Given...
        Tag tag1 = new Tag("high-priority-tag");
        tag1.setPriority(200);

        Tag tag2 = new Tag("another-tag");
        tag2.setPriority(20);

        Map<String, Tag> tagsMap = new HashMap<>();
        tagsMap.put(tag1.getName(), tag1);
        tagsMap.put(tag2.getName(), tag2);

        Instant now = Instant.now();
        List<IRun> runs = new ArrayList<>();
        MockRun run = new MockRun(null, null, "run3", null, null, null, null, false);
        run.setQueued(now);
        run.setStatus(TestRunLifecycleStatus.QUEUED.toString());
        run.setTags(Set.of("high-priority-tag", "another-tag"));

        runs.add(run);

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(runs);
        MockIConfigurationPropertyStoreService mockCps = new MockIConfigurationPropertyStoreService();
        MockTimeService mockTimeService = new MockTimeService(now);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockTagsService mockTagsService = new MockTagsService();

        PrioritySchedulingService schedulingService = new PrioritySchedulingService(mockFrameworkRuns, mockCps, mockRBACService, mockTimeService, mockTagsService);

        // When...
        double runPriority = schedulingService.getQueuedRunTotalPriorityPoints(run, tagsMap);

        // Then...
        // Check that both tag priorities have been added to the run's total priority
        assertThat(runPriority).isEqualTo(220);
    }
}
