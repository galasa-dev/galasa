/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

/**
 * Resource management for Selenium manager.
 * 
 * We only watch the slot properties and the Grid interactions. Both Docker
 * and Kubernetes are responsible for the own cleanup of stale resources.
 * 
 *  
 *
 */
@Component(service = { IResourceManagementProvider.class })
public class SeleniumResourceManagement implements IResourceManagementProvider{
	
	private IFramework                              framework;
    private IResourceManagement                     resourceManagement;
    private IDynamicStatusStoreService              dss;
    private IConfigurationPropertyStoreService      cps;
    
    private SeleniumSlotResourceMonitor slotMonitor;
    private SeleniumGridSessionMonitor gridSessionMonitor;

	
	@Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework              = framework;
        this.resourceManagement     = resourceManagement;

        try {
            this.cps = framework.getConfigurationPropertyService("selenium");
            this.dss = framework.getDynamicStatusStoreService("selenium");
        } catch(DynamicStatusStoreException e) {
            throw new ResourceManagerException("Could not initialise docker resource monitor, due to the CPS:  ", e);
        } catch(ConfigurationPropertyStoreException e) {
            throw new ResourceManagerException("Could not initialise docker resource monitor, due to the DSS:  ", e);
        }

        slotMonitor = new SeleniumSlotResourceMonitor(framework, resourceManagement, dss, cps);
        gridSessionMonitor = new SeleniumGridSessionMonitor(framework, resourceManagement, dss, cps);
        return true;

    }


	@Override
	public void start() {
		this.resourceManagement.
        getScheduledExecutorService().
        scheduleWithFixedDelay(slotMonitor, this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
		this.resourceManagement.
        getScheduledExecutorService().
        scheduleWithFixedDelay(gridSessionMonitor, this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
	}


	@Override
	public void shutdown() {
		
	}


	@Override
	public void runFinishedOrDeleted(String runName) {
		// TODO Auto-generated method stub
		
	}

}
