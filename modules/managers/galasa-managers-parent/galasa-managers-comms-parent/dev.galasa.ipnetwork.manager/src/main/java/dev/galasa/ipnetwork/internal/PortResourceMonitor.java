/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.internal;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;

public class PortResourceMonitor implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger         = LogFactory.getLog(this.getClass());
    private final Pattern                    portRunPattern = Pattern
            .compile("^port\\.run\\.(\\w+)\\.host\\.(\\w+)\\.port\\.(\\d+)$");

    protected PortResourceMonitor(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, IpNetworkResourceManagement ipNetworkResourceManagement,
            IConfigurationPropertyStoreService cps) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("IP Network Port resource monitor initialised");
    }

    @Override
    public void run() {
        logger.info("Starting Run Port search");
        try {
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            // *** Find all the runs with ports
            Map<String, String> portRuns = dss.getPrefix("port.run.");
            for (String key : portRuns.keySet()) {
                Matcher matcher = portRunPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String hostId = matcher.group(2);
                        String port = matcher.group(3);

                        logger.info(
                                "Discarding port " + port + " on host " + hostId + " as run " + runName + " has gone");

                        try {
                            IpPortImpl.deleteDss(runName, hostId, port, dss);
                        } catch (Exception e) {
                            logger.error(
                                    "Failed to discard port " + port + " on host " + hostId + " as run " + runName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during port active runs scan", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Port search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> portRuns = dss.getPrefix("port.run." + runName + ".");
            for (String key : portRuns.keySet()) {
                Matcher matcher = portRunPattern.matcher(key);
                if (matcher.find()) {
                    String hostId = matcher.group(2);
                    String port = matcher.group(3);

                    logger.info("Discarding port " + port + " on host " + hostId + " as run " + runName + " has gone");

                    IpPortImpl.deleteDss(runName, hostId, port, dss);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete ports for run " + runName);
        }
    }
}
