/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
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
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Run Resource Management
 */
@Component(service = { ResourceManagement.class })
public class ResourceManagement extends AbstractResourceManagement {

    private Log logger = LogFactory.getLog(this.getClass());

    private BundleContext                                bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected IMavenRepository mavenRepository;

    private ResourceManagementProviders                  resourceManagementProviders;

    // This flag is set by one thread, and read by another, so we always want the variable to be in memory rather than 
    // in some code-optimised register.
    private volatile boolean                             shutdown                           = false;

    // Same for this flag too. Multi-threaded access.
    private volatile boolean                             shutdownComplete                   = false;

    private Instant                                      lastSuccessfulRun                  = Instant.now();

    private HTTPServer                                   metricsServer;
    private Counter                                      successfulRunsCounter;

    private ResourceManagementHealth                     healthServer;

    private String                                       serverName;
    private String                                       hostname;

    /**
     * Run Resource Management    
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

        // *** Add shutdown hook to allow for orderly shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        try {
            super.init(bootstrapProperties, overrideProperties, stream, bundleIncludes, bundleExcludes);

            // A heartbeat for the resource monitor was previously set and updated but wasn't used anywhere,
            // so any heartbeat-related properties that may still exist in the DSS from previous versions of
            // Galasa should be removed from the DSS to avoid taking up space.
            IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");
            clearHeartbeatProperties(dss);

            // Load the requested monitor bundles
            super.loadMonitorBundles(bundleContext, stream, repositoryAdmin, mavenRepository);

            // *** Now start the Resource Management framework
            logger.info("Starting Resource Management");

            this.hostname = getHostName();
            this.serverName = getServerName(cps, this.serverName);
            
            int metricsPort = getMetricsPort(cps);
            int healthPort = getHealthPort(cps);
 
            this.metricsServer = startMetricsServer(metricsPort);

            // *** Create metrics
            // DefaultExports.initialize() - problem within the the exporter at the moment
            // TODO

            this.successfulRunsCounter = Counter.build().name("galasa_resource_management_successfull_runs")
                    .help("The number of successfull resource management runs").register();

            this.healthServer = createHealthServer(healthPort);

            MonitorConfiguration monitorConfig = new MonitorConfiguration(stream, bundleIncludes, bundleExcludes);
            this.resourceManagementProviders = new ResourceManagementProviders(framework, cps, bundleContext, this, monitorConfig);

            this.resourceManagementProviders.start();
            
            // *** Start the Run watch thread
            ResourceManagementRunWatch runWatch = new ResourceManagementRunWatch(framework, resourceManagementProviders, getScheduledExecutorService());

            logger.info("Resource Manager has started");

            // *** Loop until we are asked to shutdown
            while (!shutdown) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    throw new FrameworkException("Interrupted sleep", e);
                }
            }

            shutdown(runWatch);

        } finally {
            logger.info("Resource Management shutdown is complete.");

            // Let the ShutDownHook know that the main thread has shut things down via this shared-state boolean.
            shutdownComplete = true;
        }
    }

    private void shutdown(ResourceManagementRunWatch runWatch) {
        super.shutdown();
        runWatch.shutdown();
        resourceManagementProviders.shutdown();

        stopMetricsServer(this.metricsServer);
        stopHealthServer(this.healthServer);
    }

    private void stopHealthServer(ResourceManagementHealth healthServer) {
        // *** Stop the health server
        if (healthServer != null) {
            healthServer.shutdown();
        }
    }

    private void stopMetricsServer(HTTPServer metricsServer) {
        if (metricsServer != null) {
            metricsServer.close();
        }
    }



    private ResourceManagementHealth createHealthServer(int healthPort) throws FrameworkException {
        ResourceManagementHealth healthServer = null;
        if (healthPort > 0) {
            healthServer = new ResourceManagementHealth(this, healthPort);
            logger.info("Health monitoring on port " + healthPort);
        } else {
            logger.info("Health monitoring disabled");
        }
        return healthServer ;
    }

    private HTTPServer startMetricsServer(int metricsPort) throws FrameworkException {

        HTTPServer metricsServer = null ;
        if (metricsPort > 0) {
            try {
                metricsServer = new HTTPServer(metricsPort);
                logger.info("Metrics server running on port " + metricsPort);
            } catch (IOException e) {
                throw new FrameworkException("Unable to start the metrics server", e);
            }
        } else {
            logger.info("Metrics server disabled");
        }
        return metricsServer;
    }

    private int getHealthPort(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
        int healthPort = 9011;
        String port = AbstractManager.nulled(cps.getProperty("resource.management.health", "port"));
        if (port != null) {
            healthPort = Integer.parseInt(port);
        }
        return healthPort;
    }

    private int getMetricsPort(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
        int metricsPort = 9010;
        String port = AbstractManager.nulled(cps.getProperty("resource.management.metrics", "port"));
        if (port != null) {
            metricsPort = Integer.parseInt(port);
        }
        return metricsPort ;
    }

    private String getHostName() {
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.error("Unable to obtain the host name", e);
        }
        hostName = hostName.toLowerCase();
        return hostName;
    }

    private String getServerName(IConfigurationPropertyStoreService cps, String serverName) throws ConfigurationPropertyStoreException {
        // The server name may already have been worked out. If so, don't bother doing that again.
        if (serverName==null) {
            AbstractManager.nulled(cps.getProperty("server", "name"));
            if (serverName == null) {
                serverName = AbstractManager.nulled(System.getenv("framework.server.name"));
                if (serverName == null) {
                    String[] split = this.hostname.split("\\.");
                    if (split.length >= 1) {
                        serverName = split[0];
                    }
                }
            }
            if (serverName == null) {
                serverName = "unknown";
            }
            serverName = serverName.toLowerCase();
            serverName = serverName.replaceAll("\\.", "-");
        }
        return serverName;
    }

    private void clearHeartbeatProperties(IDynamicStatusStoreService dss) {
        try {
            dss.deletePrefix("servers.resourcemonitor.");
        } catch (DynamicStatusStoreException e) {
            logger.error("Problem clearing heartbeat properties", e);
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

    @Override
    public synchronized void resourceManagementRunSuccessful() {
        this.lastSuccessfulRun = Instant.now();

        this.successfulRunsCounter.inc();
    }

    protected synchronized Instant getLastSuccessfulRun() {
        return this.lastSuccessfulRun;
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            ResourceManagement.this.logger.info("Shutdown request received");
            
            // Tell the main thread to shut down via this shared variable.
            ResourceManagement.this.shutdown = true;

            while (!shutdownComplete) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    ResourceManagement.this.logger.info("Shutdown wait was interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

}