/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.metrics;

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
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IMetricsServer;
import dev.galasa.framework.spi.IMetricsProvider;
import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;

@Component(service = { MetricsServer.class })
public class MetricsServer implements IMetricsServer {

    private Log                               logger                              = LogFactory.getLog(this.getClass());

    private BundleContext                     bundleContext;

    private final ArrayList<IMetricsProvider> metricsProviders                    = new ArrayList<>();
    private ScheduledExecutorService          scheduledExecutorService;

    private boolean                           shutdown                            = false;
    private boolean                           shutdownComplete                    = false;

    private long                              successfulPollsSinceLastHealthCheck = 0;

    private HTTPServer                        metricsServer;
    private Counter                           successfulPollsCounter;

    private MetricsServerHealth               healthServer;

    private String                            serverName;
    private String                            hostname;

    /**
     * Run Metrics Server
     * 
     * @param bootstrapProperties
     * @param overrideProperties
     * @throws FrameworkException
     */
    public void run(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        // *** Add shutdown hook to allow for orderly shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        // *** Initialise the framework services
        FrameworkInitialisation frameworkInitialisation = null;
        try {
            frameworkInitialisation = new FrameworkInitialisation(bootstrapProperties, overrideProperties);
        } catch (Exception e) {
            throw new FrameworkException("Unable to initialise the Framework Services", e);
        }
        IFramework framework = frameworkInitialisation.getFramework();

        IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService("framework");
        IDynamicStatusStoreService dss = framework.getDynamicStatusStoreService("framework");

        // *** Now start the Metrics Server framework

        logger.info("Starting Metrics Server");

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

        String threads = AbstractManager.nulled(cps.getProperty("metrics", "threads"));
        if (threads != null) {
            numberOfRunThreads = Integer.parseInt(threads);
        }

        String port = AbstractManager.nulled(cps.getProperty("metrics", "port"));
        if (port != null) {
            metricsPort = Integer.parseInt(port);
        }

        port = AbstractManager.nulled(cps.getProperty("metrics.health", "port"));
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

        this.successfulPollsCounter = Counter.build().name("galasa_metric_successfull_polls")
                .help("The number of successfull metrics pools").register();

        // *** Create Health Server
        if (healthPort > 0) {
            this.healthServer = new MetricsServerHealth(this, healthPort);
            logger.info("Health monitoring on port " + healthPort);
        } else {
            logger.info("Health monitoring disabled");
        }

        // *** Locate all the Metrics providers in the framework
        try {
            final ServiceReference<?>[] mpServiceReference = bundleContext
                    .getAllServiceReferences(IMetricsProvider.class.getName(), null);
            if ((mpServiceReference == null) || (mpServiceReference.length == 0)) {
                logger.info("No additional Metrics providers have been found");
            } else {
                for (final ServiceReference<?> mpReference : mpServiceReference) {
                    final IMetricsProvider mpStoreRegistration = (IMetricsProvider) bundleContext
                            .getService(mpReference);
                    try {
                        if (mpStoreRegistration.initialise(framework, this)) {
                            logger.info("Found Metrics Provider " + mpStoreRegistration.getClass().getName());
                            metricsProviders.add(mpStoreRegistration);
                        } else {
                            logger.info("Metrics Provider " + mpStoreRegistration.getClass().getName()
                                    + " opted out of this Metrics run");
                        }
                    } catch (Exception e) {
                        logger.error("Failed initialisation of Metrics Provider "
                                + mpStoreRegistration.getClass().getName() + " ignoring", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new FrameworkException("Problem during Metrics Server initialisation", e);
        }

        // *** Start the providers
        for (IMetricsProvider provider : metricsProviders) {
            provider.start();
        }
        
        logger.info("Metrics Server has started");

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
        this.scheduledExecutorService.shutdown();
        try {
            this.scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unable to shutdown the scheduler");
        }

        // *** shutdown the providers
        for (IMetricsProvider provider : metricsProviders) {
            logger.info("Requesting Metrics Management Provider " + provider.getClass().getName() + " shutdown");
            provider.shutdown();
        }

        // *** Stop the metics server
        if (metricsPort > 0) {
            this.metricsServer.stop();
        }

        // *** Stop the health server
        if (healthPort > 0) {
            this.healthServer.shutdown();
        }

        logger.info("Metrics Server shutdown");
        shutdownComplete = true;
        return;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return this.scheduledExecutorService;
    }

    @Override
    public synchronized void metricsPollSuccessful() {
        if (this.successfulPollsSinceLastHealthCheck == Integer.MAX_VALUE) {
            this.successfulPollsSinceLastHealthCheck = 0;
        }
        this.successfulPollsSinceLastHealthCheck++;

        this.successfulPollsCounter.inc();
    }

    protected synchronized long getSuccessfulPollsSinceLastHealthCheck() {
        long lastCount = this.successfulPollsSinceLastHealthCheck;
        this.successfulPollsSinceLastHealthCheck = 0;

        return lastCount;
    }

    private void updateHeartbeat(IDynamicStatusStoreService dss) {
        Instant time = Instant.now();

        HashMap<String, String> props = new HashMap<>();
        props.put("servers.metricsserver." + serverName + ".heartbeat", time.toString());
        props.put("servers.metricsserver." + serverName + ".hostname", hostname);

        try {
            dss.put(props);
        } catch (DynamicStatusStoreException e) {
            logger.error("Problem logging heartbeat", e);
        }
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            MetricsServer.this.logger.info("Shutdown request received");
            MetricsServer.this.shutdown = true;

            while (!shutdownComplete) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    MetricsServer.this.logger.info("Shutdown wait was interrupted", e);
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Activate
    public void activate(BundleContext context) {
        this.bundleContext = context;
    }

}