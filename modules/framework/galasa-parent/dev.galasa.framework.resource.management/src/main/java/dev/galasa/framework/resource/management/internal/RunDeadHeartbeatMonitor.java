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
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;

public class RunDeadHeartbeatMonitor implements Runnable {

    private final IResourceManagement                resourceManagement;
    private final IConfigurationPropertyStoreService cps;
    private final IFrameworkRuns                     frameworkRuns;
    private final Log                                logger ;
    private final ITimeService                       timeService ;

    private final DateTimeFormatter                  dtf    = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
            .withZone(ZoneId.systemDefault());

    private Map<String,RunWithNoHeartbeatRecord> runsWithNoHeartbeatCache = new HashMap<String,RunWithNoHeartbeatRecord>();


    protected RunDeadHeartbeatMonitor(
        IFramework framework, IResourceManagement resourceManagement,
        IDynamicStatusStoreService dss, IResourceManagementProvider runResourceManagement,
        IConfigurationPropertyStoreService cps) throws FrameworkException {

        this(framework, resourceManagement, dss, 
            runResourceManagement, cps, 
            LogFactory.getLog(RunDeadHeartbeatMonitor.class),
            new SystemTimeService()
        );
    }

    protected RunDeadHeartbeatMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, IResourceManagementProvider runResourceManagement,
            IConfigurationPropertyStoreService cps, Log logger, ITimeService timeService) throws FrameworkException {
        this.resourceManagement = resourceManagement;
        this.frameworkRuns = framework.getFrameworkRuns();
        this.cps = cps;
        this.logger = logger;
        this.logger.info("Run Dead Heartbeat Monitor initialised");
        this.timeService = timeService ;
    }

    @Override
    public void run() {

        int maxDeadHeartbeatTimeSecs = getMaxDeadHearbeatTimeSecs();

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

                Instant now = timeService.now();

                Instant heartbeat = run.getHeartbeat();
                if (heartbeat == null) {
                    processRunWithNoHeartbeat(runName, now, maxDeadHeartbeatTimeSecs);
                } else {
                    processRunWithHeartbeat(heartbeat, now, maxDeadHeartbeatTimeSecs, run, runName);
                }
            }

            evictAncientNoHeartbeatCacheItems(timeService.now(), maxDeadHeartbeatTimeSecs*2);

        } catch (Throwable e) {
            logger.error("Scan of runs failed", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Dead Heartbeat search");
    }


    /**
     * Look through all the items we have in the no-heartbeat cache.
     * Items which have been in there, but not used for a while can be removed.
     * The test either cleaned itself up, or added a heartbeat, so we need not
     * be concerned about further cleanup.
     * @param now The time.
     */
    private void evictAncientNoHeartbeatCacheItems(Instant now, int maxUncheckedSecs) {

        List<String> cacheItemsToRemove = new ArrayList<String>();
        for( Map.Entry<String,RunWithNoHeartbeatRecord> cacheItem : this.runsWithNoHeartbeatCache.entrySet() ) {
            Instant expires = cacheItem.getValue().getLastCheckedTime().plusSeconds(maxUncheckedSecs);
            if ( expires.compareTo(now) <= 0) {
                // The entry in the cache has not been used for a while...
                // Mark it for removal.
                cacheItemsToRemove.add( cacheItem.getKey());
            }
        }

        // We have a list of items to remove from the cache now.
        for( String runNameToRemove : cacheItemsToRemove ) {
            logger.info("Test run "+runNameToRemove+" used to exist in the DSS with no heartbeat, but has gone. Forgetting that now.");
            this.runsWithNoHeartbeatCache.remove(runNameToRemove);
        }
    }

    /**
     * Calculate the max deadHeartbeatTimeSecs. This indicates the max number of seconds a test run
     * should be in the DSS without updating it's DSS entry.
     * After this time, the run will be deemed to have stalled/stopped/hung.
     * 
     * Default is 5 minutes. 
     * Overridden by the framework.resource.management.dead.heartbeat.timeout CPS property
     * (which is in units of a second).
     * 
     * @return
     */
    private int getMaxDeadHearbeatTimeSecs() { 
        int defaultDeadHeartbeatTimeSecs = 300; // ** 5 minutes
        try { // TODO do we need a different timeout for automation run reset?
            String overrideTime = AbstractManager
                    .nulled(cps.getProperty("resource.management", "dead.heartbeat.timeout"));
            if (overrideTime != null) {
                defaultDeadHeartbeatTimeSecs = Integer.parseInt(overrideTime);
            }
        } catch (Throwable e) {
            logger.error("Problem with resource.management.dead.heartbeat.timeout, using default "
                    + defaultDeadHeartbeatTimeSecs, e);
        }
        return defaultDeadHeartbeatTimeSecs;
    }

    private void processRunWithHeartbeat(
        Instant heartbeat , Instant now, int defaultDeadHeartbeatTimeSecs, IRun run, String runName) 
        throws DynamicStatusStoreException {

        Instant expires = heartbeat.plusSeconds(defaultDeadHeartbeatTimeSecs);
                
        if (expires.compareTo(now) <= 0) {
            logger.trace("Run " + runName + " has a dead heartbeat");
            String lastHeartbeat = dtf.format(LocalDateTime.ofInstant(heartbeat, ZoneId.systemDefault()));
            if (run.isLocal()) {
                /// TODO put time management into the framework
                logger.warn("Deleting run " + runName + ", last heartbeat was at " + lastHeartbeat);
                this.frameworkRuns.markRunInterrupted(runName, Result.HUNG);
            } else {
                logger.warn("Resetting run " + runName + ", last heartbeat was at " + lastHeartbeat);
                this.frameworkRuns.markRunInterrupted(runName, Result.REQUEUED);
            }
        } else {
            logger.trace("Run " + runName + " heartbeat is ok");
        }
    }

    private void processRunWithNoHeartbeat( 
        String runName, Instant now, int defaultDeadHeartbeatTimeSecs) 
        throws DynamicStatusStoreException {

        RunWithNoHeartbeatRecord noHeartBeatRun = runsWithNoHeartbeatCache.get(runName);
        if (noHeartBeatRun==null) {
            // We've never noticed that this run has no heartbeat.
            // Remember the fact, and the time we noticed it for later.
            // If it hangs around for too long we will remove it.
            noHeartBeatRun = new RunWithNoHeartbeatRecord(runName, now);
            runsWithNoHeartbeatCache.put(runName, noHeartBeatRun);
            logger.warn("Active run without heartbeat = " + runName + " discovered. ignored for now.");
        } else {
            // We have already noticed that this run has no heartbeat.
            // Was it a long time ago ?
            Instant expires = noHeartBeatRun.getFirstDetectedTime().plusSeconds(defaultDeadHeartbeatTimeSecs);
            if (expires.compareTo(now) <= 0) {
                // The run has no heartbeat and has been around for a long while.
                // So interrupt it.
                logger.warn("Active run without heartbeat = " + runName + " still present. It's old. Interrupting now.");
                this.frameworkRuns.markRunInterrupted(runName, Result.HUNG);
                // No longer need to have any knowledge of this run in the cache either.
                runsWithNoHeartbeatCache.remove(runName);
            } else {
                // The run has no heartbeat but we only noticed a short time ago.
                // Leave it for now to either get a heartbeat, or stay with no 
                // hearbeat for too long, when we will clean it up.
                logger.warn("Active run without heartbeat = " + runName + " still present. ignored for now.");

                // Don't allow this entry to be removed from the cache.
                noHeartBeatRun.setLastCheckedTime(now);
            }
        }
    }

}