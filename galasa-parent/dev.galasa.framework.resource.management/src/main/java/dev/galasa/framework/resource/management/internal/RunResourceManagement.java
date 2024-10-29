/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;

@Component(service = { IResourceManagementProvider.class })
public class RunResourceManagement implements IResourceManagementProvider {
    private final Log                          logger = LogFactory.getLog(getClass());
    private IFramework                         framework;
    private IResourceManagement                resourceManagement;
    private IDynamicStatusStoreService         dss;
    private IConfigurationPropertyStoreService cps;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement)
            throws ResourceManagerException {
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        try {
            this.dss = this.framework.getDynamicStatusStoreService("framework");
            this.cps = this.framework.getConfigurationPropertyService("framework");
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise Active Run resource monitor", e);
        }

        return true;
    }

    @Override
    public void start() {

        try {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    new RunDeadHeartbeatMonitor(this.framework, this.resourceManagement, this.dss, this, cps),
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Run Dead Heartbeat monitor", e);
        }
        try {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    new RunExpiredSharedEnvironment(this.framework, this.resourceManagement, this.dss, this, cps),
                    this.framework.getRandom().nextInt(1), 5, TimeUnit.MINUTES);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Run Dead Heartbeat monitor", e);
        }
        try {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    new RunFinishedRuns(this.framework, this.resourceManagement, this.dss, this, cps),
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Finished Run monitor", e);
        }
        try {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    new RunWaitingRuns(this.framework, this.resourceManagement, this.dss, this, cps),
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Finished Run monitor", e);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
    }

}
