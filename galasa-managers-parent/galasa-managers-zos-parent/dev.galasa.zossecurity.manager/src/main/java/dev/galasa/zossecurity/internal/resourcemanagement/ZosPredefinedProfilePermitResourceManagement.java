/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_PRE_DEFINED_PROFILE_PERMIT_PATTERN;

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
import dev.galasa.zossecurity.internal.resources.ZosPredefinedProfilePermitImpl;

public class ZosPredefinedProfilePermitResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());

    public ZosPredefinedProfilePermitResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Predefined Profile Permit resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS Predefined Profile Permit cleanup");
        try {
            // Find all the runs with zOS Predefined Profile Permits
            Map<String, String> zosPredefinedProfilePermits = dss.getPrefix(ResourceType.ZOS_PRE_DEFINED_PROFILE_PERMIT.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosPredefinedProfilePermits.keySet()) {
                Matcher matcher = ZOS_PRE_DEFINED_PROFILE_PERMIT_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String classname = matcher.group(2);
                        String profile = matcher.group(3);
                        String userid = matcher.group(4);
                        String sysplexId = matcher.group(5);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS Predefined Profile Permit " + classname + "/" + profile + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosPredefinedProfilePermitImpl zosPredefinedProfilePermit = new ZosPredefinedProfilePermitImpl(zosSecurity, classname, profile, userid, sysplexId, runName);
                        		zosPredefinedProfilePermit.discard();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS Predefined Profile Permit " + classname + "/" + profile + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS Predefined Profile Permit cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS Predefined Profile Permit cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_PRE_DEFINED_PROFILE_PERMIT.getName() + ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_PRE_DEFINED_PROFILE_PERMIT_PATTERN.matcher(key);
                if (matcher.find()) {
                    String classname = matcher.group(2);
                    String profile = matcher.group(3);
                    String userid = matcher.group(4);
                    String sysplexId = matcher.group(5);

                    logger.info("Discarding zOS Predefined Profile Permit " + classname + "/" + profile + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosPredefinedProfilePermitImpl zosPreDefinedProfilePermit = new ZosPredefinedProfilePermitImpl(zosSecurity, classname, profile, userid, sysplexId, runName);
                		zosPreDefinedProfilePermit.discard();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS Predefined Profile Permit " + classname + "/" + profile + " for userid " + userid + " on sysplex " + sysplexId + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS Predefined Profile Permit for run " + runName + " - " + e.getCause());
        }
    }
}
