/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.ITimeService;

public class RasRunCleanupScheduler implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    public static final String TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR = "GALASA_TEST_RUN_CLEANUP_INTERVAL_HOURS";
    public static final String TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR = "GALASA_TEST_RUN_CLEANUP_MAX_AGE_DAYS";

    private static final String CPS_NAMESPACE = "framework";
    private static final String TEST_RUN_CLEANUP_CPS_PREFIX = "ras.cleanup";
    private static final String TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY = "test.run.interval.hours";

    private static final long DEFAULT_RUN_CLEANUP_INTERVAL_HOURS = 24;
    private static final int DEFAULT_RUN_CLEANUP_MAX_AGE_DAYS = 30;

    private long initialRunCleanupIntervalHours;
    private int initialRunCleanupMaxAgeDays;

    private IConfigurationPropertyStoreService cpsService;
    private ScheduledExecutorService scheduledExecutorService;
    private RasRunCleanup rasRunCleanup;

    private ScheduledFuture<?> cleanupTask;
    private long previousRunCleanupIntervalHours = Long.MIN_VALUE;

    public RasRunCleanupScheduler(
        IFramework framework,
        ScheduledExecutorService scheduledExecutorService,
        ITimeService timeService,
        Environment env
    ) throws FrameworkException {
        this.cpsService = framework.getConfigurationPropertyService(CPS_NAMESPACE);
        this.scheduledExecutorService = scheduledExecutorService;

        String testRunCleanupIntervalHoursStr = env.getenv(TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR);
        String testRunCleanupMaxAgeDaysStr = env.getenv(TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR);

        this.initialRunCleanupIntervalHours = parseLong(testRunCleanupIntervalHoursStr, DEFAULT_RUN_CLEANUP_INTERVAL_HOURS, TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR);
        this.initialRunCleanupMaxAgeDays = parseInt(testRunCleanupMaxAgeDaysStr, DEFAULT_RUN_CLEANUP_MAX_AGE_DAYS, TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR);

        rasRunCleanup = new RasRunCleanup(cpsService, framework.getResultArchiveStore(), timeService, initialRunCleanupMaxAgeDays);
    }

    @Override
    public void run() {
        try {
            long runCleanupIntervalHours = getRunCleanupIntervalHours();

            if (runCleanupIntervalHours > 0 && runCleanupIntervalHours != previousRunCleanupIntervalHours) {
                // Only reschedule if the interval has changed
                previousRunCleanupIntervalHours = runCleanupIntervalHours;
                scheduleCleanupTask(runCleanupIntervalHours);

            }
        } catch (Exception e) {
            logger.error("Error while getting run cleanup interval", e);
        }

    }

    private void scheduleCleanupTask(long runCleanupIntervalHours) {
        logger.trace("Scheduling cleanup task with interval (in hours): " + runCleanupIntervalHours);
        if (cleanupTask != null) {
            // Cancel the previous task before scheduling a new one
            logger.trace("Cancelling previously scheduled cleanup task");
            cleanupTask.cancel(false);
        }
        cleanupTask = scheduledExecutorService.scheduleWithFixedDelay(rasRunCleanup, 0, runCleanupIntervalHours, TimeUnit.HOURS);
        logger.trace("Scheduled cleanup task with interval (in hours): " + runCleanupIntervalHours);
    }

    private long getRunCleanupIntervalHours() throws ConfigurationPropertyStoreException {
        long runCleanupIntervalHours = initialRunCleanupIntervalHours;
        String runCleanupIntervalHoursStr = cpsService.getProperty(TEST_RUN_CLEANUP_CPS_PREFIX, TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY);
        if (runCleanupIntervalHoursStr != null) {
            runCleanupIntervalHours = parseLong(runCleanupIntervalHoursStr, runCleanupIntervalHours, TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY);
        }

        return runCleanupIntervalHours;
    }

    private long parseLong(String valueToParse, long defaultValue, String identifier) {
        long valueToReturn = defaultValue;
        try {
            valueToReturn = Long.parseLong(valueToParse);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value provided. A numeric value is expected for '" + identifier + "'.");
        }
        return valueToReturn;
    }

    private int parseInt(String valueToParse, int defaultValue, String identifier) {
        int valueToReturn = defaultValue;
        try {
            valueToReturn = Integer.parseInt(valueToParse);
        } catch (NumberFormatException e) {
            logger.warn("Invalid value provided. A numeric value is expected for '" + identifier + "'.");
        }
        return valueToReturn;
    }
}
