/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;
import java.util.concurrent.TimeUnit;
import org.osgi.service.component.annotations.Component;

/**
 * This class acts as the high-level resource management deamon for
 * SDV manager.
 *
 * <p>It will ensure sdv entries in the DSS are kept in-sync,
 * up-to-date, and will tidy up any stale information.
 *
 */
@Component(service = {IResourceManagementProvider.class})
public class SdvResourceManagement implements IResourceManagementProvider {

    private IFramework framework;
    private IResourceManagement resourceManagement;

    private SdvUserResourceMonitor sdvUserResourceMonitor;
    private SdvManagersResourceMonitor sdvManagersResourceMonitor;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;

        IDynamicStatusStoreService dss;
        try {
            dss = this.framework.getDynamicStatusStoreService("sdv");
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise 'sdv' namespace from DSS.", e);
        }

        sdvUserResourceMonitor = new SdvUserResourceMonitor(
            framework,
            resourceManagement,
            dss
        );
        sdvManagersResourceMonitor = new SdvManagersResourceMonitor(
            framework,
            resourceManagement,
            dss
        );

        return true;
    }

    @Override
    public void start() {

        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                sdvUserResourceMonitor, this.framework.getRandom().nextInt(20), 20,
                TimeUnit.SECONDS);

        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
            sdvManagersResourceMonitor, this.framework.getRandom().nextInt(20), 20,
            TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        // Not required
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        this.sdvUserResourceMonitor.runFinishedOrDeleted(runName);
        this.sdvManagersResourceMonitor.runFinishedOrDeleted(runName);
    }
}
