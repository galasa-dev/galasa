/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import dev.galasa.framework.BundleManager;
import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.streams.IOBR;
import dev.galasa.framework.spi.streams.IStream;
import dev.galasa.framework.spi.streams.IStreamsService;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Run Resource Management
 */
@Component(service = { ResourceManagement.class })
public class ResourceManagement implements IResourceManagement {

    private Log logger = LogFactory.getLog(this.getClass());

    private BundleContext                                bundleContext;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected RepositoryAdmin repositoryAdmin;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    protected IMavenRepository mavenRepository;

    private ResourceManagementProviders                  resourceManagementProviders;
    private ScheduledExecutorService                     scheduledExecutorService;

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
            // *** Initialise the framework services
            FrameworkInitialisation frameworkInitialisation = null;
            try {
                frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties, GalasaFactory.getInstance().newResourceManagerInitStrategy());
            } catch (Exception e) {
                throw new FrameworkException("Unable to initialise the Framework Services", e);
            }
            IFramework framework = frameworkInitialisation.getFramework();

            IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
            IStreamsService streamsService = framework.getStreamsService();
            IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");

            // Load the requested monitor bundles
            IBundleManager bundleManager = new BundleManager();
            loadMonitorBundles(bundleManager, stream, streamsService);

            // *** Now start the Resource Management framework

            logger.info("Starting Resource Management");

            this.hostname = getHostName();
            this.serverName = getServerName(cps, this.serverName);
            
            int numberOfRunThreads = getRunThreadCount(cps);
            int metricsPort = getMetricsPort(cps);
            int healthPort = getHealthPort(cps);
 

            scheduledExecutorService = new ScheduledThreadPoolExecutor(numberOfRunThreads);

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
            ResourceManagementRunWatch runWatch = new ResourceManagementRunWatch(framework, resourceManagementProviders, scheduledExecutorService);

            logger.info("Resource Manager has started");

            // *** Loop until we are asked to shutdown
            long heartbeatExpire = 0;
            while (!shutdown) {
                if (System.currentTimeMillis() >= heartbeatExpire) {
                    updateHeartbeat(dss);
                    heartbeatExpire = System.currentTimeMillis() + 20000;
                }

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    throw new FrameworkException("Interrupted sleep", e);
                }
            }

            // *** shutdown the scheduler
            logger.error("Asking the scheduler to shut down.");
            this.scheduledExecutorService.shutdown();
            try {
                this.scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
                logger.error("The scheduler shut down ok.");
            } catch (Exception e) {
                logger.error("Unable to shutdown the scheduler");
            }

            runWatch.shutdown();

            resourceManagementProviders.shutdown();
            stopMetricsServer(this.metricsServer);
            stopHealthServer(this.healthServer);

        } finally {
            logger.info("Resource Management shutdown is complete.");

            // Let the ShutDownHook know that the main thread has shut things down via this shared-state boolean.
            shutdownComplete = true;
        }
    }

    // Package-level to allow unit testing
    void loadMonitorBundles(IBundleManager bundleManager, String stream, IStreamsService streamsService) throws FrameworkException {
        if (stream != null && !stream.isBlank()) {
            loadRepositoriesFromStream(stream.trim(), streamsService);
        }

        Set<String> bundlesToLoad = getResourceMonitorBundles();

        // Load the resulting bundles that have the IResourceManagementProvider service
        for (String bundle : bundlesToLoad) {
            if (!bundleManager.isBundleActive(bundleContext, bundle)) {
                logger.info("ResourceManagement - loading bundle: " + bundle);

                bundleManager.loadBundle(repositoryAdmin, bundleContext, bundle);

                logger.info("ResourceManagement - bundle '" + bundle + "' loaded OK");
            }
        }
    }

    private void loadRepositoriesFromStream(String streamName, IStreamsService streamsService) throws FrameworkException {
        IStream stream = streamsService.getStreamByName(streamName);
        stream.validate();

        // Add the stream's maven repo to the maven repositories
        URL mavenRepo = stream.getMavenRepositoryUrl();
        mavenRepository.addRemoteRepository(mavenRepo);

        // Add the stream's OBR to the repository admin
        List<IOBR> obrs = stream.getObrs();
        for (IOBR obr : obrs) {
            try {
                repositoryAdmin.addRepository(obr.toString());
            } catch (Exception e) {
                throw new FrameworkException("Unable to load repository " + obr, e);
            }
        }
    }

    private boolean isResourceMonitorCapability(Capability capability) {
        boolean isResourceMonitor = false;
        if ("service".equals(capability.getName())) {
            Map<String, Object> properties = capability.getPropertiesAsMap();
            String services = (String) properties.get("objectClass");
            if (services == null) {
                services = (String) properties.get("objectClass:List<String>");
            }

            if (services != null) {
                for (String service : services.split(",")) {
                    if ("dev.galasa.framework.spi.IResourceManagementProvider".equals(service)) {
                        isResourceMonitor = true;
                        break;
                    }
                }
            }
        }
        return isResourceMonitor;
    }

    private Set<String> getResourceMonitorBundles() {
        Set<String> bundlesToLoad = new HashSet<>();
        for (Repository repository : repositoryAdmin.listRepositories()) {
            if (repository.getResources() != null) {
                bundlesToLoad.addAll(getResourceMonitorsFromRepository(repository));
            }
        }
        return bundlesToLoad;
    }

    private Set<String> getResourceMonitorsFromRepository(Repository repository) {
        Set<String> resourceMonitorBundles = new HashSet<>();
        for (Resource resource : repository.getResources()) {
            if (isResourceContainingAResourceMonitor(resource)) {
                resourceMonitorBundles.add(resource.getSymbolicName());
            }
        }
        return resourceMonitorBundles;
    }

    private boolean isResourceContainingAResourceMonitor(Resource resource) {
        boolean isResourceContainsResourceMonitor = false;
        if (resource.getCapabilities() != null) {
            for (Capability capability : resource.getCapabilities()) {
                if (isResourceMonitorCapability(capability)) {
                    isResourceContainsResourceMonitor = true;
                    break;
                }
            }
        }
        return isResourceContainsResourceMonitor;
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

    private int getRunThreadCount(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
        int runThreadCount = 5;
        String threads = AbstractManager.nulled(cps.getProperty("resource.management", "threads"));
        if (threads != null) {
            runThreadCount = Integer.parseInt(threads);
        }
        return runThreadCount ;
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

    

    private void updateHeartbeat(IDynamicStatusStoreService dss) {
        Instant time = Instant.now();

        HashMap<String, String> props = new HashMap<>();
        props.put("servers.resourcemonitor." + serverName + ".heartbeat", time.toString());
        props.put("servers.resourcemonitor." + serverName + ".hostname", hostname);

        try {
            dss.put(props);
        } catch (DynamicStatusStoreException e) {
            logger.error("Problem logging heartbeat", e);
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return this.scheduledExecutorService;
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