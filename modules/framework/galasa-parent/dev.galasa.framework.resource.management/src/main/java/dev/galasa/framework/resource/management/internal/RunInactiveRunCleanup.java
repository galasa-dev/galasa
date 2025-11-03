/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.time.Instant;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.TestRunLifecycleStatus;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.utils.ITimeService;

public class RunInactiveRunCleanup implements Runnable {

    // The default amount of time in seconds that local runs can be left in 'queued'
    private static final int DEFAULT_MAX_LOCAL_RUN_QUEUED_TIME_SECS = 60 * 15;

    private final IResourceManagement resourceManagement;
    private final IFrameworkRuns      frameworkRuns;
    private final ITimeService        timeService;
    private final Log                 logger = LogFactory.getLog(this.getClass());
    private final IConfigurationPropertyStoreService cps;

    protected RunInactiveRunCleanup(
        IFrameworkRuns frameworkRuns,
        IResourceManagement resourceManagement,
        ITimeService timeService,
        IConfigurationPropertyStoreService cps
    ) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = frameworkRuns;
        this.timeService = timeService;
        this.cps = cps;
        this.logger.info("Inactive runs cleanup monitor initialised");
    }

    @Override
    public void run() {
        logger.info("Starting search for inactive runs to clean up");
        try {
            List<IRun> runs = frameworkRuns.getAllRuns();
            for (IRun run : runs) {

                String status = run.getStatus();
                if (TestRunLifecycleStatus.ALLOCATED.toString().equals(status)) {
                    interruptAllocatedRunIfItHasTimedOut(run);
                } else if (run.isLocal()) {
                    processLocalRunCleanup(run);
                }
            }
        } catch (FrameworkException e) {
            logger.error("Scan of inactive runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished search for inactive runs to clean up");
    }

    private void interruptAllocatedRunIfItHasTimedOut(IRun run) throws DynamicStatusStoreException {
        Instant now = timeService.now();
        Instant allocatedRunExpiryTime = run.getAllocatedTimeout();
        if (allocatedRunExpiryTime != null && now.isAfter(allocatedRunExpiryTime)) {
            String runName = run.getName();
            logger.info("Interrupting run " + runName + " as the run has been in the 'allocated' state for too long");
            this.frameworkRuns.markRunInterrupted(runName, Result.HUNG);
        }
    }

    private void processLocalRunCleanup(IRun run) throws DynamicStatusStoreException {
        String status = run.getStatus();

        // Local runs may remain in the 'queued' state if a user stops the JVM after submitting a test,
        // so clean up runs in the 'queued' state if they have been in the DSS for too long.
        if (TestRunLifecycleStatus.QUEUED.toString().equals(status)) {
            Instant runQueuedTime = run.getQueued();
            Instant now = timeService.now();
            int maxLocalRunQueuedTimeSecs = getMaxLocalRunQueuedTimeSecs();
            Instant queuedRunExpiryTime = runQueuedTime.plusSeconds(maxLocalRunQueuedTimeSecs);
            String runName = run.getName();

            if (runQueuedTime != null && now.isAfter(queuedRunExpiryTime)) {
                logger.info("Interrupting local run " + runName + " as the run has been in the 'queued' state for too long");
                this.frameworkRuns.markRunInterrupted(runName, Result.HUNG);
            } else {
                logger.info("Local run " + runName + " has not been in the DSS for too long. It will expire after " + queuedRunExpiryTime.toString());
            }
        }
    }

    /**
     * Gets the maximum number of seconds that local runs can remain in the 'queued' state before needing to
     * be cleaned up.
     *
     * This value can be customised using the CPS property:
     *     framework.resource.management.queued.timeout=[number of seconds]
     *
     * If no value is set, then a default value will be returned.
     */
    private int getMaxLocalRunQueuedTimeSecs() {
        int maxLocalRunQueuedTimeSecs = DEFAULT_MAX_LOCAL_RUN_QUEUED_TIME_SECS;
        try {
            String overrideTime = AbstractManager.nulled(cps.getProperty("resource.management", "local.queued.timeout"));
            if (overrideTime != null) {
                maxLocalRunQueuedTimeSecs = Integer.parseInt(overrideTime);
            }
        } catch (Throwable e) {
            logger.error("Problem with resource.management.local.queued.timeout, using default "
                    + maxLocalRunQueuedTimeSecs, e);
        }
        return maxLocalRunQueuedTimeSecs;
    }
}
