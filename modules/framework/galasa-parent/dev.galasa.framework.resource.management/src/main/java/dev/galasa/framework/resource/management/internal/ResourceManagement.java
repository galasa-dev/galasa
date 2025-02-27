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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

/**
 * Run Resource Management
 */
@Component(service = { ResourceManagement.class })
public class ResourceManagement implements IResourceManagement {

    private Log                                          logger                             = LogFactory
            .getLog(this.getClass());

    private BundleContext                                bundleContext;

    private final ArrayList<IResourceManagementProvider> resourceManagementProviders        = new ArrayList<>();
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
    public void run(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

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

            // *** Now start the Resource Management framework

            logger.info("Starting Resource Management");

            // *** Calculate servername

            this.hostname = "unknown";
            try {
                this.hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.error("Unable to obtain the host name", e);
            }
            this.serverName = AbstractManager.nulled(cps.getProperty("server", "name"));
            if (this.serverName == null) {
                this.serverName = AbstractManager.nulled(System.getenv("framework.server.name"));
                if (this.serverName == null) {
                    String[] split = this.hostname.split("\\.");
                    if (split.length >= 1) {
                        this.serverName = split[0];
                    }
                }
            }
            if (serverName == null) {
                this.serverName = "unknown";
            }
            this.serverName = this.serverName.toLowerCase();
            this.hostname = this.hostname.toLowerCase();
            this.serverName = this.serverName.replaceAll("\\.", "-");

            // *** Setup defaults and properties

            int numberOfRunThreads = 5;
            int metricsPort = 9010;
            int healthPort = 9011;

            String threads = AbstractManager.nulled(cps.getProperty("resource.management", "threads"));
            if (threads != null) {
                numberOfRunThreads = Integer.parseInt(threads);
            }

            String port = AbstractManager.nulled(cps.getProperty("resource.management.metrics", "port"));
            if (port != null) {
                metricsPort = Integer.parseInt(port);
            }

            port = AbstractManager.nulled(cps.getProperty("resource.management.health", "port"));
            if (port != null) {
                healthPort = Integer.parseInt(port);
            }

            // *** Setup scheduler
            scheduledExecutorService = new ScheduledThreadPoolExecutor(numberOfRunThreads);

            // *** Start the metrics server
            if (metricsPort > 0) {
                try {
                    this.metricsServer = new HTTPServer(metricsPort);
                    logger.info("Metrics server running on port " + metricsPort);
                } catch (IOException e) {
                    throw new FrameworkException("Unable to start the metrics server", e);
                }
            } else {
                logger.info("Metrics server disabled");
            }

            // *** Create metrics
            // DefaultExports.initialize() - problem within the the exporter at the moment
            // TODO

            this.successfulRunsCounter = Counter.build().name("galasa_resource_management_successfull_runs")
                    .help("The number of successfull resource management runs").register();

            // *** Create Health Server
            if (healthPort > 0) {
                this.healthServer = new ResourceManagementHealth(this, healthPort);
                logger.info("Health monitoring on port " + healthPort);
            } else {
                logger.info("Health monitoring disabled");
            }

            // *** Locate all the Resource Management providers in the framework
            try {
                final ServiceReference<?>[] rmpServiceReference = bundleContext
                        .getAllServiceReferences(IResourceManagementProvider.class.getName(), null);
                if ((rmpServiceReference == null) || (rmpServiceReference.length == 0)) {
                    logger.info("No additional Resource Manager providers have been found");
                } else {
                    for (final ServiceReference<?> rmpReference : rmpServiceReference) {
                        final IResourceManagementProvider rmpStoreRegistration = (IResourceManagementProvider) bundleContext
                                .getService(rmpReference);
                        try {
                            if (rmpStoreRegistration.initialise(framework, this)) {
                                logger.info(
                                        "Found Resource Management Provider " + rmpStoreRegistration.getClass().getName());
                                resourceManagementProviders.add(rmpStoreRegistration);
                            } else {
                                logger.info("Resource Management Provider " + rmpStoreRegistration.getClass().getName()
                                        + " opted out of this Resource Management run");
                            }
                        } catch (Exception e) {
                            logger.error("Failed initialisation of Resource Management Provider "
                                    + rmpStoreRegistration.getClass().getName() + " ignoring", e);
                        }
                    }
                }
            } catch (Exception e) {
                throw new FrameworkException("Problem during Resource Manager initialisation", e);
            }

            // *** Start the providers
            for (IResourceManagementProvider provider : resourceManagementProviders) {
                logger.debug("starting provider "+provider.getClass().getCanonicalName());
                provider.start();
                logger.debug("started provider "+provider.getClass().getCanonicalName());
            }

            // *** Start the Run watch thread
            ResourceManagementRunWatch runWatch = new ResourceManagementRunWatch(framework, this);

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

            // *** Ask the run watch to terminate
            runWatch.shutdown();

            // *** shutdown the providers
            for (IResourceManagementProvider provider : resourceManagementProviders) {
                logger.info("Requesting Resource Management Provider " + provider.getClass().getName() + " shutdown");
                provider.shutdown();
                logger.debug("Resource Management Provider " + provider.getClass().getName() + " shutdown OK");
            }

            // *** Stop the metics server
            if (metricsPort > 0) {
                this.metricsServer.close();
            }

            // *** Stop the health server
            if (healthPort > 0) {
                this.healthServer.shutdown();
            }

        } finally {
            logger.info("Resource Management shutdown is complete.");

            // Let the ShutDownHook know that the main thread has shut things down via this shared-state boolean.
            shutdownComplete = true;
        }
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

    public void runFinishedOrDeleted(String runName) {
        for (IResourceManagementProvider provider : resourceManagementProviders) {
            logger.debug("About to call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
            provider.runFinishedOrDeleted(runName);
            logger.debug("Returned from call runFinishedOrDeleted() for provider "+provider.getClass().getCanonicalName());
        }
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