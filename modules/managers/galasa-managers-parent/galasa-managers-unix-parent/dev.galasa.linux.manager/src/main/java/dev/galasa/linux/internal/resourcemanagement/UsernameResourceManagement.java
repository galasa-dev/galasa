/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.resourcemanagement;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.ipnetwork.spi.SSHClient;
import dev.galasa.linux.internal.properties.LinuxPropertiesSingleton;
import dev.galasa.linux.internal.shared.LinuxSharedImage;
import dev.galasa.linux.internal.shared.LinuxSharedIpHost;

public class UsernameResourceManagement implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger        = LogFactory.getLog(this.getClass());

    private final Pattern                    serverPattern = Pattern.compile("^run\\.(\\w+)\\.image\\.(\\w+)\\.username.(\\w+)$");

    public UsernameResourceManagement(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("Linux username resource management initialised");
    }

    @Override
    public void run() {
        logger.info("Starting Linux username search");
        try {
            // *** Find all the runs with usernames
            Map<String, String> linuxRuns = dss.getPrefix("run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : linuxRuns.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);
                    String imageName = matcher.group(2);
                    String username = matcher.group(3);

                    if (!activeRunNames.contains(runName)) {
                        logger.info("Discarding Linux username " + imageName + "/" + username + " as run " + runName + " has gone");

                        SSHClient commandShell = null;
                        
                        try {
                            LinuxSharedIpHost ipHost = new LinuxSharedIpHost(this.framework, LinuxPropertiesSingleton.cps(), this.dss, imageName);
                            commandShell = new SSHClient(ipHost.getHostname(), ipHost.getSshPort(), ipHost.getDefaultCredentials(), 60000);
                            
                            LinuxSharedImage.discardDssUsername(this.dss, commandShell, imageName, username, runName);
                        } catch (Exception e) {
                            logger.error("Failed to discard Linux username " + imageName + "/" + username + " for run " + runName);
                        } finally {
                            if (commandShell != null) {
                                commandShell.disconnect();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during Linux username scan", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Linux username search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix("run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String imageName = matcher.group(2);
                    String username = matcher.group(3);

                    logger.info("Discarding Linux username " + imageName + "/" + username + " as run " + runName + " has gone");

                    SSHClient commandShell = null;
                    
                    try {
                        LinuxSharedIpHost ipHost = new LinuxSharedIpHost(this.framework, LinuxPropertiesSingleton.cps(), this.dss, imageName);
                        commandShell = new SSHClient(ipHost.getHostname(), ipHost.getSshPort(), ipHost.getDefaultCredentials(), 60000);
                        
                        LinuxSharedImage.discardDssUsername(this.dss, commandShell, imageName, username, runName);
                    } catch (Exception e) {
                        logger.error("Failed to discard Linux username " + imageName + "/" + username + " for run " + runName);
                    } finally {
                        if (commandShell != null) {
                            commandShell.disconnect();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete Linux username for run " + runName);
        }
    }

}
