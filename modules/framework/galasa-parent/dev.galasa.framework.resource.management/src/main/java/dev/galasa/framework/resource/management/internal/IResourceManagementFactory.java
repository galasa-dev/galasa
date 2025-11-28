/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.streams.IStreamsService;

/**
 * An interface for resource management factories to implement.
 * This provides methods to create certain dependencies used by resource management
 * and allows us to inject a mock factory for unit tests.
 */
public interface IResourceManagementFactory {

    IFrameworkInitialisation getFrameworkInitialisation(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException;

    ScheduledExecutorService getScheduledExecutorService(int numberOfRunThreads);

    IBundleManager getBundleManager();

    IResourceMonitorBundleLoader getBundleLoader(
        BundleContext bundleContext,
        IStreamsService streamsService,
        RepositoryAdmin repositoryAdmin,
        IMavenRepository mavenRepository
    ) throws FrameworkException;

    IResourceManagementProviders getResourceManagementProviders(
        IFramework framework,
        IConfigurationPropertyStoreService cps,
        BundleContext bundleContext,
        IResourceManagement resourceManagement,
        MonitorConfiguration monitorConfig
    ) throws FrameworkException;
}
