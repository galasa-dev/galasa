/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.utils.ITimeService;

/**
 * A thread that monitors the test run duration and interrupts the test if it exceeds
 * the configured timeout period. This helps prevent tests from hanging indefinitely.
 */
public class TestRunTimeoutMonitor extends Thread {

    // Poll every 30 seconds
    private static final long TIMEOUT_POLL_DELAY_MS = 30 * 1000;

    private final Log logger = LogFactory.getLog(this.getClass());

    private final IFramework framework;
    private final ITimeService timeService;
    private final Instant endTime;
    private final String runName;

    private boolean shutdown = false;

    /**
     * Creates a new timeout monitor for a test run.
     *
     * @param framework the framework instance
     * @param timeoutMinutes the timeout duration in minutes
     * @throws FrameworkException if the monitor cannot be initialized
     */
    protected TestRunTimeoutMonitor(IFramework framework, long timeoutMinutes, ITimeService timeService) throws FrameworkException {
        this.framework = framework;
        this.timeService = timeService;

        this.runName = framework.getTestRunName();
        this.endTime = timeService.now().plus(timeoutMinutes, ChronoUnit.MINUTES);

        logger.info("Test run timeout monitor initialized. The test run will be interrupted if it runs beyond " + endTime.toString());
    }

    /**
     * Shuts down the timeout monitor thread.
     */
    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public void run() {
        while (!shutdown) {
            Instant now = timeService.now();

            // Check if the current time has exceeded the end time
            if (now.isAfter(endTime)) {
                logger.warn("Test run has exceeded the timeout limit. Interrupting test run: " + runName);
                interruptTestRun();
                break;
            }

            try {
                timeService.sleepMillis(TIMEOUT_POLL_DELAY_MS);
            } catch (InterruptedException e) {
                shutdown = true;
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("Test run timeout monitor stopped for run: " + runName);
    }

    /**
     * Interrupts the test run by marking it as hung in the DSS.
     */
    private void interruptTestRun() {
        try {
            logger.info("Marking test run as 'Hung' due to timeout: " + runName);
            framework.getFrameworkRuns().markRunInterrupted(runName, Result.HUNG);
        } catch (FrameworkException e) {
            logger.error("Failed to mark test run as hung", e);
        }
    }
}
