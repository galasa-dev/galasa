/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.StandAloneHttpClient;
import dev.galasa.selenium.SeleniumManagerException;

/**
 * Monitors any configured Selenium Grids for stale sessions.
 * 
 *  
 *
 */
public class SeleniumGridSessionMonitor implements Runnable {
	private final IFramework                            framework;
    private final IResourceManagement                   resourceManagement;
    private final IDynamicStatusStoreService            dss;
    private final IConfigurationPropertyStoreService    cps;
    private final Log                                   logger = LogFactory.getLog(SeleniumGridSessionMonitor.class);
                                                                    //dss.selenium.driver.slot.<SLOTNAME>.session
    private final Pattern                               slotRunPattern = Pattern.compile("driver\\.slot\\.(.*)(?=\\.session)");
	
    private String 										gridEndpoint;
    
	/**
	    * Selenium resource monitor
	    * @param framework
	    * @param resourceManagement
	    * @param dss
	    * @param cps
	    */
	    public SeleniumGridSessionMonitor(IFramework framework, IResourceManagement resourceManagement, 
	    		IDynamicStatusStoreService dss, IConfigurationPropertyStoreService cps) {
	        this.framework =            framework;
	        this.dss =                  dss;
	        this.cps =                  cps;
	        this.resourceManagement =   resourceManagement;

	        this.logger.info("Selenium Grid Session monitor intialised");
	    }

	@Override
	public void run() {
		logger.info("Starting search for stale sessions.");
		try {
			this.gridEndpoint = cps.getProperty("grid","endpoint");
			if (gridEndpoint == null) {
				logger.info("No Selenium grid defined, finishing.");
				return;
			}
		} catch (ConfigurationPropertyStoreException e) {
			logger.error("Failed to retrieve grid endpoint, ending.");
			return;
		}
		checkForStaleSessions();
        logger.info("Finished search for stale sessions..");
	}
	
	public void checkForStaleSessions() {
		try {
			Map<String, String> driverSlots = dss.getPrefix("driver.slot");
			Map<String,String> activeSeleniumSessions = new HashMap<>();
			
			IHttpClient client = StandAloneHttpClient.getHttpClient(3600, logger);
			client.setURI(new URI(gridEndpoint));
			HttpClientResponse<JsonObject> resp = client.getJson("/status");
			if (resp.getStatusCode() > 200) {
				logger.error("Failed to get grid status: " + resp.getStatusLine());
				return;
			}
			
			JsonObject value = resp.getContent();
			client.close();
			
			JsonArray nodes = value.getAsJsonObject().get("value").getAsJsonObject().get("nodes").getAsJsonArray();
			
			for (JsonElement n: nodes) {
				JsonObject node = n.getAsJsonObject();
				activeSeleniumSessions = retrieveActiveSessionIds(node);
			}
			
			for(String key: driverSlots.keySet()) {
				if (!key.endsWith(".session")) {
					// Session slot, not session key
					continue;
				}
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String slotName = matcher.group(1);
					if (driverSlots.keySet().contains("driver.slot." + slotName)) {
						logger.info("Slot found for this session, moving on");
						continue;
					}
					
					logger.info("Looking to discard the session " + driverSlots.get(key) + ". Slot " + slotName + " no longer exists");
					// Check to see if the selenium session still exists on the grid
					String sessionId = driverSlots.get(key);
					if (activeSeleniumSessions.keySet().contains(sessionId)) {
						client.setURI(new URI(activeSeleniumSessions.get(sessionId)));
						client.addCommonHeader("X-REGISTRATION-SECRET", "");
						
						HttpClientResponse<JsonObject> delResp = client.deleteJson("/se/grid/node/session/"+driverSlots.get(key));
						
						if(delResp.getStatusCode() > 200) {
							throw new SeleniumManagerException("Unable to delete session");
						}
						logger.info(sessionId + " has been removed from Grid");
					} else {
						logger.info("Selenium session has already expired on the the grid, no action required");
					}
					dss.performActions(new DssDelete(key,driverSlots.get(key)));	
					logger.info(slotName + " has been cleaned up");
				} 
			} 
		} catch(Exception e) {
			logger.error("Probelm running the selenium slot monitor", e);
		}
		logger.info("Stale slot search finished");

	}
	
	public Map<String,String> retrieveActiveSessionIds(JsonObject nodeJson) {
		Map<String,String> sessionIDs = new HashMap<>();
		
		JsonArray slots = nodeJson.getAsJsonObject().get("slots").getAsJsonArray();
		for (JsonElement s: slots) {
			JsonObject slot = s.getAsJsonObject();
			JsonElement session = slot.get("session");
			if (slot.get("session") != null) {
				sessionIDs.put(session.getAsJsonObject().get("sessionId").getAsString(),
						nodeJson.get("uri").getAsString());
			}
		}
		
		return sessionIDs;
	}
	
	public void removeGalasaStaleSessions(String uri, List<String> sessions) {
		
	}

}
