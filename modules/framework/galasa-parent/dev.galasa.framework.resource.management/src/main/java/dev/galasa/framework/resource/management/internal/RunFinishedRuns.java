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
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.IRun;

public class RunFinishedRuns implements Runnable {

    private final IResourceManagement                resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IFrameworkRuns                     frameworkRuns;
    private final Log                                logger = LogFactory.getLog(this.getClass());

    private final DateTimeFormatter                  dtf    = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            .withZone(ZoneId.systemDefault());

    protected RunFinishedRuns(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, IResourceManagementProvider runResourceManagement,
            IConfigurationPropertyStoreService cps) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.cps = cps;
        this.logger.info("Finished Runs Monitor initialised");
    }

    @Override
    public void run() {
        logger.info("Entering run() method");
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
        logger.info("Finished fetching defaultFinishedDelete value: " + defaultFinishedDelete);

        logger.info("Starting Finished Run search");
        try {
            logger.info("About to call getAllRuns()");
            List<IRun> runs = frameworkRuns.getAllRuns();
            logger.info("Finished calling getAllRuns()");
            for (IRun run : runs) {
                String runName = run.getName();
                logger.info("Found run with run name: " + runName);

                String status = run.getStatus();
                logger.info("The runs status is: " + status);
                if (!"finished".equals(status)) {
                    continue;
                }

                Instant finished = run.getFinished();
                logger.info("The runs finished time is: " + finished);
                Instant expires = null;
                if (finished != null) {
                    expires = finished.plusSeconds(defaultFinishedDelete);
                }

                logger.info("The runs expired time is: " + expires);
                Instant now = Instant.now();
                if (expires == null || expires.compareTo(now) <= 0) {
                    logger.info("The run has expired so we will attempt to delete it");
                    if (finished != null) {
                        String sFinished = dtf.format(LocalDateTime.ofInstant(finished, ZoneId.systemDefault()));
                        /// TODO put time management into the framework
                        logger.info("Deleting run " + runName + ", finished at " + sFinished);
                    } else {
                        logger.info("Deleting run " + runName + ", finished is null");
                    }

                    this.frameworkRuns.delete(runName);
                    logger.info("We have been able to delete the run in the delete() method");
                }
            }
        } catch (Exception e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Finished Run search");
    }

}