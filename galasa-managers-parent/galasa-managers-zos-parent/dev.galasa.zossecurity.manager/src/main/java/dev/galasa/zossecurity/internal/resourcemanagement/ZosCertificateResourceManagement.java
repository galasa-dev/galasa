/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import static dev.galasa.zossecurity.internal.ZosSecurityImpl.ZOS_CERTIFICATE_PATTERN;

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
import dev.galasa.zossecurity.internal.resources.ZosCertificateImpl;

public class ZosCertificateResourceManagement implements Runnable {

	private final ZosSecurityImpl zosSecurity;
    private final IFramework framework;
    private final IResourceManagement resourceManagement;
    private final IDynamicStatusStoreService dss;
    private final Log logger = LogFactory.getLog(this.getClass());

    public ZosCertificateResourceManagement(ZosSecurityImpl zosSecurtityImpl, IFramework framework, IResourceManagement resourceManagement, IDynamicStatusStoreService dss) {
        this.zosSecurity = zosSecurtityImpl;
    	this.framework = framework;
        this.resourceManagement = resourceManagement;
        this.dss = dss;
        this.logger.info("zOS Certificate resource management initialised");
    }

	@Override
    public void run() {
        logger.info("Starting zOS Certificate cleanup");
        try {
            // Find all the runs with zOS Certificates
            Map<String, String> zosCertificates = dss.getPrefix(ResourceType.ZOS_CERTIFICATE.getName() + ".run.");

            Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

            for (String key : zosCertificates.keySet()) {
                Matcher matcher = ZOS_CERTIFICATE_PATTERN.matcher(key);
                if (matcher.find()) {
                    String runName = matcher.group(1);

                    if (!activeRunNames.contains(runName)) {
                        String type = matcher.group(2);
                        String userid = matcher.group(3);
                        String label = matcher.group(4);
                        String sysplexId = matcher.group(5);

                        if (!activeRunNames.contains(runName)) {
                        	logger.info("Discarding zOS certificate " + label + " for userid " + userid + " type " + type + " on sysplex " + sysplexId + " as run " + runName + " has gone");

                        	try {
                        		ZosCertificateImpl zosCertificate = new ZosCertificateImpl(zosSecurity, type, userid, label, sysplexId, runName);
                        		zosCertificate.delete();
                        	} catch (ZosSecurityManagerException e) {
                        		logger.error("Failed to discard zOS Certificate " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                        	}
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failure during zOS Certificate cleanup", e);
        }

        this.resourceManagement.resourceManagementRunSuccessful();
        logger.info("Finished zOS Certificate cleanup");
    }

    public void runFinishedOrDeleted(String runName) {
        try {
            Map<String, String> serverRuns = dss.getPrefix(ResourceType.ZOS_CERTIFICATE.getName() + ".run." + runName + ".");
            for (String key : serverRuns.keySet()) {
                Matcher matcher = ZOS_CERTIFICATE_PATTERN.matcher(key);
                if (matcher.find()) {
                    String type = matcher.group(2);
                    String userid = matcher.group(3);
                    String label = matcher.group(4);
                    String sysplexId = matcher.group(5);

                    logger.info("Discarding zOS certificate " + label + " for userid " + userid + " on sysplex " + sysplexId + " as run " + runName + " has gone");
                    
                    try {
                		ZosCertificateImpl zosCertificate = new ZosCertificateImpl(zosSecurity, type, userid, label, sysplexId, runName);
                		zosCertificate.delete();
                	} catch (ZosSecurityManagerException e) {
                		logger.error("Failed to discard zOS Certificate " + label + " for userid " + userid + " for run " + runName + " - " + e.getCause());
                	}
                }
            }
        } catch (Exception e) {
            logger.error("Failed to delete zOS Certificate for run " + runName + " - " + e.getCause());
        }
    }
}
