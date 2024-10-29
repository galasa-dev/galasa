/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;

public class ServerResourceMonitor implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final OpenstackHttpClient        openstackHttpClient;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger        = LogFactory.getLog(this.getClass());

    private final Pattern                    serverPattern = Pattern.compile("^run\\.(\\w+)\\.compute\\.(\\w+)$");

    public ServerResourceMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, OpenstackHttpClient openstackHttpClient) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.openstackHttpClient = openstackHttpClient;
        this.logger.info("OpenStack Server resource monitor initialised");
    }

    @Override
    public void run() {
        logger.info("Starting OpenStack Server search");
        try {
            // *** Find all the runs with slots
            Map<String, String> computeServers = dss.getPrefix("run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : computeServers.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String serverName = matcher.group(2);
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        logger.info("Discarding OpenStack server " + serverName + " as run " + runName + " has gone");

                        try {
                            OpenstackServerImpl.deleteServerByName(serverName, runName, dss, this.openstackHttpClient);
                        } catch (Exception e) {
                            logger.error("Failed to discard OpenStack server " + serverName + " for run " + runName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during OpenStack server scan", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished OpenStack Server search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix("run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String serverName = matcher.group(2);

                    logger.info("Discarding OpenStack server " + serverName + " as run " + runName + " has gone");

                    try {
                        OpenstackServerImpl.deleteServerByName(serverName, runName, dss, this.openstackHttpClient);
                    } catch (Exception e) {
                        logger.error("Failed to discard OpenStack server " + serverName + " for run " + runName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete OpenStack Compute Server for run " + runName);
        }
    }

}
