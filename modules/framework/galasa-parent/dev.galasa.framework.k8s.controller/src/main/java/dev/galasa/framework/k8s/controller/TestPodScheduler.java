/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1NodeAffinity;
import io.kubernetes.client.openapi.models.V1NodeSelectorRequirement;
import io.kubernetes.client.openapi.models.V1NodeSelectorTerm;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.prometheus.client.Counter;

public class TestPodScheduler implements Runnable {

    private static final String RAS_TOKEN_ENV = "GALASA_RAS_TOKEN";
    private static final String EVENT_TOKEN_ENV = "GALASA_EVENT_STREAMS_TOKEN";

    private static final String ENCRYPTION_KEYS_PATH_ENV = FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV;
    public static final String ENCRYPTION_KEYS_VOLUME_NAME = "encryption-keys";

    private static final String CPS_ENV_VAR   = "GALASA_CONFIG_STORE";
    private static final String DSS_ENV_VAR   = "GALASA_DYNAMICSTATUS_STORE";
    private static final String CREDS_ENV_VAR = "GALASA_CREDENTIALS_STORE";

    private final Log                        logger           = LogFactory.getLog(getClass());

    private final Settings                   settings;
    private final CoreV1Api                  api;
    private final IDynamicStatusStoreService dss;
    private final IFrameworkRuns             runs;
    private final QueuedComparator           queuedComparator = new QueuedComparator();

    private Counter                          submittedRuns;
    private Environment                      env              = new SystemEnvironment();
    private CPSFacade cpsFacade ;


    public TestPodScheduler(IDynamicStatusStoreService dss, IConfigurationPropertyStoreService cps, Settings settings, CoreV1Api api, IFrameworkRuns runs) {
        this(new SystemEnvironment(), dss, cps, settings, api, runs);
    }

    public TestPodScheduler(Environment env, IDynamicStatusStoreService dss, IConfigurationPropertyStoreService cps, Settings settings, CoreV1Api api, IFrameworkRuns runs) {
        this.env = env;
        this.settings = settings;
        this.api = api;
        this.runs = runs;
        this.dss = dss;

        // *** Create metrics

        this.submittedRuns = Counter.build().name("galasa_k8s_controller_submitted_runs")
                .help("The number of runs submitted by the Kubernetes controller").register();

        this.cpsFacade = new CPSFacade(cps);
    }

    @Override
    public void run() {
        logger.info("Looking for new runs");

        try {
            // *** No we are not, get all the queued runs
            List<IRun> queuedRuns = this.runs.getQueuedRuns();
            // TODO filter by capability

            // *** Remove all the local runs
            Iterator<IRun> queuedRunsIterator = queuedRuns.iterator();
            while (queuedRunsIterator.hasNext()) {
                IRun run = queuedRunsIterator.next();
                if (run.isLocal()) {
                    queuedRunsIterator.remove();
                }
            }

            if (queuedRuns.isEmpty()) {
                logger.info("There are no queued runs");
                return;
            }

            while (true) {
                // *** Check we are not at max engines
                List<V1Pod> pods = getPods(this.api, this.settings);
                
                // Do not filter out active runs. Completed runs still consume memory. 
                // We should limit active+inactive runs to the max threashold.

                logger.info("Active runs=" + pods.size() + ",max=" + settings.getMaxEngines());

                int currentActive = pods.size();
                if (currentActive >= settings.getMaxEngines()) {
                    logger.info(
                            "Not looking for runs, currently at maximim engines (" + settings.getMaxEngines() + ")");
                    return;
                }

                // List<IRun> activeRuns = this.runs.getActiveRuns();

                // TODO Create the group algorithim same as the galasa scheduler

                // *** Build pool lists
                // HashMap<String, Pool> queuePools = getPools(queuedRuns);
                // HashMap<String, Pool> activePools = getPools(activeRuns);

                // *** cheat for the moment
                Collections.sort(queuedRuns, queuedComparator);

                IRun selectedRun = queuedRuns.remove(0);

                startPod(selectedRun);

                if (!queuedRuns.isEmpty()) {
                    // Slight delay to allow Kubernetes to catch up....
                    //
                    // Why do this ? 
                    //
                    // If we don't do this, then all the tests get scheduled on the same node, and the 
                    // node will run out of memory.
                    //
                    // We assume that's because the usage statistics on a pod are not synchronized totally at
                    // real-time, but have a lag in which they catch up. Hopefully this delay is greater
                    // than the lag and when we actually schedule the next pod it gets evenly distributed over
                    // the nodes which are available.
                    //
                    // This may or may not be necessary if the scheduling policies in the cluster are changed. Not sure.
                    long launchIntervalMilliseconds = cpsFacade.getKubeLaunchIntervalMilliseconds();
                    Thread.sleep(launchIntervalMilliseconds); 
                } else {
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Unable to poll for new runs", e);
        }

        return;
    }

    private void startPod(IRun run) {
        String runName = run.getName();
        String engineName = this.settings.getEngineLabel() + "-" + runName.toLowerCase();
        String namespace = this.settings.getNamespace();

        logger.info("Received run " + runName);

        try {
            // *** First attempt to allocate the run to this controller
            Instant now = Instant.now();
            Instant expire = now.plus(15, ChronoUnit.MINUTES);
            HashMap<String, String> props = new HashMap<>();
            props.put("run." + runName + ".controller", settings.getPodName());
            props.put("run." + runName + ".allocated", now.toString());
            props.put("run." + runName + ".allocate.timeout", expire.toString());
            if (!this.dss.putSwap("run." + runName + ".status", "queued", "allocated", props)) {
                logger.info("run allocated by another controller");
                return;
            }

            V1Pod newPod = createTestPod(runName, engineName, run.isTrace());

            boolean successful = false;
            int retry = 0;
            while (!successful) {
                try {
                    // System.out.println(newPod.toString());
                    api.createNamespacedPod(namespace, newPod).pretty("true").execute();

                    logger.info("Engine Pod " + newPod.getMetadata().getName() + " started");
                    successful = true;
                    submittedRuns.inc();
                    break;
                } catch (ApiException e) {
                    String response = e.getResponseBody();
                    if (response != null) {
                        if (response.contains("AlreadyExists")) {
                            retry++;
                            String newEngineName = engineName + "-" + retry;
                            newPod.getMetadata().setName(newEngineName);
                            logger.info("Engine Pod " + engineName + " already exists, trying with " + newEngineName);
                            continue;
                        } else {
                            logger.error("Failed to create engine pod :-\n" + e.getResponseBody(), e);
                        }
                    } else {
                        logger.error("k8s api exception received without response body, will retry later",e);
                    }
                } catch (Exception e) {
                    logger.error("Failed to create engine pod", e);
                }
                logger.info("Waiting 2 seconds before trying to create pod again");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("Failed to start new engine", e);
        }
    }

    V1Pod createTestPod(String runName, String engineName, boolean isTraceEnabled) {
        V1Pod newPod = new V1Pod();
        newPod.setApiVersion("v1");
        newPod.setKind("Pod");

        V1ObjectMeta metadata = new V1ObjectMeta();
        newPod.setMetadata(metadata);
        metadata.setName(engineName);
        metadata.putLabelsItem("galasa-engine-controller", this.settings.getEngineLabel());
        metadata.putLabelsItem("galasa-run", runName);

        V1PodSpec podSpec = new V1PodSpec();
        newPod.setSpec(podSpec);
        podSpec.setOverhead(null);
        podSpec.setRestartPolicy("Never");

        String nodeArch = this.settings.getNodeArch();
        if (!nodeArch.isEmpty()) {
            HashMap<String, String> nodeSelector = new HashMap<>();
            nodeSelector.put("kubernetes.io/arch", nodeArch);
            podSpec.setNodeSelector(nodeSelector);
        }

        String nodePreferredAffinity = this.settings.getNodePreferredAffinity();
        if (!nodePreferredAffinity.isEmpty()) {
            String[] selection = nodePreferredAffinity.split("=");
            if (selection.length == 2) {
                V1Affinity affinity = new V1Affinity();
                podSpec.setAffinity(affinity);

                V1NodeAffinity nodeAffinity = new V1NodeAffinity();
                affinity.setNodeAffinity(nodeAffinity);

                V1PreferredSchedulingTerm preferred = new V1PreferredSchedulingTerm();
                nodeAffinity.addPreferredDuringSchedulingIgnoredDuringExecutionItem(preferred);
                preferred.setWeight(1);

                V1NodeSelectorTerm selectorTerm = new V1NodeSelectorTerm();
                preferred.setPreference(selectorTerm);

                V1NodeSelectorRequirement requirement = new V1NodeSelectorRequirement();
                selectorTerm.addMatchExpressionsItem(requirement);
                requirement.setKey(selection[0]);
                requirement.setOperator("In");
                requirement.addValuesItem(selection[1]);


            }
        }

        String nodeTolerations = this.settings.getNodeTolerations();
        if(!nodeTolerations.isEmpty()) {
            List<V1Toleration> tolerationsList = createNodeTolerations(nodeTolerations);
            for(V1Toleration thisToleration : tolerationsList) {
                podSpec.addTolerationsItem(thisToleration);
            }
        }

        podSpec.setVolumes(createTestPodVolumes());
        podSpec.addContainersItem(createTestContainer(runName, engineName, isTraceEnabled));
        return newPod;
    }


    /*
    * Tolerations are supplied as a string in the form:
    * "node-label1=Operator1:Condition1,node-label2=Operator2:Condition2"
    *
    * For example: "galasa-engines=Exists:NoSchedule"
    *
    * The following method parses the String comma separated list of node
    * tolerations and returns a list of K8s V1Tolerations.
    */
    private List<V1Toleration> createNodeTolerations(String nodeTolerations) {
        List<V1Toleration> tolerationsList = new ArrayList<>();

        String[] tolerationStringSplit = nodeTolerations.split(",");

        if(tolerationStringSplit.length > 0) {
            for(int i = 0; i < tolerationStringSplit.length; i++){
                String[] selection = tolerationStringSplit[i].split("=");

                if (selection.length == 2) {
                    String[] operatorAndEffect = selection[1].split(":");

                    if(operatorAndEffect.length == 2) {
                        V1Toleration toleration = new V1Toleration();
                        logger.info("Adding toleration: " + selection[0] + ", operator: " + operatorAndEffect[0] + ", effect: " + operatorAndEffect[1]);
                        toleration.setKey(selection[0]);
                        toleration.setOperator(operatorAndEffect[0]);
                        toleration.setEffect(operatorAndEffect[1]);
                        tolerationsList.add(toleration);
                    }
                    else {
                        logger.error("Failed to retrieve operator and effect for toleration condition :-\n" + selection[0]);
                    }
                }
                else {
                    logger.error("Badly formatted toleration");
                }
            }
        }
        return tolerationsList;
    }


    private V1Container createTestContainer(String runName, String engineName, boolean isTraceEnabled) {
        V1Container container = new V1Container();
        container.setName("engine");
        container.setImage(this.settings.getEngineImage());
        container.setImagePullPolicy("Always"); // TODO parameterise

        ArrayList<String> commands = new ArrayList<>();
        container.setCommand(commands);
        commands.add("java");

        ArrayList<String> args = createCommandLineArgs(settings, runName, isTraceEnabled);
        container.setArgs(args);

        V1ResourceRequirements resources = new V1ResourceRequirements();
        container.setResources(resources);

        logger.info("requests=" + Integer.toString(this.settings.getEngineMemoryRequestMegabytes()) + "Mi");
        resources.putRequestsItem("memory", new Quantity( Integer.toString(this.settings.getEngineMemoryRequestMegabytes()) + "Mi"));

        logger.info("limit=" + Integer.toString(this.settings.getEngineMemoryLimitMegabytes()) + "Mi");
        resources.putLimitsItem("memory", new Quantity(Integer.toString(this.settings.getEngineMemoryLimitMegabytes()) + "Mi"));

        if (this.settings.getEngineCPURequestM() <= 0) {
            logger.info("No requested CPU requirements set");
        } else {
            logger.info("requests=" + Integer.toString(this.settings.getEngineCPURequestM()) + "m");
            resources.putRequestsItem("cpu", new Quantity( Integer.toString(this.settings.getEngineCPURequestM()) + "m"));
        }

        if (this.settings.getEngineCPULimitM() <= 0 ) {
            logger.info("No maximum CPU requirements set");
        } else {
            logger.info("limit=" + Integer.toString(this.settings.getEngineCPULimitM()) + "m");
            resources.putLimitsItem("cpu", new Quantity( Integer.toString(this.settings.getEngineCPULimitM()) + "m"));
        }

        container.setVolumeMounts(createTestContainerVolumeMounts());
        container.setEnv(createTestContainerEnvVariables());
        return container;
    }

    // This method is protected so we can easily unit test it.
    protected ArrayList<String> createCommandLineArgs(Settings settings, String runName, boolean isTraceEnabled) {
        
        ArrayList<String> args = new ArrayList<>();
        
        // Set the max heap size for the test pod...
        if (settings.getEngineMemoryHeapSizeMegabytes() != 0 ) {
            args.add("-Xmx:"+Integer.toString(settings.getEngineMemoryHeapSizeMegabytes())+"m");
        }

        args.add("-jar");
        args.add("boot.jar");
        args.add("--obr");
        args.add("file:galasa.obr");
        args.add("--bootstrap");
        args.add(settings.getBootstrap());
        args.add("--run");
        args.add(runName);
        if (isTraceEnabled) {
            args.add("--trace");
        }
        return args ;
    }

    private List<V1Volume> createTestPodVolumes() {
        List<V1Volume> volumes = new ArrayList<>();

        V1Volume encryptionKeysVolume = new V1Volume();
        encryptionKeysVolume.setName(ENCRYPTION_KEYS_VOLUME_NAME);

        V1SecretVolumeSource encryptionKeysSecretSource = new V1SecretVolumeSource();
        encryptionKeysSecretSource.setSecretName(this.settings.getEncryptionKeysSecretName());

        encryptionKeysVolume.setSecret(encryptionKeysSecretSource);
        volumes.add(encryptionKeysVolume);
        return volumes;
    }

    private List<V1VolumeMount> createTestContainerVolumeMounts() {
        List<V1VolumeMount> volumeMounts = new ArrayList<>();

        String encryptionKeysMountPath = env.getenv(ENCRYPTION_KEYS_PATH_ENV);
        if (encryptionKeysMountPath != null && !encryptionKeysMountPath.isBlank()) {
            Path encryptionKeysDirectory = Paths.get(encryptionKeysMountPath).getParent().toAbsolutePath();

            V1VolumeMount encryptionKeysVolumeMount = new V1VolumeMount();
            encryptionKeysVolumeMount.setName(ENCRYPTION_KEYS_VOLUME_NAME);
            encryptionKeysVolumeMount.setMountPath(encryptionKeysDirectory.toString());
            encryptionKeysVolumeMount.setReadOnly(true);

            volumeMounts.add(encryptionKeysVolumeMount);
        }
        return volumeMounts;
    }

    private List<V1EnvVar> createTestContainerEnvVariables() {
        ArrayList<V1EnvVar> envs = new ArrayList<>();
        // envs.add(createConfigMapEnv("GALASA_URL", configMapName, "galasa_url"));
        // envs.add(createConfigMapEnv("GALASA_INFRA_OBR", configMapName,
        // "galasa_maven_infra_obr"));
        // envs.add(createConfigMapEnv("GALASA_INFRA_REPO", configMapName,
        // "galasa_maven_infra_repo"));
        // envs.add(createConfigMapEnv("GALASA_TEST_REPO", configMapName,
        // "galasa_maven_test_repo"));
        // envs.add(createConfigMapEnv("GALASA_HELPER_REPO", configMapName,
        // "galasa_maven_helper_repo"));
        //
        // envs.add(createValueEnv("GALASA_ENGINE_TYPE", engineLabel));
        envs.add(createValueEnv("MAX_HEAP", Integer.toString(this.settings.getEngineMemoryHeapSizeMegabytes()) + "m"));
        envs.add(createValueEnv(RAS_TOKEN_ENV, env.getenv(RAS_TOKEN_ENV)));
        envs.add(createValueEnv(EVENT_TOKEN_ENV, env.getenv(EVENT_TOKEN_ENV)));
        envs.add(createValueEnv(ENCRYPTION_KEYS_PATH_ENV, env.getenv(ENCRYPTION_KEYS_PATH_ENV)));
        String cpsEnvValue = env.getenv(CPS_ENV_VAR);
        if (cpsEnvValue != null && !cpsEnvValue.isBlank()) {
            envs.add(createValueEnv(CPS_ENV_VAR, cpsEnvValue));
        }
        String dssEnvValue = env.getenv(DSS_ENV_VAR);
        if (dssEnvValue != null && !dssEnvValue.isBlank()) {
            envs.add(createValueEnv(DSS_ENV_VAR, dssEnvValue));
        }
        String credsEnvValue = env.getenv(CREDS_ENV_VAR);
        if (credsEnvValue != null && !credsEnvValue.isBlank()) {
            envs.add(createValueEnv(CREDS_ENV_VAR, credsEnvValue));
        }        
        //
        // envs.add(createSecretEnv("GALASA_SERVER_USER", "galasa-secret",
        // "galasa-server-username"));
        // envs.add(createSecretEnv("GALASA_SERVER_PASSWORD", "galasa-secret",
        // "galasa-server-password"));
        // envs.add(createSecretEnv("GALASA_MAVEN_USER", "galasa-secret",
        // "galasa-maven-username"));
        // envs.add(createSecretEnv("GALASA_MAVEN_PASSWORD", "galasa-secret",
        // "galasa-maven-password"));
        //
        // envs.add(createValueEnv("GALASA_RUN_ID", runUUID.toString()));
        // envs.add(createFieldEnv("GALASA_ENGINE_ID", "metadata.name"));
        // envs.add(createFieldEnv("GALASA_K8S_NODE", "spec.nodeName"));
        return envs;
    }

    public static @NotNull List<V1Pod> getPods(CoreV1Api api, Settings settings) throws K8sControllerException {
        LinkedList<V1Pod> pods = new LinkedList<>();

        try {
            V1PodList list = api.listNamespacedPod(settings.getNamespace())
                .labelSelector("galasa-engine-controller=" + settings.getEngineLabel())
                .execute();

            for (V1Pod pod : list.getItems()) {
                pods.add(pod);
            }
        } catch (Exception e) {
            throw new K8sControllerException("Failed retrieving pods", e);
        }

        return pods;
    }

    public static void filterActiveRuns(@NotNull List<V1Pod> pods) {
        Iterator<V1Pod> iPod = pods.iterator();
        while (iPod.hasNext()) {
            V1Pod pod = iPod.next();
            V1PodStatus status = pod.getStatus();

            if (status != null) {
                String phase = status.getPhase();
                if ("failed".equalsIgnoreCase(phase)) {
                    iPod.remove();
                } else if ("succeeded".equalsIgnoreCase(phase)) {
                    iPod.remove();
                }
            }
        }
    }

    public static void filterTerminated(@NotNull List<V1Pod> pods) {
        Iterator<V1Pod> iPod = pods.iterator();
        while (iPod.hasNext()) {
            V1Pod pod = iPod.next();
            V1PodStatus status = pod.getStatus();

            if (status != null) {
                String phase = status.getPhase();
                if ("failed".equalsIgnoreCase(phase)) {
                    continue;
                } else if ("succeeded".equalsIgnoreCase(phase)) {
                    continue;
                }
            }
            iPod.remove();
        }
    }

    private static class QueuedComparator implements Comparator<IRun> {

        @Override
        public int compare(IRun o1, IRun o2) {
            return o1.getQueued().compareTo(o2.getQueued());
        }

    }

    private V1EnvVar createValueEnv(String name, String value) {
        V1EnvVar env = new V1EnvVar();
        env.setName(name);
        env.setValue(value);

        return env;
    }

}