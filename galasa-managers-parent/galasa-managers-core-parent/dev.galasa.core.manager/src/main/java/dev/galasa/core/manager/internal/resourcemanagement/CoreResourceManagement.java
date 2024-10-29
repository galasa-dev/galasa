/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal.resourcemanagement;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.core.manager.internal.CoreManagerImpl;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

/**
 * Core Resource Management
 * 
 * Called by the Resource Manager to maintain the DSS, by cleaning up any
 * resources after a test has been terminated
 *  
 * 
 *  
 *
 */
@Component(service = { IResourceManagementProvider.class })
public class CoreResourceManagement implements IResourceManagementProvider {

    private IFramework                 framework;
    private IResourceManagement        resourceManagement;
    private IDynamicStatusStoreService dss;

    // Resources Strings from @ResourceString
    private ResourceStringResourceManagement       slotResourceManagement;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        try {
            this.dss = this.framework.getDynamicStatusStoreService(CoreManagerImpl.NAMESPACE);
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise Linux resource manager", e);
        }

        // Set up the Resource String management routine
        slotResourceManagement = new ResourceStringResourceManagement(framework, resourceManagement, dss);

        return true;
    }

    @Override
    public void start() {
    	// Start the 20 second check for Resources strings left hangin
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.slotResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
    	// A run has been detected as deleted, pass to the individual resources routines
        this.slotResourceManagement.runFinishedOrDeleted(runName);

    }

}
