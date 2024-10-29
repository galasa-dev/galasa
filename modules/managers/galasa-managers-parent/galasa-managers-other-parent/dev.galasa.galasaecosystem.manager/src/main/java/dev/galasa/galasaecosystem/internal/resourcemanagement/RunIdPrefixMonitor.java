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
import dev.galasa.galasaecosystem.internal.RunIdPrefixImpl;

/**
 * Clean up the hanging runid prefixes 
 * 
 *  
 *
 */
public class RunIdPrefixMonitor implements Runnable {

	private final IFramework                 framework;
	private final IResourceManagement        resourceManagement;
	private final IDynamicStatusStoreService dss;
	private final Log                        logger = LogFactory.getLog(this.getClass());
	private final Pattern                    prefixPattern = Pattern.compile("^runid\\.prefix\\.(\\w+)$");

	public RunIdPrefixMonitor(IFramework framework, 
			IResourceManagement resourceManagement,
			IDynamicStatusStoreService dss, 
			GalasaEcosystemResourceManagement galasaEcosystemResourceManagement,
			IConfigurationPropertyStoreService cps) {
		this.framework          = framework;
		this.resourceManagement = resourceManagement;
		this.dss = dss;
		this.logger.info("Galasa Ecosystem run ID prefix resource monitor initialised");
	}

	
	@Override
	public void run() {
		logger.info("Galasa Ecosystem run ID prefix search");
		try {
			//*** run prefixes
			Map<String, String> prefixed = dss.getPrefix("runid.prefix.");
			
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();
			
			HashSet<String> cleanedPrefixes = new HashSet<>();

			for(String key : prefixed.keySet()) {
				Matcher matcher = prefixPattern.matcher(key);
				if (matcher.find()) {
					String prefixName = matcher.group(1);
					String runName = prefixed.get(key);
					
					if (cleanedPrefixes.contains(prefixName)) {
					    continue; //*** just cleaned this run
					}

					if (!activeRunNames.contains(runName)) {
						logger.info("Cleaning run ID prefix " + prefixName + " as run " + runName + " has gone");
						
						cleanedPrefixes.add(prefixName);
						
						try {
						    RunIdPrefixImpl.deleteFromDss(dss, runName, prefixName);
						} catch(Exception e) {
							logger.error("Failed to clean run ID prefix " + prefixName + " ecosystem properties",e);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Failure during run id prefix scan",e);
		}

		this.resourceManagement.resourceManagementRunSuccessful();
		logger.info("Galasa Ecosystem Run search");
	}

	public void runFinishedOrDeleted(String runName) {
	}

}
