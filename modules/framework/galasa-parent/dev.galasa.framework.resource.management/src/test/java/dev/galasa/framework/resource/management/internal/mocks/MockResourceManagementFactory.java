/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal.mocks;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.mocks.MockBundleManager;
import dev.galasa.framework.mocks.MockFramework;
import dev.galasa.framework.mocks.MockFrameworkInitialisation;
import dev.galasa.framework.resource.management.internal.IResourceManagementFactory;
import dev.galasa.framework.resource.management.internal.IResourceManagementProviders;
import dev.galasa.framework.resource.management.internal.IResourceMonitorBundleLoader;
import dev.galasa.framework.resource.management.internal.MonitorConfiguration;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.streams.IStreamsService;

public class MockResourceManagementFactory implements IResourceManagementFactory {

    private MockFramework mockFramework;
    private IResourceManagementProviders resourceManagementProviders;
    private ScheduledExecutorService scheduledExecutorService;
    private IResourceMonitorBundleLoader monitorBundleLoader;
    
    public MockResourceManagementFactory(
        MockFramework mockFramework,
        IResourceManagementProviders resourceManagementProviders,
        ScheduledExecutorService scheduledExecutorService,
        IResourceMonitorBundleLoader monitorBundleLoader
    ) {
        this.mockFramework = mockFramework;
        this.resourceManagementProviders = resourceManagementProviders;
        this.scheduledExecutorService = scheduledExecutorService;
        this.monitorBundleLoader = monitorBundleLoader;
    }

    @Override
    public IFrameworkInitialisation getFrameworkInitialisation(Properties bootstrapProperties,
            Properties overrideProperties) throws FrameworkException {
        return new MockFrameworkInitialisation(mockFramework);
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService(int numberOfRunThreads) {
        return this.scheduledExecutorService;
    }

    @Override
    public IBundleManager getBundleManager() {
        return new MockBundleManager();
    }

    @Override
    public IResourceManagementProviders getResourceManagementProviders(IFramework framework,
            IConfigurationPropertyStoreService cps, BundleContext bundleContext, IResourceManagement resourceManagement,
            MonitorConfiguration monitorConfig) throws FrameworkException {
        return this.resourceManagementProviders;
    }

    @Override
    public IResourceMonitorBundleLoader getBundleLoader(BundleContext bundleContext, IStreamsService streamsService,
            RepositoryAdmin repositoryAdmin, IMavenRepository mavenRepository) throws FrameworkException {
        return this.monitorBundleLoader;
    }
    
}
