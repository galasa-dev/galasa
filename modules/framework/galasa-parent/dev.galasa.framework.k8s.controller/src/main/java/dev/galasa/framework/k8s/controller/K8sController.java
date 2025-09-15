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
import dev.galasa.framework.k8s.controller.api.IKubernetesApiClient;
import dev.galasa.framework.k8s.controller.api.KubernetesApiClient;
import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.interruptedruns.RunInterruptHandler;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.utils.SystemTimeService;
import io.kubernetes.client.ProtoClient;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.prometheus.client.exporter.HTTPServer;

@Component(service = { K8sController.class })
public class K8sController {

    private static final int ENGINE_CONTROLLER_NUMBER_OF_THREADS = 5;
    private static final int INTERRUPTED_RUN_WATCH_POLL_INTERVAL_SECONDS = 5;

    private static final String GALASA_INSTALL_NAME_ENV_VAR = "GALASA_INSTALL_NAME";
    private static final String NAMESPACE_ENV_VAR = "NAMESPACE";
    private static final String PODNAME_ENV_VAR = "PODNAME";
    private static final String CONFIG_ENV_VAR = "CONFIG";

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

    private ITimeService timeService;

    private Environment env ;

    private Settings settings ;

    public K8sController() {
        this(new SystemTimeService(), new SystemEnvironment() );
    }

    public K8sController(ITimeService timeService, Environment env) {
        this.timeService = timeService ;
        this.env = env;
    }

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
            ProtoClient protoClient = new ProtoClient(client);
            CoreV1Api api = new CoreV1Api();

            // *** Fetch the settings
            String galasaServiceInstallName = getEnvironmentVariableOrDefault(GALASA_INSTALL_NAME_ENV_VAR, "");
            logger.info("Setting Galasa installation name to '" + galasaServiceInstallName + "'");
            String namespace = getEnvironmentVariableOrDefault(NAMESPACE_ENV_VAR, "default");
            logger.info("Setting Namespace to '" + namespace + "'");

            IKubernetesApiClient apiClient = new KubernetesApiClient(api, protoClient);
            KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(apiClient, namespace, galasaServiceInstallName);

            String podName = getEnvironmentVariableOrDefault(PODNAME_ENV_VAR, "k8s-controller");
            logger.info("Setting Pod Name to '" + podName + "'");

            String configMapName = getEnvironmentVariableOrDefault(CONFIG_ENV_VAR, "config");
            logger.info("Setting ConfigMap to '" + configMapName + "'");

            settings = new Settings(this, kubeEngineFacade, podName , configMapName);
            settings.init();

            // *** Setup defaults and properties

            int metricsPort = getMetricsPort(cps);
            int healthPort = getHealthPort(cps);

            // *** Setup scheduler
            scheduledExecutorService = new ScheduledThreadPoolExecutor(ENGINE_CONTROLLER_NUMBER_OF_THREADS);

            // *** Start the heartbeat
            scheduledExecutorService.scheduleWithFixedDelay(new Heartbeat(dss, settings), 0, 20, TimeUnit.SECONDS);
            // *** Start the settings poll
            scheduledExecutorService.scheduleWithFixedDelay(settings, Settings.REFRESH_DELAY_SECS_INITIAL , Settings.REFRESH_DELAY_SECS, TimeUnit.SECONDS);

            // *** Start the metrics server
            this.metricsServer = startMetricsServer(metricsPort);

            // *** Create metrics
            // DefaultExports.initialize() - problem within the the exporter at the moment
            // TODO

            // *** Create Health Server
            this.healthServer = createHealthServer(healthPort);

            // *** Start the run polling
            IFrameworkRuns frameworkRuns = framework.getFrameworkRuns();
            IResultArchiveStore ras = framework.getResultArchiveStore();
            startRunPollingThreads(frameworkRuns, cps, dss, ras, kubeEngineFacade, settings);
            
            
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
            shutdownExecutorService();

            // *** Stop the metics server
            stopMetricsServer(metricsPort);

            // *** Stop the health server
            stopHealthServer(healthPort);

        } finally {
            logger.info("Kubernetes Controller shutdown");
            // This allows the shutdown hook to exit.
            shutdownComplete = true;
        }

        return;

    }

    private String getEnvironmentVariableOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        return value.trim();
    }

    private void startRunPollingThreads(
        IFrameworkRuns frameworkRuns,
        IConfigurationPropertyStoreService cps,
        IDynamicStatusStoreService dss,
        IResultArchiveStore ras,
        KubernetesEngineFacade kubeEngineFacade,
        Settings settings
    ) throws FrameworkException {

        runCleanup = new RunPodCleanup(kubeEngineFacade, frameworkRuns);
        schedulePodCleanup();

        podScheduler = new TestPodScheduler(env, dss, cps, settings, kubeEngineFacade, frameworkRuns, timeService);
        schedulePoll();
     
        RunInterruptHandler interruptedRunHandler = new RunInterruptHandler(kubeEngineFacade, frameworkRuns, settings, timeService, ras);
        scheduledExecutorService.scheduleWithFixedDelay(interruptedRunHandler, 0, INTERRUPTED_RUN_WATCH_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);


    }

    private void shutdownExecutorService() {
        this.scheduledExecutorService.shutdown();
        try {
            this.scheduledExecutorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Unable to shutdown the scheduler");
        }
    }

    private void stopHealthServer(int healthPort) {
        if (healthPort > 0) {
            this.healthServer.shutdown();
        }
    }

    private void stopMetricsServer(int metricsPort) {
        if (metricsPort > 0) {
            this.metricsServer.close();
        }
    }

    private Health createHealthServer(int healthPort) throws FrameworkException {
        Health healthServer = null;
        if (healthPort > 0) {
            healthServer = new Health(healthPort);
            logger.info("Health monitoring on port " + healthPort);
        } else {
            logger.info("Health monitoring disabled");
        }
        return healthServer;
    }

    private HTTPServer startMetricsServer(int metricsPort) throws FrameworkException {
        HTTPServer metricsServer = null;
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
        String port = nulled(cps.getProperty("controller.health", "port"));
        if (port != null) {
            healthPort = Integer.parseInt(port);
        }
        return healthPort;
    }

    private int getMetricsPort(IConfigurationPropertyStoreService cps) throws ConfigurationPropertyStoreException {
        int metricsPort = 9010;
        String port = nulled(cps.getProperty("controller.metrics", "port"));
        if (port != null) {
            metricsPort = Integer.parseInt(port);
        }
        return metricsPort;
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            this.pollFuture.cancel(false);
        }
        
        pollFuture = scheduledExecutorService.scheduleWithFixedDelay(podScheduler, 1, settings.getPollSeconds(), TimeUnit.SECONDS);
    }

    private void schedulePodCleanup() {
        if (cleanupFuture != null) {
            this.cleanupFuture.cancel(false);
        }
        
        cleanupFuture = scheduledExecutorService.scheduleWithFixedDelay(runCleanup, 0, settings.getPollSeconds(), TimeUnit.SECONDS);
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