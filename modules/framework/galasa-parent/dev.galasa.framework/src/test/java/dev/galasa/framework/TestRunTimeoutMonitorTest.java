/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.utils.ITimeService;

public class TestRunTimeoutMonitorTest {

    class MockTimeServiceWithSleepCount extends MockTimeService {
        int sleepCount = 0;

        public MockTimeServiceWithSleepCount(Instant currentTime) {
            super(currentTime);
        }

        @Override
        public void sleepMillis(long millisToSleep) throws InterruptedException {
            sleepCount++;
            super.sleepMillis(millisToSleep);
        }

        public int getSleepCount() {
            return sleepCount;
        }
    }

    class MockTimeServiceThatThrowsInterrupt extends MockTimeServiceWithSleepCount {

        public MockTimeServiceThatThrowsInterrupt(Instant currentTime) {
            super(currentTime);
        }

        @Override
        public void sleepMillis(long millisToSleep) throws InterruptedException {
            super.sleepMillis(millisToSleep);

            if (getSleepCount() >= 2) {
                throw new InterruptedException("Test interrupt");
            }
        }
    }

    @Test
    public void testRunMethodStopsWhenInterruptOccurs() throws Exception {
        // Given...
        String runName = "RUN001";
        long timeoutMinutes = 60;
        Instant startTime = Instant.now();

        MockTimeServiceThatThrowsInterrupt timeService = new MockTimeServiceThatThrowsInterrupt(startTime);

        MockRun mockRun = createMockRun(runName);
        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns(List.of(mockRun));
        MockFramework framework = createMockFramework(runName, frameworkRuns);

        TestRunTimeoutMonitor monitor = new TestRunTimeoutMonitor(framework, timeoutMinutes, timeService);

        // When...
        monitor.run();

        // Then...
        assertThat(mockRun.getInterruptReason()).isNull();
        assertThat(timeService.getSleepCount()).isEqualTo(2);
    }

    @Test
    public void testRunMethodDetectsTimeoutAndInterruptsRun() throws Exception {
        // Given...
        String runName = "RUN001";
        long timeoutMinutes = 1;
        Instant startTime = Instant.now();

        MockTimeServiceWithSleepCount timeService = new MockTimeServiceWithSleepCount(startTime);

        MockRun mockRun = createMockRun(runName);
        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns(List.of(mockRun));
        MockFramework framework = createMockFramework(runName, frameworkRuns);

        TestRunTimeoutMonitor monitor = new TestRunTimeoutMonitor(framework, timeoutMinutes, timeService);

        // When...
        monitor.run();

        // Then...
        assertThat(mockRun.getInterruptReason()).isEqualTo(Result.HUNG);
        assertThat(timeService.getSleepCount()).isEqualTo(3);
    }

    @Test
    public void testRunMethodStopsImmediatelyWhenTimeAlreadyExceeded() throws Exception {
        // Given...
        String runName = "RUN001";
        long timeoutMinutes = 1;
        Instant startTime = Instant.now();

        // Create a time service where current time is already past the timeout
        ITimeService timeService = new MockTimeService(startTime.plus(2, ChronoUnit.MINUTES));

        MockRun mockRun = createMockRun(runName);
        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns(List.of(mockRun));
        MockFramework framework = createMockFramework(runName, frameworkRuns);

        TestRunTimeoutMonitor monitor = new TestRunTimeoutMonitor(framework, timeoutMinutes, timeService);

        // When...
        monitor.run();

        // Then...
        assertThat(mockRun.getInterruptReason()).isEqualTo(Result.HUNG);
    }

    @Test
    public void testRunMethodHandlesFrameworkExceptionGracefully() throws Exception {
        // Given...
        String runName = "RUN001";
        long timeoutMinutes = 1;
        Instant startTime = Instant.now();

        ITimeService timeService = new MockTimeService(startTime);

        // Create a framework runs that throws an exception
        MockFrameworkRuns frameworkRuns = new MockFrameworkRuns(new ArrayList<>()) {
            @Override
            public boolean markRunInterrupted(String runName, String interruptReason) throws DynamicStatusStoreException {
                throw new DynamicStatusStoreException("Simulated framework exception");
            }
        };

        MockFramework framework = createMockFramework(runName, frameworkRuns);

        TestRunTimeoutMonitor monitor = new TestRunTimeoutMonitor(framework, timeoutMinutes, timeService);

        // When...
        monitor.run();

        // Then...
        // The run method should complete without throwing an exception
    }

    private MockFramework createMockFramework(String runName, MockFrameworkRuns frameworkRuns) throws FrameworkException {
        MockFramework framework = new MockFramework() {
            @Override
            public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
                return frameworkRuns;
            }
        };
        framework.setTestRunName(runName);
        return framework;
    }

    private MockRun createMockRun(String runName) {
        return new MockRun(
            "test.bundle",
            "TestClass",
            runName,
            "testStream",
            "testOBR",
            "http://repo.url",
            "testUser",
            false
        );
    }
}
