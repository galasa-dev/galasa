/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides specific implementation to monitor the number of running SDV managers.
 *
 */
public class SdvManagersResourceMonitor implements Runnable {

    private static final Log LOG = LogFactory.getLog(SdvManagersResourceMonitor.class);

    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;

    private static final String PERIOD_REGEX = "\\.";

    /**
     * SdvManagersResourceMonitor constructor.
     *
     * @param framework - Galasa framework.
     * @param resourceManagement - Galasa Resource Management.
     * @param dss - Galasa DSS.
     */
    public SdvManagersResourceMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        LOG.info("SDV Managers resource monitor initialised");
    }

    @Override
    public void run() {
        LOG.info("Monitoring for SDV managers held up in active status");

        try {
            // Get the list of SDV manager props in the DSS
            Map<String, String> sdvManagersInDss = dss.getPrefix("manager.runningManagers");

            Set<String> allActiveRuns = this.framework.getFrameworkRuns().getActiveRunNames();

            // Iterate through all SDV managers stored in the DSS
            for (Map.Entry<String, String> entry : sdvManagersInDss.entrySet()) {
                // Split list of managers into array
                Set<String> initialRunList =
                        new HashSet<String>(Arrays.asList(entry.getValue().split(",")));

                for (String runName : initialRunList) {
                    if (!allActiveRuns.contains(runName)) {
                        String removeRun = runName;
                        String removeCicsApplId = entry.getKey().split(PERIOD_REGEX)[2];

                        deleteDss(removeCicsApplId, removeRun);
                    }
                }
            }
        } catch (FrameworkException e) {
            LOG.error("Failure during scanning DSS for SDV Managers");
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        LOG.info("Finished cleaning up SDV Manager activity");
    }

    /**
     * Called when a Galasa test run ends or is removed.
     *
     * @param runName - Galasa test run name.
     */
    public void runFinishedOrDeleted(String runName) {

        try {
            // Get list of SDV Managers listed as active in the DSS
            Map<String, String> sdvManagersInDss = dss.getPrefix("manager.runningManagers");

            for (Map.Entry<String, String> entry : sdvManagersInDss.entrySet()) {
                // Split list of managers into array
                Set<String> initialRunList =
                    new HashSet<String>(Arrays.asList(entry.getValue().split(",")));

                // This SDV Manager belongs to the run
                if (initialRunList.contains(runName)) {
                    String removeCicsApplId = entry.getKey().split(PERIOD_REGEX)[2];
                    deleteDss(removeCicsApplId, runName);
                }
            }
        } catch (FrameworkException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failure cleaning up SDV Managers for finished run " + runName);
            }
        }
    }

    private void deleteDss(String cicsApplid, String run)
            throws DynamicStatusStoreMatchException, DynamicStatusStoreException {

        Set<String> currentRunList =
            new HashSet<String>(Arrays.asList(
                dss.get("manager.runningManagers." + cicsApplid).split(",")
            ));

        if (currentRunList.size() == 1) {
            // This is the last manager against the region
            // and is stale, so delete all manager entries for
            // this region
            if (LOG.isInfoEnabled()) {
                LOG.info("Removing SDV Manager props for CICS region " + cicsApplid
                        + ", assigned to inactive run " + run);
            }

            dss.performActions(
                new DssDelete("manager.runningManagers." + cicsApplid, null),
                new DssDelete("manager." + cicsApplid + ".sdcLive", null)
            );

        } else {
            // There are other managers, which may be live, or not.
            // leave it to the last manager standing to tidy up.
            // Just remove this run from the list
            if (LOG.isInfoEnabled()) {
                LOG.info("Removing SDV Manager from list on CICS region " + cicsApplid
                        + ", assigned to inactive run " + run);
            }

            currentRunList.remove(run);

            dss.put("manager.runningManagers." + cicsApplid, String.join(",", currentRunList));
        }

    }
}
