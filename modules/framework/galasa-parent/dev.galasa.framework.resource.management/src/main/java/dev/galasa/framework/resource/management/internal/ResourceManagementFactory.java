/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.BundleManager;
import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.streams.IStreamsService;

/**
 * A factory class that provides methods to create real instances of the dependencies used in
 * resource management classes.
 */
public class ResourceManagementFactory implements IResourceManagementFactory {

    private IFrameworkInitialisation frameworkInitialisation;
    private ScheduledExecutorService scheduledExecutorService;
    private IResourceManagementProviders resourceManagementProviders;

    @Override
    public IFrameworkInitialisation getFrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {
        if (this.frameworkInitialisation == null) {
            try {
                this.frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties, GalasaFactory.getInstance().newResourceManagerInitStrategy());
            } catch (Exception e) {
                throw new FrameworkException("Unable to initialise the Framework Services", e);
            }
        }
        return this.frameworkInitialisation;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService(int numberOfRunThreads) {
        if (this.scheduledExecutorService == null) {
            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(numberOfRunThreads);
        }
        return this.scheduledExecutorService;
    }

    @Override
    public IBundleManager getBundleManager() {
        return new BundleManager();
    }

    @Override
    public IResourceMonitorBundleLoader getBundleLoader(BundleContext bundleContext, IStreamsService streamsService,
            RepositoryAdmin repositoryAdmin, IMavenRepository mavenRepository) throws FrameworkException {
        return new ResourceMonitorBundleLoader(bundleContext, streamsService, repositoryAdmin, mavenRepository);
    }

    @Override
    public IResourceManagementProviders getResourceManagementProviders(IFramework framework,
            IConfigurationPropertyStoreService cps, BundleContext bundleContext, IResourceManagement resourceManagement,
            MonitorConfiguration monitorConfig) throws FrameworkException {
        if (this.resourceManagementProviders == null) {
            this.resourceManagementProviders = new ResourceManagementProviders(framework, cps, bundleContext, resourceManagement, monitorConfig);
        }
        return this.resourceManagementProviders;
    }
}
