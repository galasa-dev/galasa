/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;

import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.streams.IStreamsService;

/**
 * This is an abstract base class that provides methods that can be commonly
 * used by classes that implement the IResourceManagement interface.
 */
public abstract class AbstractResourceManagement implements IResourceManagement {

    private Log logger = LogFactory.getLog(this.getClass());

    protected IResourceManagementFactory factory;

    protected IFramework framework;
    protected IConfigurationPropertyStoreService cps;
    protected IStreamsService streamsService;
    private ScheduledExecutorService scheduledExecutorService;

    public AbstractResourceManagement() {
        this(new ResourceManagementFactory());
    }

    public AbstractResourceManagement(IResourceManagementFactory factory) {
        this.factory = factory;
    }
    
    protected void init(
        Properties bootstrapProperties,
        Properties overrideProperties,
        String stream,
        List<String> bundleIncludes,
        List<String> bundleExcludes
    ) throws FrameworkException {
        IFrameworkInitialisation frameworkInitialisation = factory.getFrameworkInitialisation(bootstrapProperties, overrideProperties);
        this.framework = frameworkInitialisation.getFramework();
        this.cps = framework.getConfigurationPropertyService("framework");
        this.streamsService = framework.getStreamsService();

        int numberOfRunThreads = getRunThreadCount(cps);
        this.scheduledExecutorService = factory.getScheduledExecutorService(numberOfRunThreads);
    }

    private int getRunThreadCount(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
        int runThreadCount = 5;
        String threads = AbstractManager.nulled(cps.getProperty("resource.management", "threads"));
        if (threads != null) {
            runThreadCount = Integer.parseInt(threads);
        }
        return runThreadCount ;
    }

    protected void loadMonitorBundles(
        BundleContext bundleContext,
        String stream,
        RepositoryAdmin repositoryAdmin,
        IMavenRepository mavenRepository
    ) throws FrameworkException {
        // Load the requested monitor bundles
        IBundleManager bundleManager = factory.getBundleManager();
        IResourceMonitorBundleLoader monitorBundleLoader = factory.getBundleLoader(bundleContext, streamsService, repositoryAdmin, mavenRepository);
        monitorBundleLoader.loadMonitorBundles(bundleManager, stream);
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return this.scheduledExecutorService;
    }

    @Override
    public synchronized void resourceManagementRunSuccessful() {
    }

    protected void shutdown() {
        if (this.scheduledExecutorService != null) {
            // *** shutdown the scheduler
            logger.error("Asking the scheduler to shut down.");
            this.scheduledExecutorService.shutdown();
            try {
                this.scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
                logger.error("The scheduler shut down ok.");
            } catch (Exception e) {
                logger.error("Unable to shutdown the scheduler");
            }
        }
    }
}
