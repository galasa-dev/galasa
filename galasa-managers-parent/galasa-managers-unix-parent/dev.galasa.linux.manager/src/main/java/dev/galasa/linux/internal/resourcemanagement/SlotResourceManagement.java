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

import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.linux.internal.shared.LinuxSharedImage;

public class SlotResourceManagement implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger        = LogFactory.getLog(this.getClass());

    private final Pattern                    serverPattern = Pattern.compile("^run\\.(\\w+)\\.image\\.(\\w+)$");

    public SlotResourceManagement(IFramework framework, IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("Linux slot resource management initialised");
    }

    @Override
    public void run() {
        logger.info("Starting Linux slot search");
        try {
            // *** Find all the runs with slots
            Map<String, String> linuxRuns = dss.getPrefix("run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : linuxRuns.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);
                    String imageName = matcher.group(2);

                    if (!activeRunNames.contains(runName)) {
                        logger.info("Discarding Linux slot " + imageName + " as run " + runName + " has gone");

                        try {
                            LinuxSharedImage.discardDssSlot(this.dss, imageName, runName);
                        } catch (DynamicStatusStoreMatchException e) {
                            logger.info("Slots changes whilst cleaning, will try again later");
                        } catch (Exception e) {
                            logger.error("Failed to discard Linux slot " + imageName + " for run " + runName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during Linux slot scan", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Linux slot search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix("run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = serverPattern.matcher(key);
                if (matcher.find()) {
                    String imageName = matcher.group(2);

                    logger.info("Discarding Linux slot " + imageName + " as run " + runName + " has gone");

                    try {
                        LinuxSharedImage.discardDssSlot(this.dss, imageName, runName);
                    } catch (DynamicStatusStoreMatchException e) {
                        logger.info("Slots changes whilst cleaning, will try again later");
                    } catch (Exception e) {
                        logger.error("Failed to discard Linux slot " + imageName + " for run " + runName);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete Linux slot for run " + runName);
        }
    }

}
