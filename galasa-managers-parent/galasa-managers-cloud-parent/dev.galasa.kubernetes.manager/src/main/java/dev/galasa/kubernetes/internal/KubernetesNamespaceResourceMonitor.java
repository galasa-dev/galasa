/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal;

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
 * Clean up namespaces when a run is finished with it.
 * 
 *  
 *
 */
public class KubernetesNamespaceResourceMonitor implements Runnable {

	private final IFramework                 framework;
	private final IResourceManagement        resourceManagement;
	private final IDynamicStatusStoreService dss;
	private final Log                        logger = LogFactory.getLog(this.getClass());
	private final Pattern                    slotRunPattern = Pattern.compile("^slot\\.run\\.(\\w+)\\.cluster\\.(\\w+)\\.namespace\\.(\\w+)$");

	public KubernetesNamespaceResourceMonitor(IFramework framework, 
			IResourceManagement resourceManagement,
			IDynamicStatusStoreService dss, 
			KubernetesResourceManagement kubernetesResourceManagement,
			IConfigurationPropertyStoreService cps) {
		this.framework          = framework;
		this.resourceManagement = resourceManagement;
		this.dss = dss;
		this.logger.info("Kubernetes Namespace resource monitor initialised");
	}

	
	@Override
	public void run() {
		logger.info("Starting Kubernetes Namespace search");
		try {
			//*** Find all the runs with slots
			Map<String, String> slotRuns = dss.getPrefix("slot.run.");
			
			Set<String> activeRunNames = this.framework.getFrameworkRuns().getActiveRunNames();

			for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String runName = matcher.group(1);

					if (!activeRunNames.contains(runName)) {
						String cluster   = matcher.group(2);
						String namespace = matcher.group(3);

						logger.info("Discarding Namespace " + namespace + " on cluster " + cluster + " as run " + runName + " has gone");

						try {
							KubernetesNamespaceImpl.deleteDss(runName, cluster, namespace, dss, this.framework);
						} catch(Exception e) {
							logger.error("Failed to discard namespace " + namespace + " on cluster " + cluster + " as run " + runName,e);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Failure during slot scan",e);
		}

		this.resourceManagement.resourceManagementRunSuccessful();
		logger.info("Finished Kubernetes Namespace search");
	}

	public void runFinishedOrDeleted(String runName) {
		try {
			Map<String, String> slotRuns = dss.getPrefix("slot.run." + runName + ".");
			for(String key : slotRuns.keySet()) {
				Matcher matcher = slotRunPattern.matcher(key);
				if (matcher.find()) {
					String cluster   = matcher.group(2);
					String namespace = matcher.group(3);

					logger.info("Discarding Namespace " + namespace + " on cluster " + cluster + " as run " + runName + " has gone");

					try {
                        KubernetesNamespaceImpl.deleteDss(runName, cluster, namespace, dss, this.framework);
					} catch(Exception e) {
						logger.error("Failed to discard namespace " + namespace + " on cluster " + cluster + " as run " + runName,e);
					}
				}
			}
		} catch(Exception e) {
			logger.error("Failed to delete namespaces for run " + runName);
		}
	}

}
