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

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IRun;

public class RunExpiredSharedEnvironment implements Runnable {

    private final IResourceManagement                resourceManagement;
    private final IDynamicStatusStoreService         dss;
    private final IFrameworkRuns                     frameworkRuns;
    private final Log                                logger = LogFactory.getLog(this.getClass());

    protected RunExpiredSharedEnvironment(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, RunResourceManagement runResourceManagement,
            IConfigurationPropertyStoreService cps) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.dss = dss;
        this.logger.info("Run Expired Shared Environment Monitor initialised");
    }

    @Override
    public void run() {
        Instant now = Instant.now();

        logger.info("Starting Expired Shared Environment search");
        try {
            logger.trace("Fetching list of Active Runs");
            List<IRun> runs = frameworkRuns.getActiveRuns();
            logger.trace("Active Run count = " + runs.size());
            for (IRun run : runs) {
                if (!run.isSharedEnvironment()) {
                    continue;  //*** Only want shared environments
                }
                String runName = run.getName();
                logger.trace("Checking shared envirnonment " + runName);


                try {
                    String sExpire = AbstractManager.nulled(dss.get("run." + runName + ".shared.environment.expire"));
                    if (sExpire == null) {
                        logger.trace("Expire for shared environment is missing");
                        continue;
                    }

                    Instant expire = Instant.parse(sExpire);

                    if (expire.isBefore(now)) {
                        logger.warn("Shared Environment " + runName + " has expired, deleting run");
                        this.frameworkRuns.delete(runName);                        
                    } else {
                        logger.trace("Shared Environment " + runName + " has not expired");
                    }
                } catch(Exception e) {
                    logger.error("Error checking shared environment " + runName,e);
                }
            }
        } catch (Throwable e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Expired Shared Environment search");
    }

}