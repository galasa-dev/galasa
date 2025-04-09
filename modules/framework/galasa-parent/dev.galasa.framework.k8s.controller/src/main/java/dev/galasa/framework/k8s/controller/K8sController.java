/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.prometheus.client.exporter.HTTPServer;

@Component(service = { K8sController.class })
public class K8sController {

    private Log                      logger           = LogFactory.getLog(this.getClass());

    // Note: These two flags are shared-state between two threads, so must be marked volatile.
    private volatile boolean shutdown         = false;
    private volatile boolean shutdownComplete = false;

    private boolean                  controllerRunning = false;

    private ScheduledExecutorService scheduledExecutorService;

    private HTTPServer               metricsServer;

    private Health                   healthServer;

    private TestPodScheduler podScheduler;
    private ScheduledFuture<?> pollFuture;

    private RunPodCleanup runCleanup;

    private ScheduledFuture<?> cleanupFuture;

    private Settings settings;

    public void run(Properties bootstrapProperties, Properties overrideProperties) throws FrameworkException {

        // *** Add shutdown hook to allow for orderly shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        try {

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

            // *** Now start the Kubernetes Controller framework

            logger.info("Starting Kubernetes Controller");

            // *** Create the API to k8s

            ApiClient client;
            try {
                client = Config.defaultClient();
            } catch (IOException e) {
                throw new FrameworkException("Unable to load Kubernetes API", e);
            }
            Configuration.setDefaultApiClient(client);
            IKubernetesProtoClient protoClient = new KubernetesProtoClient(client);
            CoreV1Api api = new CoreV1Api();

            // *** Fetch the settings

            settings = new Settings(this, api);
            settings.init();

            // *** Setup defaults and properties

            int metricsPort = 9010;
            int healthPort = 9011;

            String port = nulled(cps.getProperty("controller.metrics", "port"));
            if (port != null) {
                metricsPort = Integer.parseInt(port);
            }

            port = nulled(cps.getProperty("controller.health", "port"));
            if (port != null) {
                healthPort = Integer.parseInt(port);
            }

            // *** Setup scheduler
            scheduledExecutorService = new ScheduledThreadPoolExecutor(3);

            // *** Start the heartbeat
            scheduledExecutorService.scheduleWithFixedDelay(new Heartbeat(dss, settings), 0, 20, TimeUnit.SECONDS);
            // *** Start the settings poll
            scheduledExecutorService.scheduleWithFixedDelay(settings, 20, 20, TimeUnit.SECONDS);

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

            // *** Create Health Server
            if (healthPort > 0) {
                this.healthServer = new Health(healthPort);
                logger.info("Health monitoring on port " + healthPort);
            } else {
                logger.info("Health monitoring disabled");
            }
            // *** Start the run polling
            runCleanup = new RunPodCleanup(settings, api, protoClient, framework.getFrameworkRuns());
            schedulePodCleanup();
            podScheduler = new TestPodScheduler(dss, cps, settings, api, framework.getFrameworkRuns());
            schedulePoll();

            
            logger.info("Kubernetes controller has started");

            // *** Loop until we are asked to shutdown
            controllerRunning = true;
            while (!shutdown) {
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

            // *** Stop the metics server
            if (metricsPort > 0) {
                this.metricsServer.close();
            }

            // *** Stop the health server
            if (healthPort > 0) {
                this.healthServer.shutdown();
            }
        } finally {
            logger.info("Kubernetes Controller shutdown");
            // This allows the shutdown hook to exit.
            shutdownComplete = true;
        }

        return;

    }

    private void schedulePoll() {
        if (pollFuture != null) {
            this.pollFuture.cancel(false);
        }
        
        pollFuture = scheduledExecutorService.scheduleWithFixedDelay(podScheduler, 1, settings.getPoll(), TimeUnit.SECONDS);
    }

    private void schedulePodCleanup() {
        if (cleanupFuture != null) {
            this.cleanupFuture.cancel(false);
        }
        
        cleanupFuture = scheduledExecutorService.scheduleWithFixedDelay(runCleanup, 0, settings.getPoll(), TimeUnit.SECONDS);
    }

    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            K8sController.this.logger.info("Shutdown request received");
            
            // Tell the main thread to shut down via this shared variable.
            K8sController.this.shutdown = true;

            while (!shutdownComplete && controllerRunning) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    K8sController.this.logger.info("Shutdown wait was interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * null a String is if it is empty
     * 
     * TODO Needs to be moved to a more appropriate place as non managers use this,
     * a stringutils maybe
     * 
     * @param value
     * @return a trimmed String or a null if emtpy or null
     */
    public static String nulled(String value) {
        if (value == null) {
            return null;
        }

        value = value.trim();
        if (value.isEmpty()) {
            return value;
        }
        return value;
    }

    public void pollUpdated() {
        if (pollFuture == null) {
            return;
        }
        
        schedulePoll();
        schedulePodCleanup();
    }

}