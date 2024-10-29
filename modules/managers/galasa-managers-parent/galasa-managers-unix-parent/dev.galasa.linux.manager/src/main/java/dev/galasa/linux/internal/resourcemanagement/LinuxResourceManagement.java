/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.resourcemanagement;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;
import dev.galasa.linux.internal.LinuxManagerImpl;
import dev.galasa.linux.internal.properties.LinuxPropertiesSingleton;

@Component(service = { IResourceManagementProvider.class })
public class LinuxResourceManagement implements IResourceManagementProvider {

    private IFramework                 framework;
    private IResourceManagement        resourceManagement;
    private IDynamicStatusStoreService dss;

    private UsernameResourceManagement   usernameResourceManagement;
    private SlotResourceManagement       slotResourceManagement;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        try {
            this.dss = this.framework.getDynamicStatusStoreService(LinuxManagerImpl.NAMESPACE);
            LinuxPropertiesSingleton
                    .setCps(this.framework.getConfigurationPropertyService(LinuxManagerImpl.NAMESPACE));
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise Linux resource manager", e);
        }

        usernameResourceManagement = new UsernameResourceManagement(framework, resourceManagement, dss);
        slotResourceManagement = new SlotResourceManagement(framework, resourceManagement, dss);

        return true;
    }

    @Override
    public void start() {
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.usernameResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.slotResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        this.usernameResourceManagement.runFinishedOrDeleted(runName);
        this.slotResourceManagement.runFinishedOrDeleted(runName);

    }

}
