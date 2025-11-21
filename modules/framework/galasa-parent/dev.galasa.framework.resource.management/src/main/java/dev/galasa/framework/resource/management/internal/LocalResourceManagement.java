/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.FrameworkException;

/**
 * An implementation of resource management intended to clean up resources 
 * allocated during local runs
 */
@Component(service = { LocalResourceManagement.class })
public class LocalResourceManagement extends AbstractResourceManagement {

    private Log logger = LogFactory.getLog(this.getClass());

    private BundleContext bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected IMavenRepository mavenRepository;

    private IResourceManagementProviders resourceManagementProviders;

    public LocalResourceManagement() {
        super();
    }

    public LocalResourceManagement(IResourceManagementFactory factory) {
        super(factory);
    }

    /**
     * Run Resource Management locally
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void run(
        Properties bootstrapProperties,
        Properties overrideProperties,
        String stream,
        List<String> bundleIncludes,
        List<String> bundleExcludes
    ) throws FrameworkException {

        try {
            super.init(bootstrapProperties, overrideProperties, stream, bundleIncludes, bundleExcludes);

            loadMonitorBundles(bundleContext, stream, repositoryAdmin, mavenRepository);

            logger.info("Starting Resource Management");
            MonitorConfiguration monitorConfig = new MonitorConfiguration(stream, bundleIncludes, bundleExcludes);
            this.resourceManagementProviders = factory.getResourceManagementProviders(framework, cps, bundleContext, this, monitorConfig);

            // If no resource management providers have been initialised then there's nothing to do, so shut down
            if (this.resourceManagementProviders.getLoadedResourceManagementProviders().isEmpty()) {
                logger.info("No resource management providers have been loaded, shutting down");
            } else {
                logger.info("Running resource management providers");
                this.resourceManagementProviders.runOnce();
            }

            
        } finally {
            shutdown();
            logger.info("Resource Management shutdown is complete.");
        }
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        if (resourceManagementProviders != null) {
            resourceManagementProviders.shutdown();
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }
}