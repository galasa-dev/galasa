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
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.utils.ITimeService;

public class RasRunCleanupScheduler implements Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String CPS_NAMESPACE = "framework";
    private static final String TEST_RUN_CLEANUP_CPS_PREFIX = "ras.cleanup";
    private static final String TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY = "test.run.interval.hours";

    private IConfigurationPropertyStoreService cpsService;
    private ScheduledExecutorService scheduledExecutorService;
    private RasRunCleanup rasRunCleanup;

    private ScheduledFuture<?> cleanupTask;
    private long previousRunCleanupIntervalHours = Long.MIN_VALUE;

    public RasRunCleanupScheduler(
        IFramework framework,
        ScheduledExecutorService scheduledExecutorService,
        ITimeService timeService
    ) throws FrameworkException {
        this.cpsService = framework.getConfigurationPropertyService(CPS_NAMESPACE);
        this.scheduledExecutorService = scheduledExecutorService;

        rasRunCleanup = new RasRunCleanup(cpsService, framework.getResultArchiveStore(), timeService);
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
        if (cleanupTask != null) {
            // Cancel the previous task before scheduling a new one
            cleanupTask.cancel(false);
        }
        cleanupTask = scheduledExecutorService.scheduleWithFixedDelay(rasRunCleanup, 0, runCleanupIntervalHours, TimeUnit.HOURS);
    }

    private long getRunCleanupIntervalHours() throws ConfigurationPropertyStoreException {
        long runCleanupIntervalHours = 0;
        try {
            String runCleanupIntervalHoursStr = cpsService.getProperty(TEST_RUN_CLEANUP_CPS_PREFIX, TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY);
            if (runCleanupIntervalHoursStr != null) {
                runCleanupIntervalHours = Long.parseLong(runCleanupIntervalHoursStr);
            }

        } catch (NumberFormatException e) {
            logger.warn("Invalid CPS property value provided. A numeric value is expected for '" + TEST_RUN_CLEANUP_INTERVAL_HOURS_CPS_PROPERTY + "'.");
        }
        return runCleanupIntervalHours;
    }
    
}
