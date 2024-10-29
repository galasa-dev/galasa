/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IRun;

public class RunDeadHeartbeatMonitor implements Runnable {

    private final IResourceManagement                resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IFrameworkRuns                     frameworkRuns;
    private final Log                                logger = LogFactory.getLog(this.getClass());

    private final DateTimeFormatter                  dtf    = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            .withZone(ZoneId.systemDefault());

    protected RunDeadHeartbeatMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement,
            IConfigurationPropertyStoreService cps) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.cps = cps;
        this.logger.info("Run Dead Heartbeat Monitor initialised");
    }

    @Override
    public void run() {
        int defaultDeadHeartbeatTime = 300; // ** 5 minutes
        try { // TODO do we need a different timeout for automation run reset?
            String overrideTime = AbstractManager
                    .nulled(cps.getProperty("resource.management", "dead.heartbeat.timeout"));
            if (overrideTime != null) {
                defaultDeadHeartbeatTime = Integer.parseInt(overrideTime);
            }
        } catch (Throwable e) {
            logger.error("Problem with resource.management.dead.heartbeat.timeout, using default "
                    + defaultDeadHeartbeatTime, e);
        }

        logger.info("Starting Run Dead Heartbeat search");
        try {
            logger.trace("Fetching list of Active Runs");
            List<IRun> runs = frameworkRuns.getActiveRuns();
            logger.trace("Active Run count = " + runs.size());
            for (IRun run : runs) {
                if (run.isSharedEnvironment()) {
                    continue;  //*** Ignore shared environments,  handled by a different class
                }
                String runName = run.getName();
                logger.trace("Checking run " + runName);

                Instant heartbeat = run.getHeartbeat();
                if (heartbeat == null) {
                    logger.warn("Active run without heartbeat = " + runName + " ignoring");
                    continue;
                }

                Instant expires = heartbeat.plusSeconds(defaultDeadHeartbeatTime);
                Instant now = Instant.now();
                if (expires.compareTo(now) <= 0) {
                    logger.trace("Run " + runName + " has a dead heartbeat");
                    String lastHeartbeat = dtf.format(LocalDateTime.ofInstant(heartbeat, ZoneId.systemDefault()));
                    if (run.isLocal()) {
                        /// TODO put time management into the framework
                        logger.warn("Deleting run " + runName + ", last heartbeat was at " + lastHeartbeat);
                        this.frameworkRuns.delete(runName);
                    } else {
                        logger.warn("Reseting run " + runName + ", last heartbeat was at " + lastHeartbeat);
                        this.frameworkRuns.reset(runName);
                    }
                } else {
                    logger.trace("Run " + runName + " heartbeat is ok");
                }
            }
        } catch (Throwable e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Dead Heartbeat search");
    }

}