/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.resourcemanagement;

import java.util.HashSet;
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
 * Clean up the ecosystem properties when the run is deleted
 * 
 *  
 *
 */
public class RunResourceMonitor implements Runnable {

	private final IFramework                 framework;
	private final IResourceManagement        resourceManagement;
	private final IDynamicStatusStoreService dss;
	private final Log                        logger = LogFactory.getLog(this.getClass());
	private final Pattern                    slotRunPattern = Pattern.compile("^run\\.(\\w+)\\..+$");

	public RunResourceMonitor(IFramework framework, 
			IResourceManagement resourceManagement,
			IDynamicStatusStoreService dss, 
			GalasaEcosystemResourceManagement galasaEcosystemResourceManagement,
			IConfigurationPropertyStoreService cps) {
		this.framework          = framework;
		this.resourceManagement = resourceManagement;
		this.dss = dss;
		this.logger.info("Galasa Ecosystem Run resource monitor initialised");
	}

	
	@Override
	public void run() {
		logger.info("Galasa Ecosystem Run search");
		try {
			//*** Find all the runs with properties
			Map<String, String> slotRuns = dss.getPrefix("run.");
			
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();
			
			HashSet<String> cleanedRuns = new HashSet<>();

			for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String runName = matcher.group(1);
					
					if (cleanedRuns.contains(runName)) {
					    continue; //*** just cleaned this run
					}

					if (!activeRunNames.contains(runName)) {
						logger.info("Cleaning Run " + runName + " properties as run has gone");
						
						cleanedRuns.add(runName);
						
						try {
						    dss.deletePrefix("run." + runName + ".");
						} catch(Exception e) {
							logger.error("Failed to clean run " + runName + " ecosystem properties",e);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Failure during run scan",e);
		}

		this.resourceManagement.resourceManagementRunSuccessful();
		logger.info("Galasa Ecosystem Run search");
	}

	public void runFinishedOrDeleted(String runName) {
		try {
			Map<String, String> slotRuns = dss.getPrefix("run." + runName + ".");
			if (!slotRuns.isEmpty()) {
                logger.info("Cleaning Run " + runName + " properties as run has gone");
                try {
                    dss.deletePrefix("run." + runName + ".");
                } catch(Exception e) {
                    logger.error("Failed to clean run " + runName + " ecosystem properties",e);
                }
			}
		} catch(Exception e) {
			logger.error("Failed to delete ecosystem for run " + runName);
		}
	}

}
