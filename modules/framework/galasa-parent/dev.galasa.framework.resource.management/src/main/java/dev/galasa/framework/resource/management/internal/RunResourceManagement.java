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
import dev.galasa.framework.spi.utils.SystemTimeService;

@Component(service = { IResourceManagementProvider.class })
public class RunResourceManagement implements IResourceManagementProvider {
    private final Log                          logger = LogFactory.getLog(getClass());
    private IFramework                         framework;
    private IResourceManagement                resourceManagement;
    private IDynamicStatusStoreService         dss;
    private IConfigurationPropertyStoreService cps;
    private RunDeadHeartbeatMonitor            deadHeartbeatMonitor;
    private RunExpiredSharedEnvironment        runExpiredSharedEnvironment;
    private RunFinishedRuns                    runFinishedRuns;
    private RunInactiveRunCleanup              runInactiveRunCleanup;
    private RunWaitingRuns                     runWaitingRuns;

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

        try {
            this.deadHeartbeatMonitor = new RunDeadHeartbeatMonitor(this.framework, this.resourceManagement, this.dss, this, cps);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Run Dead Heartbeat monitor", e);
        }

        try {
            this.runExpiredSharedEnvironment = new RunExpiredSharedEnvironment(this.framework, this.resourceManagement, this.dss, this, cps);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Run expired shared environment monitor", e);
        }

        try {
            this.runFinishedRuns = new RunFinishedRuns(this.framework, this.resourceManagement, this.dss, this, cps);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise Finished Run monitor", e);
        }

        try {
            this.runInactiveRunCleanup = new RunInactiveRunCleanup(this.framework.getFrameworkRuns(), this.resourceManagement,
                new SystemTimeService(), cps);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise inactive run monitor", e);
        }

        try {
            this.runWaitingRuns = new RunWaitingRuns(this.framework, this.resourceManagement, this.dss, this, cps);
        } catch (FrameworkException e) {
            logger.error("Unable to initialise waiting run monitor", e);
        }

        return true;
    }

    @Override
    public void start() {
        if (this.deadHeartbeatMonitor != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.deadHeartbeatMonitor,
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        }

        if (this.runExpiredSharedEnvironment != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    runExpiredSharedEnvironment,
                    this.framework.getRandom().nextInt(1), 5, TimeUnit.MINUTES);
        }

        if (this.runFinishedRuns != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runFinishedRuns,
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        }

        if (this.runInactiveRunCleanup != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runInactiveRunCleanup,
                    this.framework.getRandom().nextInt(20), 5, TimeUnit.MINUTES);
        }

        if (this.runWaitingRuns != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runWaitingRuns,
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        }
    }

    @Override
    public void runOnce() {
        if (this.deadHeartbeatMonitor != null) {
            this.deadHeartbeatMonitor.run();
        }

        if (this.runExpiredSharedEnvironment != null) {
            this.runExpiredSharedEnvironment.run();
        }

        if (this.runFinishedRuns != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runFinishedRuns,
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        }

        if (this.runInactiveRunCleanup != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runInactiveRunCleanup,
                    this.framework.getRandom().nextInt(20), 5, TimeUnit.MINUTES);
        }

        if (this.runWaitingRuns != null) {
            this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(
                    this.runWaitingRuns,
                    this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
    }

}
