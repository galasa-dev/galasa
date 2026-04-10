/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.rascleanup;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;

@Component(service = { IResourceManagementProvider.class })
public class RasRunCleanupResourceManagementProvider implements IResourceManagementProvider {

    public static final String TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR = "GALASA_TEST_RUN_CLEANUP_INTERVAL_HOURS";
    public static final String TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR = "GALASA_TEST_RUN_CLEANUP_MAX_AGE_DAYS";

    private static final int RESOURCE_MANAGEMENT_RAS_CLEANUP_POLL_INTERVAL_MINUTES = 5;

    private final Log logger = LogFactory.getLog(getClass());

    private RasRunCleanupScheduler rasRunCleanupScheduler;
    private IResourceManagement resourceManagement;
    private ITimeService timeService;
    private Environment env;

    public RasRunCleanupResourceManagementProvider() {
        this(new SystemTimeService(), new SystemEnvironment());
    }

    public RasRunCleanupResourceManagementProvider(ITimeService timeService, Environment env) {
        this.timeService = timeService;
        this.env = env;
    }

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.resourceManagement = resourceManagement;

        String testRunCleanupIntervalHoursStr = env.getenv(TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR);
        String testRunCleanupMaxAgeDaysStr = env.getenv(TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR);

        long initialRunCleanupIntervalHours = parseLong(testRunCleanupIntervalHoursStr, 0, TEST_RUN_CLEANUP_INTERVAL_HOURS_ENV_VAR);
        int initialRunCleanupMaxAgeDays = parseInt(testRunCleanupMaxAgeDaysStr, 0, TEST_RUN_CLEANUP_MAX_AGE_DAYS_ENV_VAR);

        // Only initialise the scheduler if the interval and max age are set
        if (initialRunCleanupIntervalHours > 0 && initialRunCleanupMaxAgeDays > 0) {
            try {
                this.rasRunCleanupScheduler = new RasRunCleanupScheduler(
                    framework,
                    resourceManagement.getScheduledExecutorService(),
                    initialRunCleanupIntervalHours,
                    initialRunCleanupMaxAgeDays,
                    this.timeService
                );
            } catch (FrameworkException e) {
                logger.error("Unable to initialise RAS cleanup scheduler", e);
            }
        }

        return true;
    }

    @Override
    public void start() {
        if (this.rasRunCleanupScheduler != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                rasRunCleanupScheduler,
                0,
                RESOURCE_MANAGEMENT_RAS_CLEANUP_POLL_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            );
        } else {
            logger.info("RAS cleanup process will not be scheduled");
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
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
