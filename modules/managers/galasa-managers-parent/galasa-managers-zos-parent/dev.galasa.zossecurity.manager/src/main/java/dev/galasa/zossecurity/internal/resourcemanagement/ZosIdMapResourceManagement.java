/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_ID_MAP_PATTERN;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityImpl.ResourceType;
import dev.galasa.zossecurity.internal.resources.ZosIdMapImpl;

public class ZosIdMapResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());

    public ZosIdMapResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS ID Map resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS ID Map cleanup");
        try {
            // Find all the runs with zOS ID Maps
            Map<String, String> zosIdMaps = dss.getPrefix(ResourceType.ZOS_ID_MAP.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosIdMaps.keySet()) {
                Matcher matcher = ZOS_ID_MAP_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String userid = matcher.group(2);
                        String label = matcher.group(3);
                        String sysplexId = matcher.group(4);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS ID Map " + label + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosIdMapImpl zosIdMap = new ZosIdMapImpl(zosSecurity, userid, label, sysplexId, runName);
                        		zosIdMap.delete();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS ID Map " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS ID Map cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS ID Map cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_ID_MAP.getName() +  ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_ID_MAP_PATTERN.matcher(key);
                if (matcher.find()) {
                	String userid = matcher.group(2);
                    String label = matcher.group(3);
                    String sysplexId = matcher.group(4);

                    logger.info("Discarding zOS keyring " + label + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosIdMapImpl zosIdMap = new ZosIdMapImpl(zosSecurity, userid, label, sysplexId, runName);
                		zosIdMap.delete();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS ID Map " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS ID Map for run " + runName + " - " + e.getCause());
        }
    }
}
