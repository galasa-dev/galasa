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
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.RunRasAction;

public class RunWaitingRuns implements Runnable {

    private final IResourceManagement        resourceManagement;
    private final IFrameworkRuns             frameworkRuns;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger = LogFactory.getLog(this.getClass());

    protected RunWaitingRuns(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement,
            IConfigurationPropertyStoreService cps) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.logger.info("Waiting Runs Monitor initialised");
    }

    @Override
    public void run() {
        logger.info("Starting Waiting Run search");
        try {
            List<IRun> runs = frameworkRuns.getAllRuns();
            for (IRun run : runs) {
                String runName = run.getName();

                String status = run.getStatus();
                if (!"waiting".equals(status)) {
                    continue;
                }

                Instant finished = run.getWaitUntil();
                if (finished == null) {
                    continue;
                }
                Instant now = Instant.now();
                if (finished.compareTo(now) <= 0) {
                    if (this.dss.putSwap("run." + run.getName() + ".status", "waiting", "queued")) {
                        // *** Leave the queue time as is as we want the waiting runs to be actioned
                        // before 1st time queued runs
                        logger.info("Requeueing Waiting run " + runName);

                        // Add a RAS action so that the record for the requeued run can be updated later
                        RunRasAction rasActionToAdd = new RunRasAction(run.getRasRunId(), TestRunLifecycleStatus.FINISHED.toString(), Result.REQUEUED);
                        frameworkRuns.addRunRasAction(run, rasActionToAdd);

                        this.dss.delete("run." + run.getName() + ".wait.until");
                    }
                }
            }
        } catch (FrameworkException e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Waiting search");
    }

}