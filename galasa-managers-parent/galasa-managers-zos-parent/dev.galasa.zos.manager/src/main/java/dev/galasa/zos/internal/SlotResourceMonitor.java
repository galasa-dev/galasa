/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

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

public class SlotResourceMonitor implements Runnable {

    private final IFramework                 framework;
    private final IResourceManagement        resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log                        logger = LogFactory.getLog(this.getClass());
    private final Pattern                    slotRunPattern = Pattern.compile("^slot\\.run\\.(\\w+)\\.image\\.(\\w+)\\.slot\\.(\\w+)$");

    public SlotResourceMonitor(IFramework framework, 
            IResourceManagement resourceManagement,
            IDynamicStatusStoreService dss, 
            ZosResourceManagement zosResourceManagement,
            IConfigurationPropertyStoreService cps) {
        this.framework          = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Slot resource monitor initialised");
    }

    
    @Override
    public void run() {
        logger.info("Starting Run Slot search");
        try {
            //*** Find all the runs with slots
            Map<String, String> slotRuns = dss.getPrefix("slot.run.");
            
            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for(String key : slotRuns.keySet()) {
                Matcher matcher = slotRunPattern.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String imageId = matcher.group(2);
                        String slot    = matcher.group(3);

                        logger.info("Discarding slot " + slot + " on image " + imageId + " as run " + runName + " has gone");

                        try {
                            ZosProvisionedImageImpl.deleteDss(runName, imageId, slot, dss);
                        } catch(Exception e) {
                            logger.error("Failed to discard slot " + slot + " on image " + imageId + " as run " + runName);
                        }
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failure during slot scan",e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished Run Slot search");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> slotRuns = dss.getPrefix("slot.run." + runName + ".");
            for(String key : slotRuns.keySet()) {
                Matcher matcher = slotRunPattern.matcher(key);
                if (matcher.find()) {
                    String imageId = matcher.group(2);
                    String slot    = matcher.group(3);

                    logger.info("Discarding slot " + slot + " on image " + imageId + " as run " + runName + " has gone");

                    try {
                        ZosProvisionedImageImpl.deleteDss(runName, imageId, slot, dss);
                    } catch(Exception e) {
                        logger.error("Failed to discard slot " + slot + " on image " + imageId + " as run " + runName);
                    }
                }
            }
        } catch(Exception e) {
            logger.error("Failed to delete slots for run " + runName);
        }
    }

}
