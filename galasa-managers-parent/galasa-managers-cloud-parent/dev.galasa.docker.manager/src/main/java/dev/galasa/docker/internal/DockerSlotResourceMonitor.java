/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

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

/**
 * Docker resource monitor
 * 
 *   
 */
public class DockerSlotResourceMonitor implements Runnable {
    private final IFramework                            framework;
    private final IResourceManagement                   resourceManagement;
    private final IDynamicStatusStoreService            dss;
    private final IConfigurationPropertyStoreService    cps;
    private final Log                                   logger = LogFactory.getLog(DockerSlotResourceMonitor.class);
                                                                    //dss.docker.slot.default.run.L7.SLOT_L7_0=free
    private final Pattern                               slotRunPattern = Pattern.compile("^slot\\.(\\w+)\\.run\\.(\\w+)\\.(\\w+)");


   /**
    * Docker resource monitor

    * @param framework
    * @param resourceManagement
    * @param dss
    * @param dockerResourceManagement
    * @param cps
    */
    public DockerSlotResourceMonitor(IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss, 
            DockerResourceManagement dockerResourceManagement, IConfigurationPropertyStoreService cps) {
        this.framework =            framework;
        this.dss =                  dss;
        this.cps =                  cps;
        this.resourceManagement =   resourceManagement;

        this.logger.info("Docker slot resource monitor intialised");
    }

    /**
     * Runs the docker resource monitor.
     */
    @Override
    public void run() {
        logger.info("Starting search for run slots.");
        checkForStaleSlots();
        logger.info("Finished search for run slots.");
    }

    public void checkForStaleSlots() {
        try {
            Map<String, String> slotRuns = dss.getPrefix("slot");
			
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

			for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String runName = matcher.group(2);

					if (!activeRunNames.contains(runName)) {
						String dockerEngine = matcher.group(1);
						String slot    = matcher.group(3);

						logger.info("Discarding slot " + slot + " on Docker engine " + dockerEngine + " as run " + runName + " has gone");

						try {
							DockerEnvironment.deleteStaleDssSlot(runName, dockerEngine, slot, dss);
						} catch(Exception e) {
							logger.error("Failed to discard slot " + slot + " on image " + dockerEngine + " as run " + runName);
						}
					}
				}
			}
        } catch (Exception e) {
            logger.error("Problem when trying run the Docker resource monitor.", e);
        }
        logger.info("Stale slot search finished");
    }

    /**
     * 
     * @param runName
     */
    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String,String> slotRuns = dss.getPrefix("slot.run." + runName + ".");
            for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String dockerEngine = matcher.group(2);
					String slot    = matcher.group(3);

					logger.info("Discarding slot " + slot + " on image " + dockerEngine + " as run " + runName + " has gone");

					try {
						DockerEnvironment.deleteStaleDssSlot(runName, dockerEngine, slot, dss);
					} catch(Exception e) {
						logger.error("Failed to discard slot " + slot + " on image " + dockerEngine + " as run " + runName);
					}
				}
			}
        } catch (Exception e) {
            logger.error("Failed to delete stale dss properties for runtName: " + runName, e);
        }
    }
    
}