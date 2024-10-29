/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides specific implementation to monitor SdvUser usage.
 *
 */
public class SdvUserResourceMonitor implements Runnable {

    private static final Log LOG = LogFactory.getLog(SdvUserResourceMonitor.class);

    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;

    private static final String PERIOD_REGEX = "\\.";

    /**
     * SdvUserResourceMonitor constructor.
     *
     * @param framework - Galasa framework.
     * @param resourceManagement - Galasa Resource Management.
     * @param dss - Galasa DSS.
     */
    public SdvUserResourceMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        LOG.info("SDV User provisioning resource monitor initialised");
    }

    @Override
    public void run() {
        LOG.info("Monitoring for SDV Users held up in allocated status");

        try {

            // Get the list of SDV Users allocated in the DSS
            Map<String, String> sdvUsersInDss = dss.getPrefix("sdvuser");

            Set<String> allActiveRuns = this.framework.getFrameworkRuns().getActiveRunNames();

            // Iterate through all SDV Users stored in the DSS
            for (Map.Entry<String, String> entry : sdvUsersInDss.entrySet()) {
                // Delete DSS SDV User allocation to run not active
                if (!allActiveRuns.contains(entry.getValue())) {
                    String removeRun = entry.getValue();
                    String removeCicsApplId = entry.getKey().split(PERIOD_REGEX)[1];
                    String removeUser = entry.getKey().split(PERIOD_REGEX)[2];

                    if (LOG.isInfoEnabled()) {
                        LOG.info("Freeing allocated user " + removeUser
                                + " assigned to inactive run " + removeRun);
                    }

                    deleteDss(removeUser, removeCicsApplId, removeRun);
                }
            }
        } catch (Exception e) {
            LOG.error("Failure during scanning DSS for SDV Users");
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        LOG.info("Finished cleaning up SDV User allocation");
    }

    /**
     * Called when a Galasa test run ends or is removed.
     *
     * @param runName - Galasa test run name.
     */
    public void runFinishedOrDeleted(String runName) {

        try {
            // Get list of SDV Users allocated in the DSS
            Map<String, String> sdvUsersInDss = dss.getPrefix("sdvuser");

            for (Map.Entry<String, String> entry : sdvUsersInDss.entrySet()) {

                // This SDV User allocation belongs to the run
                if (entry.getValue().equals(runName)) {
                    deleteDss(entry.getKey().split(PERIOD_REGEX)[2],
                            entry.getKey().split(PERIOD_REGEX)[1], entry.getValue());
                }
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failure cleaning up SDV Users for finished run " + runName);
            }
        }
    }

    private void deleteDss(String user, String cicsApplid, String run) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Freeing user " + user + " on CICS Applid " + cicsApplid + " allocated to run "
                    + run + " which has finished");
        }

        try {
            SdvUserPool.deleteDss(user, cicsApplid, run, dss);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failure in discarding SDV User " + user + " on CICS Applid " + cicsApplid
                        + " allocated to run " + run);
            }
        }
    }
}
