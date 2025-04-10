/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.security.SecureRandom;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssAdd;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.properties.PoolPorts;

public class ZosPoolPorts {
	
    private static final Log logger = LogFactory.getLog(ZosPoolPorts.class);

	private final ZosManagerImpl 			 manager;
	private final IDynamicStatusStoreService dss;
	private final IResourcePoolingService 	 rps;	
		
	public ZosPoolPorts(ZosManagerImpl manager, IDynamicStatusStoreService dss, IResourcePoolingService rps) throws ZosManagerException {
		this.manager = manager;
		this.dss = dss;
		this.rps = rps;
	}

	public String allocatePort(String image) throws ZosManagerException, ConfigurationPropertyStoreException, InterruptedException, ResourceUnavailableException {
		return allocatePort(image, 1);
	}
	
	public String allocatePort(String image, int retryCount) throws ZosManagerException, ConfigurationPropertyStoreException, InterruptedException, ResourceUnavailableException {

		if (retryCount > 10) {
            // tried too many times
            throw new ResourceUnavailableException("Failed to allocate a zos port after several attempts.");
        }
		
		// Get the pool of ports for this image
		List<String> resourceStrings = PoolPorts.get(image);
		List<String> zosPorts = null;
		String thePort = null;
		
		// Retrieve a free resource from the pool
		try {
			zosPorts = this.rps.obtainResources(resourceStrings,  null, 1, 1, dss, "zosport." + image);
		} catch (InsufficientResourcesAvailableException exception) {
			throw new ZosManagerException("Could not obtain a port from the z/OS port pool for image " + image);
		}
		
		// Allocate the port retrieved from the pool
		try {	
			
			// There should only be a single port in the list allocated
			thePort = zosPorts.get(0);
			
			// Allocate the port in the DSS
			this.dss.performActions(
					new DssAdd("zosport." + image + "." + thePort, this.manager.getFramework().getTestRunName()),
					new DssAdd("run." + this.manager.getFramework().getTestRunName() + ".zosport." + image + "." + thePort, "active"));

			logger.trace("Allocated z/OS port " + thePort + " on image " + image + " from z/OS port pool allocation");
				
		} catch (DynamicStatusStoreException exception) {
			logger.info("Allocation of port failed due to collision. z/OS port " + thePort + " on image " + image);

			//*** collision on the port, retry
			Thread.sleep(200 + new SecureRandom().nextInt(200)); // *** To avoid race conditions
			return allocatePort(image, retryCount++);
		}
		return thePort;
	}
	
	public static void deleteDss(String port, String image, String run, IDynamicStatusStoreService dss) throws DynamicStatusStoreMatchException, DynamicStatusStoreException {
		dss.performActions(
				new DssDelete("zosport." + image + "." + port, run),
				new DssDelete("run." + run + ".zosport." + image + "." + port, "active"));
	}
}
