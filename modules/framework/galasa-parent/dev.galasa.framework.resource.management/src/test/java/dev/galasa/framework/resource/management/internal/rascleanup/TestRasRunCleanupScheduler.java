/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.resource.management.internal.mocks.MockScheduledExecutorService;
import dev.galasa.framework.resource.management.internal.mocks.MockScheduledFuture;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class TestRasRunCleanupScheduler {

    @Test
    public void testCanInstantiateScheduler() throws Exception {
        // Given...
        MockFramework framework = createMockFramework(new HashMap<>());
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        // When...
        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // Then...
        assertThat(scheduler).as("Scheduler should be instantiated").isNotNull();
    }

    @Test
    public void testRunWithZeroIntervalDoesNotScheduleTask() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        // No interval property set
        MockFramework framework = createMockFramework(cpsProperties);
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        env.setenv(RasRunCleanupScheduler.TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR, "0");

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should not be scheduled when interval is zero").isEqualTo(0);
    }

    @Test
    public void testRunWithNegativeIntervalDoesNotScheduleTask() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "-5");
        MockFramework framework = createMockFramework(cpsProperties);
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should not be scheduled when interval is negative").isEqualTo(0);
    }

    @Test
    public void testRunWithValidIntervalSchedulesTask() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "24");
        MockFramework framework = createMockFramework(cpsProperties);
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should be scheduled once").isEqualTo(1);
        assertThat(executorService.lastDelay).as("Delay should be 24 hours").isEqualTo(24);
    }

    @Test
    public void testRunWithSameIntervalDoesNotReschedule() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "12");
        MockFramework framework = createMockFramework(cpsProperties);
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should only be scheduled once when interval hasn't changed").isEqualTo(1);
    }

    @Test
    public void testRunWithChangedIntervalReschedulesTask() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "12");
        MockFramework framework = createMockFramework(cpsProperties);

        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();
        MockScheduledFuture firstFuture = executorService.lastScheduledFuture;
        
        // Change the interval
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "24");
        scheduler.run();
        MockScheduledFuture secondFuture = executorService.lastScheduledFuture;

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should be rescheduled when interval changes").isEqualTo(2);
        assertThat(firstFuture.isCancelled()).as("Previous task should be cancelled").isTrue();
        assertThat(secondFuture.isCancelled()).as("New task should not be cancelled").isFalse();
        assertThat(executorService.lastDelay).as("New delay should be 24 hours").isEqualTo(24);
    }

    @Test
    public void testRunWithInvalidIntervalInCpsUsesDefault() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "not-a-number");
        MockFramework framework = createMockFramework(cpsProperties);
        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        env.setenv(RasRunCleanupScheduler.TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR, "10");

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should have been scheduled").isEqualTo(1);
        assertThat(executorService.lastDelay).as("Task should be scheduled using the default interval").isEqualTo(10);
    }

    @Test
    public void testRunCancelsExistingTaskBeforeRescheduling() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "6");
        MockFramework framework = createMockFramework(cpsProperties);

        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();
        MockScheduledFuture firstFuture = executorService.lastScheduledFuture;
        
        // Change interval to trigger rescheduling
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "12");
        scheduler.run();

        // Then...
        assertThat(firstFuture.isCancelled()).as("Previous task should be cancelled before rescheduling").isTrue();
    }

    @Test
    public void testRunHandlesExceptionGracefully() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        MockCPSStore cps = new MockCPSStore(cpsProperties) {
            @Override
            public String getProperty(String prefix, String suffix, String... infixes) throws ConfigurationPropertyStoreException {
                throw new ConfigurationPropertyStoreException("CPS error");
            }
        };
        MockFramework framework = new MockFramework(cps, null);
        framework.setMockRas(new MockRASStoreService(new HashMap<>()));

        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();

        // Then...
        // Should not throw exception and should not schedule task
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should not be scheduled when exception occurs").isEqualTo(0);
    }

    @Test
    public void testRunWithMultipleIntervalChanges() throws Exception {
        // Given...
        Map<String, String> cpsProperties = new HashMap<>();
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "6");
        MockCPSStore cps = new MockCPSStore(cpsProperties);
        MockFramework framework = new MockFramework(cps, null);
        framework.setMockRas(new MockRASStoreService(new HashMap<>()));

        MockScheduledExecutorService executorService = new MockScheduledExecutorService();
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        RasRunCleanupScheduler scheduler = new RasRunCleanupScheduler(framework, executorService, timeService, env);

        // When...
        scheduler.run();
        MockScheduledFuture firstFuture = executorService.lastScheduledFuture;
        
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "12");
        scheduler.run();
        MockScheduledFuture secondFuture = executorService.lastScheduledFuture;
        
        cpsProperties.put("ras.cleanup.test.run.interval.hours", "24");
        scheduler.run();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should be scheduled three times").isEqualTo(3);
        assertThat(firstFuture.isCancelled()).as("First task should be cancelled").isTrue();
        assertThat(secondFuture.isCancelled()).as("Second task should be cancelled").isTrue();
        assertThat(executorService.lastDelay).as("Final delay should be 24 hours").isEqualTo(24);
    }

    private MockFramework createMockFramework(Map<String, String> cpsProperties) {
        MockCPSStore cps = new MockCPSStore(cpsProperties);
        MockFramework framework = new MockFramework(cps, null);
        framework.setMockRas(new MockRASStoreService(new HashMap<>()));
        return framework;
    }
}
