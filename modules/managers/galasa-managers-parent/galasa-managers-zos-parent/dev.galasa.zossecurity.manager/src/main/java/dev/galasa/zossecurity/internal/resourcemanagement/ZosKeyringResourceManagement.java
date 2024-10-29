/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_KEYRING_PATTERN;

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
import dev.galasa.zossecurity.internal.resources.ZosKeyringImpl;

public class ZosKeyringResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());

    public ZosKeyringResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Keyring resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS Keyring cleanup");
        try {
            // Find all the runs with zOS Keyrings
            Map<String, String> zosKeyrings = dss.getPrefix(ResourceType.ZOS_KEYRING.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosKeyrings.keySet()) {
                Matcher matcher = ZOS_KEYRING_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String userid = matcher.group(2);
                        String label = matcher.group(3);
                        String sysplexId = matcher.group(4);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS Keyring " + label + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosKeyringImpl zosKeyring = new ZosKeyringImpl(zosSecurity, userid, label, sysplexId, runName);
                        		zosKeyring.delete();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS Keyring " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS Keyring cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS Keyring cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_KEYRING.getName() + ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_KEYRING_PATTERN.matcher(key);
                if (matcher.find()) {
                	String userid = matcher.group(2);
                    String label = matcher.group(3);
                    String sysplexId = matcher.group(4);

                    logger.info("Discarding zOS keyring " + label + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosKeyringImpl zosKeyring = new ZosKeyringImpl(zosSecurity, userid, label, sysplexId, runName);
                		zosKeyring.delete();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS Keyring " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS Keyring for run " + runName + " - " + e.getCause());
        }
    }
}
