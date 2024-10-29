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

public class RunFinishedRuns implements Runnable {

    private final IResourceManagement                resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IFrameworkRuns                     frameworkRuns;
    private final Log                                logger = LogFactory.getLog(this.getClass());

    private final DateTimeFormatter                  dtf    = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            .withZone(ZoneId.systemDefault());

    protected RunFinishedRuns(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement,
            IConfigurationPropertyStoreService cps) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.cps = cps;
        this.logger.info("Finished Runs Monitor initialised");
    }

    @Override
    public void run() {
        int defaultFinishedDelete = 300; // ** 5 minutes
        try { // TODO do we need a different timeout for automation run reset?
            String overrideTime = AbstractManager.nulled(cps.getProperty("resource.management", "finished.timeout"));
            if (overrideTime != null) {
                defaultFinishedDelete = Integer.parseInt(overrideTime);
            }
        } catch (Exception e) {
            logger.error("Problem with resource.management.finished.timeout, using default " + defaultFinishedDelete,
                    e);
        }

        logger.info("Starting Finished Run search");
        try {
            List<IRun> runs = frameworkRuns.getAllRuns();
            for (IRun run : runs) {
                String runName = run.getName();

                String status = run.getStatus();
                if (!"finished".equals(status)) {
                    continue;
                }

                Instant finished = run.getFinished();
                Instant expires = finished.plusSeconds(defaultFinishedDelete);
                Instant now = Instant.now();
                if (expires.compareTo(now) <= 0) {
                    String sFinished = dtf.format(LocalDateTime.ofInstant(finished, ZoneId.systemDefault()));
                    /// TODO put time management into the framework
                    logger.info("Deleting run " + runName + ", finished at " + sFinished);
                    this.frameworkRuns.delete(runName);
                }
            }
        } catch (FrameworkException e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Finished search");
    }

}