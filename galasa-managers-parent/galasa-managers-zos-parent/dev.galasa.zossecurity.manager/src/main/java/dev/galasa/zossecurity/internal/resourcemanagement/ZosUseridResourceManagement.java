/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_USERID_PATTERN;

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
import dev.galasa.zossecurity.internal.resources.ZosUseridImpl;

public class ZosUseridResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());

    public ZosUseridResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Userid resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS Userid cleanup");
        try {
            // Find all the runs with zOS Userids
            Map<String, String> zosUserids = dss.getPrefix(ResourceType.ZOS_USERID.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosUserids.keySet()) {
                Matcher matcher = ZOS_USERID_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String userid = matcher.group(2);
                        String sysplexId = matcher.group(3);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS Userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosUseridImpl zosUserid = new ZosUseridImpl(zosSecurity, userid, sysplexId, runName);
                        		zosUserid.delete();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS Userid " + userid + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS Userid cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS Userid cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_USERID.getName() + ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_USERID_PATTERN.matcher(key);
                if (matcher.find()) {
                	String userid = matcher.group(2);
                    String sysplexId = matcher.group(3);

                    logger.info("Discarding zOS Userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosUseridImpl zosUserid = new ZosUseridImpl(zosSecurity, userid, sysplexId, runName);
                		zosUserid.delete();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS Userid " + userid + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS Userid for run " + runName + " - " + e.getCause());
        }
    }
}
