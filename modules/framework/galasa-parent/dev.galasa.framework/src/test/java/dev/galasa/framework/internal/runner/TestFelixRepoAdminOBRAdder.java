/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.internal.runner.FelixRepoAdminOBRAdder.*;

import java.time.Instant;
import java.util.ArrayList;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.junit.Test;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.mocks.MockRepositoryAdmin;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.utils.ITimeService;

public class TestFelixRepoAdminOBRAdder {

    @Test
    public void testCanInstantiateFelixRepoAdminOBRAdder() {
        // Given...
        RepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        // When...
        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // Then...
        assertThat(adder).isNotNull();
    }

    @Test
    public void testAddOBRsWithNullStreamNameDoesNotThrowException() throws Exception {
        // Given...
        RepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        IConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);
        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(null, null);

        // Then...
        // No exception should be thrown
        assertThat(repoAdmin.listRepositories()).isEmpty();
    }

    @Test
    public void testAddSingleOBRSuccessfully() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String obrUrl = "https://example.com/obr.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrl);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(1);
        assertThat(repoAdmin.listRepositories()[0].getURI().toString()).isEqualTo(obrUrl);
    }

    @Test
    public void testAddMultipleOBRsSuccessfully() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String obrUrls = "https://example.com/obr1.xml,https://example.com/obr2.xml,https://example.com/obr3.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrls);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(3);
    }

    @Test
    public void testAddOBRsWithSpacesAreTrimmed() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String obrUrls = " https://example.com/obr1.xml , https://example.com/obr2.xml ";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrls);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(2);
        assertThat(repoAdmin.listRepositories()[0].getURI().toString()).isEqualTo("https://example.com/obr1.xml");
        assertThat(repoAdmin.listRepositories()[1].getURI().toString()).isEqualTo("https://example.com/obr2.xml");
    }

    @Test
    public void testAddOBRsIgnoresEmptyStrings() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String obrUrls = "https://example.com/obr1.xml,,https://example.com/obr2.xml,  ,https://example.com/obr3.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrls);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(3);
    }

    @Test
    public void testRunOBRListOverridesStreamOBR() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String streamObrUrl = "https://example.com/stream-obr.xml";
        String runObrUrl = "https://example.com/run-obr.xml";

        cps.setProperty("test.stream." + streamName + ".obr", streamObrUrl);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, runObrUrl);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(1);
        assertThat(repoAdmin.listRepositories()[0].getURI().toString()).isEqualTo(runObrUrl);
    }

    @Test
    public void testAddOBRWithRetrySucceedsOnFirstAttempt() throws Exception {
        // Given...
        MockRepositoryAdmin repoAdmin = new MockRepositoryAdmin(new ArrayList<>(), null);
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant now = Instant.now();
        ITimeService timeService = new MockTimeService(now);

        String streamName = "testStream";
        String obrUrl = "https://example.com/obr.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrl);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(1);
    }

    @Test
    public void testAddOBRRetriesOnFailure() throws Exception {
        // Given...
        // Fail first 2 attempts
        FailingMockRepositoryAdmin repoAdmin = new FailingMockRepositoryAdmin(2);

        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant startTime = Instant.now();
        MockTimeService timeService = new MockTimeService(startTime);

        String streamName = "testStream";
        String obrUrl = "https://example.com/obr.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrl);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.getAttemptCount()).isEqualTo(3);
        assertThat(repoAdmin.listRepositories()).hasSize(1);

        // Should have slept twice between retries
        long expectedDelayMillis = 2 * DELAY_BETWEEN_ADD_OBR_OPERATION_RETRIES_MILLISECS;
        assertThat(timeService.now()).isEqualTo(startTime.plusMillis(expectedDelayMillis));
    }

    @Test
    public void testAddOBRFailsAfterMaxRetries() throws Exception {
        // Given...
        // Fail more than max retries
        FailingMockRepositoryAdmin repoAdmin = new FailingMockRepositoryAdmin(MAX_ADD_OBR_OPERATION_RETRIES + 3);

        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant startTime = Instant.now();
        MockTimeService timeService = new MockTimeService(startTime);

        String streamName = "testStream";
        String obrUrl = "https://example.com/obr.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrl);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When/Then...
        assertThatThrownBy(() -> adder.addOBRsToRepoAdmin(streamName, null))
            .isInstanceOf(TestRunException.class)
            .hasMessageContaining("Unable to load specified OBR")
            .hasMessageContaining("after " + MAX_ADD_OBR_OPERATION_RETRIES + " attempts");

        assertThat(repoAdmin.getAttemptCount()).isEqualTo(MAX_ADD_OBR_OPERATION_RETRIES);

        // Should have slept 9 times
        long expectedDelayMillis = 9 * DELAY_BETWEEN_ADD_OBR_OPERATION_RETRIES_MILLISECS;
        assertThat(timeService.now()).isEqualTo(startTime.plusMillis(expectedDelayMillis));
    }

    @Test
    public void testAddMultipleOBRsWithSomeFailures() throws Exception {
        // Given...
        SelectivelyFailingMockRepositoryAdmin repoAdmin = new SelectivelyFailingMockRepositoryAdmin();
        repoAdmin.setFailingUrl("https://example.com/obr2.xml", 2); // Second OBR fails twice

        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService();
        Instant startTime = Instant.now();
        MockTimeService timeService = new MockTimeService(startTime);

        String streamName = "testStream";
        String obrUrls = "https://example.com/obr1.xml,https://example.com/obr2.xml,https://example.com/obr3.xml";
        cps.setProperty("test.stream." + streamName + ".obr", obrUrls);

        FelixRepoAdminOBRAdder adder = new FelixRepoAdminOBRAdder(repoAdmin, cps, timeService);

        // When...
        adder.addOBRsToRepoAdmin(streamName, null);

        // Then...
        assertThat(repoAdmin.listRepositories()).hasSize(3);
        assertThat(repoAdmin.getAttemptCount("https://example.com/obr1.xml")).isEqualTo(1);
        assertThat(repoAdmin.getAttemptCount("https://example.com/obr2.xml")).isEqualTo(3);
        assertThat(repoAdmin.getAttemptCount("https://example.com/obr3.xml")).isEqualTo(1);

        // Only slept for two retries
        long expectedDelayMillis = 2 * DELAY_BETWEEN_ADD_OBR_OPERATION_RETRIES_MILLISECS;
        assertThat(timeService.now()).isEqualTo(startTime.plusMillis(expectedDelayMillis));
    }

    // Helper class that fails a specified number of times before succeeding
    private static class FailingMockRepositoryAdmin extends MockRepositoryAdmin {
        private int failuresRemaining;
        private int attemptCount = 0;

        public FailingMockRepositoryAdmin(int failuresBeforeSuccess) {
            super(new ArrayList<>(), null);
            this.failuresRemaining = failuresBeforeSuccess;
        }

        @Override
        public Repository addRepository(String repository) throws Exception {
            attemptCount++;
            if (failuresRemaining > 0) {
                failuresRemaining--;
                throw new Exception("Simulated HTTP 502 error");
            }
            return super.addRepository(repository);
        }

        public int getAttemptCount() {
            return attemptCount;
        }
    }

    // Helper class that can fail selectively for specific URLs
    private static class SelectivelyFailingMockRepositoryAdmin extends MockRepositoryAdmin {
        private java.util.Map<String, Integer> failureMap = new java.util.HashMap<>();
        private java.util.Map<String, Integer> attemptMap = new java.util.HashMap<>();

        public SelectivelyFailingMockRepositoryAdmin() {
            super(new ArrayList<>(), null);
        }

        public void setFailingUrl(String url, int failuresBeforeSuccess) {
            failureMap.put(url, failuresBeforeSuccess);
            attemptMap.put(url, 0);
        }

        @Override
        public Repository addRepository(String repository) throws Exception {
            attemptMap.put(repository, attemptMap.getOrDefault(repository, 0) + 1);

            if (failureMap.containsKey(repository)) {
                int failuresRemaining = failureMap.get(repository);
                if (failuresRemaining > 0) {
                    failureMap.put(repository, failuresRemaining - 1);
                    throw new Exception("Simulated HTTP 502 error for " + repository);
                }
            }
            return super.addRepository(repository);
        }

        public int getAttemptCount(String url) {
            return attemptMap.getOrDefault(url, 0);
        }
    }
}
