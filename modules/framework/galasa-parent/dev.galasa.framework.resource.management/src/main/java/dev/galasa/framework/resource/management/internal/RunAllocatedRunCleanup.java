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
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.utils.ITimeService;

public class RunAllocatedRunCleanup implements Runnable {

    private final IResourceManagement resourceManagement;
    private final IFrameworkRuns      frameworkRuns;
    private final ITimeService        timeService;
    private final Log                 logger = LogFactory.getLog(this.getClass());

    protected RunAllocatedRunCleanup(
        IFrameworkRuns frameworkRuns,
        IResourceManagement resourceManagement,
        ITimeService timeService
    ) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = frameworkRuns;
        this.timeService = timeService;
        this.logger.info("Allocated runs cleanup monitor initialised");
    }

    public void run() {
        logger.info("Starting search for allocated runs to clean up");
        try {
            List<IRun> runs = frameworkRuns.getAllRuns();
            for (IRun run : runs) {
                String runName = run.getName();
                Instant now = timeService.now();

                String status = run.getStatus();
                if (TestRunLifecycleStatus.ALLOCATED.toString().equals(status)) {
                    Instant allocatedRunExpiryTime = run.getAllocatedTimeout();
                    if (allocatedRunExpiryTime != null && now.isAfter(allocatedRunExpiryTime)) {
                        logger.info("Interrupting run " + runName + " as the run has been in the 'allocated' state for too long");
                        this.frameworkRuns.markRunInterrupted(runName, Result.HUNG);
                    }
                }
            }
        } catch (FrameworkException e) {
            logger.error("Scan of allocated runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished search for allocated runs to clean up");
    }
}
