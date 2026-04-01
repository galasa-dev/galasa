/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockIResultArchiveStore;
import dev.galasa.framework.mocks.MockResultArchiveStoreDirectoryService;
import dev.galasa.framework.mocks.MockRunResult;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.IRunResult;
import dev.galasa.framework.spi.teststructure.TestStructure;

public class TestRasRunCleanup {

    @Test
    public void testCanInstantiateRasRunCleanup() throws Exception {
        // Given...
        MockCPSStore cps = createMockCPSStore(new HashMap<>());
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        MockTimeService timeService = new MockTimeService(Instant.now());

        // When...
        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // Then...
        assertThat(cleanup).isNotNull();
    }

    @Test
    public void testRunWithNoMaxAgeDaysDoesNotCleanUpRuns() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        // No max age days property set
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        List<IRunResult> runs = createMockRuns(5);
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(Instant.now());

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        for (IRunResult run : runs) {
            MockRunResult mockRun = (MockRunResult) run;
            assertThat(mockRun.isDiscarded()).as("Run should not be discarded when no max age is set").isFalse();
        }
    }

    @Test
    public void testRunCleansUpOldRuns() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create runs: 2 old (should be cleaned), 2 recent (should not be cleaned)
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRun("run1", now.minus(40, ChronoUnit.DAYS))); // Old
        runs.add(createMockRun("run2", now.minus(35, ChronoUnit.DAYS))); // Old
        runs.add(createMockRun("run3", now.minus(20, ChronoUnit.DAYS))); // Recent
        runs.add(createMockRun("run4", now.minus(10, ChronoUnit.DAYS))); // Recent
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        MockRunResult run3 = (MockRunResult) runs.get(2);
        MockRunResult run4 = (MockRunResult) runs.get(3);
        
        assertThat(run1.isDiscarded()).as("Old run1 should be discarded").isTrue();
        assertThat(run2.isDiscarded()).as("Old run2 should be discarded").isTrue();
        assertThat(run3.isDiscarded()).as("Recent run3 should not be discarded").isFalse();
        assertThat(run4.isDiscarded()).as("Recent run4 should not be discarded").isFalse();
    }

    @Test
    public void testRunExcludesRunsWithMatchingTags() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        cpsProperties.put("test.run.exclude.tags", "production,critical");
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create old runs with different tags
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRunWithTags("run1", now.minus(40, ChronoUnit.DAYS), "production")); // Excluded
        runs.add(createMockRunWithTags("run2", now.minus(35, ChronoUnit.DAYS), "critical")); // Excluded
        runs.add(createMockRunWithTags("run3", now.minus(40, ChronoUnit.DAYS), "test")); // Should be cleaned
        runs.add(createMockRunWithTags("run4", now.minus(35, ChronoUnit.DAYS), "dev")); // Should be cleaned
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        MockRunResult run3 = (MockRunResult) runs.get(2);
        MockRunResult run4 = (MockRunResult) runs.get(3);
        
        assertThat(run1.isDiscarded()).as("Run with 'production' tag should be excluded from cleanup").isFalse();
        assertThat(run2.isDiscarded()).as("Run with 'critical' tag should be excluded from cleanup").isFalse();
        assertThat(run3.isDiscarded()).as("Run with 'test' tag should be cleaned up").isTrue();
        assertThat(run4.isDiscarded()).as("Run with 'dev' tag should be cleaned up").isTrue();
    }

    @Test
    public void testRunExcludesRunsWithMatchingUser() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        cpsProperties.put("test.run.exclude.user", "admin,system");
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create old runs with different users
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRunWithUser("run1", now.minus(40, ChronoUnit.DAYS), "admin")); // Excluded
        runs.add(createMockRunWithUser("run2", now.minus(35, ChronoUnit.DAYS), "system")); // Excluded
        runs.add(createMockRunWithUser("run3", now.minus(40, ChronoUnit.DAYS), "developer")); // Should be cleaned
        runs.add(createMockRunWithUser("run4", now.minus(35, ChronoUnit.DAYS), "tester")); // Should be cleaned
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        MockRunResult run3 = (MockRunResult) runs.get(2);
        MockRunResult run4 = (MockRunResult) runs.get(3);
        
        assertThat(run1.isDiscarded()).as("Run by 'admin' user should be excluded from cleanup").isFalse();
        assertThat(run2.isDiscarded()).as("Run by 'system' user should be excluded from cleanup").isFalse();
        assertThat(run3.isDiscarded()).as("Run by 'developer' user should be cleaned up").isTrue();
        assertThat(run4.isDiscarded()).as("Run by 'tester' user should be cleaned up").isTrue();
    }

    @Test
    public void testRunExcludesRunsWithMatchingBundle() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        cpsProperties.put("test.run.exclude.bundle", "dev.galasa.important.tests");
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create old runs with different bundles
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRunWithBundle("run1", now.minus(40, ChronoUnit.DAYS), "dev.galasa.important.tests")); // Excluded
        runs.add(createMockRunWithBundle("run2", now.minus(35, ChronoUnit.DAYS), "dev.galasa.other.tests")); // Should be cleaned
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        
        assertThat(run1.isDiscarded()).as("Run from important bundle should be excluded from cleanup").isFalse();
        assertThat(run2.isDiscarded()).as("Run from other bundle should be cleaned up").isTrue();
    }

    @Test
    public void testRunWithMultipleExcludeCriteria() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        cpsProperties.put("test.run.exclude.tags", "production");
        cpsProperties.put("test.run.exclude.user", "admin");
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create old runs
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRunWithTagsAndUser("run1", now.minus(40, ChronoUnit.DAYS), "production", "developer")); // Excluded by tag
        runs.add(createMockRunWithTagsAndUser("run2", now.minus(35, ChronoUnit.DAYS), "test", "admin")); // Excluded by user
        runs.add(createMockRunWithTagsAndUser("run3", now.minus(40, ChronoUnit.DAYS), "test", "developer")); // Should be cleaned
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        MockRunResult run3 = (MockRunResult) runs.get(2);
        
        assertThat(run1.isDiscarded()).as("Run with 'production' tag should be excluded").isFalse();
        assertThat(run2.isDiscarded()).as("Run by 'admin' user should be excluded").isFalse();
        assertThat(run3.isDiscarded()).as("Run matching no exclusions should be cleaned up").isTrue();
    }

    @Test
    public void testRunWithInvalidMaxAgeDaysProperty() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", "invalid");
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        List<IRunResult> runs = createMockRuns(3);
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(Instant.now());

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        // Should not throw exception, just log warning and not clean up runs
        for (IRunResult run : runs) {
            MockRunResult mockRun = (MockRunResult) run;
            assertThat(mockRun.isDiscarded()).as("Run should not be discarded with invalid max age").isFalse();
        }
    }

    @Test
    public void testRunWithWhitespaceInExcludeValues() throws Exception {
        // Given...
        Instant now = Instant.now();
        int maxAgeDays = 30;
        
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.age.max.days", String.valueOf(maxAgeDays));
        cpsProperties.put("test.run.exclude.tags", " production , critical "); // With whitespace
        MockCPSStore cps = createMockCPSStore(cpsProperties);
        
        // Create old runs
        List<IRunResult> runs = new ArrayList<>();
        runs.add(createMockRunWithTags("run1", now.minus(40, ChronoUnit.DAYS), "production")); // Should be excluded
        runs.add(createMockRunWithTags("run2", now.minus(35, ChronoUnit.DAYS), "critical")); // Should be excluded
        
        MockResultArchiveStoreDirectoryService directoryService = new MockResultArchiveStoreDirectoryService(runs);
        MockIResultArchiveStore ras = new MockIResultArchiveStore();
        ras.addDirectoryService(directoryService);
        
        MockTimeService timeService = new MockTimeService(now);

        RasRunCleanup cleanup = new RasRunCleanup(cps, ras, timeService);

        // When...
        cleanup.run();

        // Then...
        MockRunResult run1 = (MockRunResult) runs.get(0);
        MockRunResult run2 = (MockRunResult) runs.get(1);
        
        assertThat(run1.isDiscarded()).as("Whitespace should be trimmed, run should be excluded").isFalse();
        assertThat(run2.isDiscarded()).as("Whitespace should be trimmed, run should be excluded").isFalse();
    }

    // Helper methods

    private MockCPSStore createMockCPSStore(Map<String, String> properties) {
        return new MockCPSStore(properties) {
            @Override
            public Map<String, String> getPrefixedProperties(String prefix) {
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (entry.getKey().startsWith(prefix)) {
                        result.put(entry.getKey(), entry.getValue());
                    }
                }
                return result;
            }
        };
    }

    private List<IRunResult> createMockRuns(int count) {
        List<IRunResult> runs = new ArrayList<>();
        Instant now = Instant.now();
        for (int i = 0; i < count; i++) {
            runs.add(createMockRun("run" + i, now.minus(40, ChronoUnit.DAYS), null, null));
        }
        return runs;
    }

    private IRunResult createMockRun(String runId, Instant startTime) {
        return createMockRun(runId, startTime, null, null);
    }

    private IRunResult createMockRunWithTags(String runId, Instant startTime, String... tags) {
        return createMockRun(runId, startTime, null, null, tags);
    }

    private IRunResult createMockRunWithUser(String runId, Instant startTime, String user) {
        return createMockRun(runId, startTime, null, user);
    }

    private IRunResult createMockRunWithBundle(String runId, Instant startTime, String bundle) {
        return createMockRun(runId, startTime, bundle, null);
    }

    private IRunResult createMockRunWithTagsAndUser(String runId, Instant startTime, String tag, String user) {
        return createMockRun(runId, startTime, null, user, tag);
    }

    private IRunResult createMockRun(String runId, Instant startTime, String bundle, String user, String... tags) {
        TestStructure testStructure = new TestStructure();
        testStructure.setRunName(runId);
        testStructure.setStartTime(startTime);
        testStructure.setBundle(bundle != null ? bundle : "dev.galasa.test");
        testStructure.setTestName("TestClass");
        
        if (user != null) {
            testStructure.setRequestor(user);
        }
        
        if (tags != null && tags.length > 0) {
            Set<String> tagSet = new HashSet<>();
            for (String tag : tags) {
                tagSet.add(tag);
            }
            testStructure.setTags(tagSet);
        }
        
        return new MockRunResult(runId, testStructure, null, "");
    }
}
