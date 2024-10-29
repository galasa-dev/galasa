/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssSwap;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;

/**
 * Looks for slots allocated to non active tests.
 * 
 *  
 *
 */
public class SeleniumSlotResourceMonitor implements Runnable {
	private final IFramework                            framework;
    private final IResourceManagement                   resourceManagement;
    private final IDynamicStatusStoreService            dss;
    private final IConfigurationPropertyStoreService    cps;
    private final Log                                   logger = LogFactory.getLog(SeleniumSlotResourceMonitor.class);
                                                                    //dss.selenium.driver.slot.<SLOTNAME>
    private final Pattern                               slotRunPattern = Pattern.compile("^driver\\.slot\\.(\\w+)");
	
	/**
	    * Selenium resource monitor
	    * @param framework
	    * @param resourceManagement
	    * @param dss
	    * @param cps
	    */
	    public SeleniumSlotResourceMonitor(IFramework framework, IResourceManagement resourceManagement, 
	    		IDynamicStatusStoreService dss, IConfigurationPropertyStoreService cps) {
	        this.framework =            framework;
	        this.dss =                  dss;
	        this.cps =                  cps;
	        this.resourceManagement =   resourceManagement;

	        this.logger.info("Selenium slot resource monitor intialised");
	    }

	@Override
	public void run() {
		logger.info("Starting search for run slots.");
        checkForStaleSlots();
        logger.info("Finished search for run slots.");
	}
	
	public void checkForStaleSlots() {
		try {
			Map<String, String> driverSlots = dss.getPrefix("driver.slot");
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();
			
			for(String key: driverSlots.keySet()) {
				if (key.endsWith(".session")) {
					// Session key, not slot key
					continue;
				}
				
				String runName = driverSlots.get(key);
				if (activeRunNames.contains(runName)) {
					logger.info("Run still active, continuing");
					continue;
				}
				
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String slotName = matcher.group(0);
					logger.info("Stale slot found: " + slotName);
					
					try {
						logger.info("Discarding slot " + slotName + " as the run " + runName + " no longer exists");
						int currentSlots = Integer.valueOf(dss.get("driver.current.slots"));
						dss.performActions(	new DssDelete(key, runName),
											new DssSwap("driver.current.slots", String.valueOf(currentSlots), String.valueOf(currentSlots-1)));
					} catch (DynamicStatusStoreException e) {
						logger.error("Failed to clean slot:" + slotName);
					}
					
				}
			} 
		} catch(Exception e) {
			logger.error("Probelm running the selenium slot monitor");
		}
		logger.info("Stale slot search finished");

	}

}
