/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_PROFILE_PATTERN;

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
import dev.galasa.zossecurity.internal.resources.ZosProfileImpl;

public class ZosProfileResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());
    
    public ZosProfileResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Profile resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS Profile cleanup");
        try {
            // Find all the runs with zOS Profiles
            Map<String, String> zosProfiles = dss.getPrefix(ResourceType.ZOS_PROFILE.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosProfiles.keySet()) {
                Matcher matcher = ZOS_PROFILE_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String className = matcher.group(2);
                        String profileName = matcher.group(3);
                        String sysplexId = matcher.group(4);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS Profile " + profileName + " class " + className + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosProfileImpl zosProfile = new ZosProfileImpl(zosSecurity, className, profileName, sysplexId, runName);
                        		zosProfile.delete();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS Profile " + profileName + " class " + className + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS Profile cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS Profile cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_PROFILE.getName() + ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_PROFILE_PATTERN.matcher(key);
                if (matcher.find()) {
                	String className = matcher.group(2);
                    String profileName = matcher.group(3);
                    String sysplexId = matcher.group(4);
                    
                    logger.info("Discarding zOS profile " + profileName + " class " + className + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosProfileImpl zosProfile = new ZosProfileImpl(zosSecurity, className, profileName, sysplexId, runName);
                		zosProfile.delete();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS Profile " + profileName + " class " + className + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS Profile for run " + runName + " - " + e.getCause());
        }
    }
}
