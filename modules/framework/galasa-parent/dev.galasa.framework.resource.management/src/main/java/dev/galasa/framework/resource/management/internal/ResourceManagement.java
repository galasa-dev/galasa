/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.resource.management.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IBundleManager;
import dev.galasa.framework.internal.runner.BundleManager;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.resource.management.MonitorConfiguration;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
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
    public void run(Properties bootstrapProperties, Properties overrideProperties, MonitorConfiguration monitorConfig) throws FrameworkException {

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
            IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");

            // Load the requested monitor bundles
            IBundleManager bundleManager = new BundleManager();
            loadMonitorBundles(bundleManager, monitorConfig, cps);

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

            this.resourceManagementProviders = new ResourceManagementProviders(framework, cps, bundleContext, this);

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
    void loadMonitorBundles(IBundleManager bundleManager, MonitorConfiguration monitorConfig, IConfigurationPropertyStoreService cps) throws FrameworkException {
        String stream = monitorConfig.getStream();
        if (stream != null) {
            loadRepositoriesFromStream(stream, cps);
        }

        // Start filtering the bundles in the repository admin
        Set<Resource> bundlesToLoad = filterMonitorBundles(monitorConfig);
        Set<String> finalBundleNamesToLoad = getResourceManagementProviderBundles(bundlesToLoad);

        // Load the resulting bundles that have the IResourceManagementProvider service
        for (String bundle : finalBundleNamesToLoad) {
            if (!bundleManager.isBundleActive(bundleContext, bundle)) {
                bundleManager.loadBundle(repositoryAdmin, bundleContext, bundle);
            }
        }
    }

    private void loadRepositoriesFromStream(String stream, IConfigurationPropertyStoreService cps) throws FrameworkException {
        // Get the stream (TODO: replace with a streams service)
        Map<String, String> streamProperties = cps.getPrefixedProperties("test.stream." + stream);

        // Add the stream's OBR to the repository admin (TODO: replace with a streams service)
        String commaSeparatedObrs = streamProperties.get("test.stream." + stream + ".obr");
        for (String obr : commaSeparatedObrs.split(",")) {
            try {
                repositoryAdmin.addRepository(obr);
            } catch (Exception e) {
                throw new FrameworkException("Unable to load repository " + obr, e);
            }
        }

        // Add the stream's maven repo to the maven repositories (TODO: replace with a streams service)
        String mavenRepo = streamProperties.get("test.stream." + stream + ".repo");
        try {
            mavenRepository.addRemoteRepository(new URL(mavenRepo));
        } catch (MalformedURLException e) {
            throw new FrameworkException("Unable to add remote maven repository " + mavenRepo, e);
        }
    }

    private Set<Resource> filterMonitorBundles(MonitorConfiguration monitorConfig) {
        Set<Resource> bundlesToInclude = new HashSet<>();
        for (Repository repository : repositoryAdmin.listRepositories()) {
            Resource[] resources = repository.getResources();
            if (resources != null) {
                for (Resource resource : resources) {
                    // Find all the bundles that match any regex patterns in the 'includes' list
                    for (Pattern includePattern : monitorConfig.getIncludesRegexList()) {
                        String bundleName = resource.getSymbolicName();
                        Matcher includeMatcher = includePattern.matcher(bundleName);
                        if (includeMatcher.matches()) {
                            bundlesToInclude.add(resource);
                            break;
                        }
                    }
                }
            }
        }
        
        // From the filtered bundles, exclude any that match any regex patterns in the 'excludes' list
        Set<Resource> bundlesToExclude = new HashSet<>();
        for (Resource bundle : bundlesToInclude) {
            for (Pattern excludePattern : monitorConfig.getExcludesRegexList()) {
                String bundleName = bundle.getSymbolicName();
                Matcher excludeMatcher = excludePattern.matcher(bundleName);
                if (excludeMatcher.matches()) {
                    bundlesToExclude.add(bundle);
                    break;
                }
            }
        }

        bundlesToInclude.removeAll(bundlesToExclude);
        return bundlesToInclude;
    }

    private Set<String> getResourceManagementProviderBundles(Set<Resource> resources) {
        Set<String> resourceMonitorBundles = new HashSet<>();

        resourceSearch: for (Resource resource : resources) {
            if (resource.getCapabilities() != null) {
                for (Capability capability : resource.getCapabilities()) {
                    if ("service".equals(capability.getName())) {
                        Map<String, Object> properties = capability.getPropertiesAsMap();

                        String services = (String) properties.get("objectClass");
                        if (services == null) {
                            services = (String) properties.get("objectClass:List<String>");
                        }

                        if (services != null) {
                            for (String service : services.split(",")) {
                                if ("dev.galasa.framework.spi.IResourceManagementProvider".equals(service)) {
                                    resourceMonitorBundles.add(resource.getSymbolicName());
                                    continue resourceSearch;
                                }
                            }
                        }
                    }
                }
            }
        }
        return resourceMonitorBundles;
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