/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;

import org.junit.Test;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockRASStoreService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.resource.management.internal.mocks.MockResourceManagement;
import dev.galasa.framework.resource.management.internal.mocks.MockScheduledExecutorService;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;

public class TestRasRunCleanupResourceManagementProvider {

    @Test
    public void testCanInstantiateProvider() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        // When...
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        // Then...
        assertThat(provider).as("Provider should be instantiated").isNotNull();
    }

    @Test
    public void testInitialiseReturnsTrue() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();

        // When...
        boolean result = provider.initialise(framework, resourceManagement);

        // Then...
        assertThat(result).as("Initialise should return true").isTrue();
    }

    @Test
    public void testInitialiseCreatesScheduler() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR, "24");
        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR, "30");

        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();

        // When...
        provider.initialise(framework, resourceManagement);

        // Then...
        // Scheduler should be created internally (we can verify this by calling start)
        provider.start();
        assertThat(resourceManagement.getScheduledExecutorService().scheduleWithFixedDelayCallCount)
            .as("Scheduler should be created and started")
            .isEqualTo(1);
    }

    @Test
    public void testInitialiseHandlesFrameworkException() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = new MockFramework(null, null) {
            @Override
            public MockCPSStore getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                throw new ConfigurationPropertyStoreException("Test exception");
            }
        };
        MockResourceManagement resourceManagement = new MockResourceManagement();

        // When...
        boolean result = provider.initialise(framework, resourceManagement);

        // Then...
        assertThat(result).as("Initialise should still return true even with exception").isTrue();
        
        // Start should not schedule anything since scheduler creation failed
        provider.start();
        assertThat(resourceManagement.getScheduledExecutorService().scheduleWithFixedDelayCallCount)
            .as("No scheduler should be started when initialisation fails")
            .isEqualTo(0);
    }

    @Test
    public void testStartSchedulesCleanupTask() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR, "24");
        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR, "30");

        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockScheduledExecutorService executorService = resourceManagement.getScheduledExecutorService();

        provider.initialise(framework, resourceManagement);

        // When...
        provider.start();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount).as("Task should be scheduled once").isEqualTo(1);
        assertThat(executorService.lastDelay).as("Delay should be 5 minutes").isEqualTo(5);
    }

    @Test
    public void testStartWithoutInitialiseDoesNotSchedule() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        // When...
        provider.start();

        // Then...
        // No exception should be thrown, and nothing should happen
    }

    @Test
    public void testStartWithFailedInitialiseDoesNotSchedule() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = new MockFramework(null, null) {
            @Override
            public MockCPSStore getConfigurationPropertyService(String namespace) throws ConfigurationPropertyStoreException {
                throw new ConfigurationPropertyStoreException("Test exception");
            }
        };
        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockScheduledExecutorService executorService = resourceManagement.getScheduledExecutorService();

        provider.initialise(framework, resourceManagement);

        // When...
        provider.start();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount)
            .as("Task should not be scheduled when scheduler is null")
            .isEqualTo(0);
    }

    @Test
    public void testShutdownDoesNotThrowException() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();

        provider.initialise(framework, resourceManagement);
        provider.start();

        // When...
        provider.shutdown();

        // Then...
        // Should not throw any exception
    }

    @Test
    public void testRunFinishedOrDeletedDoesNotThrowException() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();
        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();

        provider.initialise(framework, resourceManagement);

        // When...
        provider.runFinishedOrDeleted("test-run-123");

        // Then...
        // Should not throw any exception
    }

    @Test
    public void testMultipleStartCallsScheduleMultipleTimes() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockEnvironment env = new MockEnvironment();

        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR, "24");
        env.setenv(RasRunCleanupResourceManagementProvider.TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR, "30");

        RasRunCleanupResourceManagementProvider provider = new RasRunCleanupResourceManagementProvider(timeService, env);

        MockFramework framework = createMockFramework();
        MockResourceManagement resourceManagement = new MockResourceManagement();
        MockScheduledExecutorService executorService = resourceManagement.getScheduledExecutorService();

        provider.initialise(framework, resourceManagement);

        // When...
        provider.start();
        provider.start();
        provider.start();

        // Then...
        assertThat(executorService.scheduleWithFixedDelayCallCount)
            .as("Each start call should schedule a task")
            .isEqualTo(3);
    }

    private MockFramework createMockFramework() {
        MockCPSStore cps = new MockCPSStore(new HashMap<>());
        MockFramework framework = new MockFramework(cps, null);
        framework.setMockRas(new MockRASStoreService(new HashMap<>()));
        return framework;
    }
}
