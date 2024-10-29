/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;
import dev.galasa.openstack.manager.internal.properties.OpenstackPropertiesSingleton;

@Component(service = { IResourceManagementProvider.class })
public class OpenstackResourceManagement implements IResourceManagementProvider {

    private IFramework                 framework;
    private IResourceManagement        resourceManagement;
    private IDynamicStatusStoreService dss;
    private OpenstackHttpClient        openstackHttpClient;

    private ServerResourceMonitor      serverResourceMonitor;
    private FloatingIpResourceMonitor  fipResourceMonitor;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        try {
            this.dss = this.framework.getDynamicStatusStoreService(OpenstackManagerImpl.NAMESPACE);
            OpenstackPropertiesSingleton
                    .setCps(this.framework.getConfigurationPropertyService(OpenstackManagerImpl.NAMESPACE));
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise OpenStack resource monitor", e);
        }

        try {
            this.openstackHttpClient = new OpenstackHttpClient(this.framework);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ResourceManagerException("Unable to initialise the OpenStack HTTP Client", e);
        }

        // TODO Must add a check every 5 minutes to tidy up all the properties that may
        // have been left hanging
        // TODO Add scan of the OpenStack server to see if compute servers and floating
        // ips have been left hanging around

        serverResourceMonitor = new ServerResourceMonitor(framework, resourceManagement, dss, this.openstackHttpClient);
        fipResourceMonitor = new FloatingIpResourceMonitor(framework, resourceManagement, dss,
                this.openstackHttpClient);

        return true;
    }

    @Override
    public void start() {
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(serverResourceMonitor,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(fipResourceMonitor,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        this.serverResourceMonitor.runFinishedOrDeleted(runName);
        this.fipResourceMonitor.runFinishedOrDeleted(runName);

    }

}
