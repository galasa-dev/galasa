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
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1ConfigMapKeySelector;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1EnvVarSource;
import io.kubernetes.client.openapi.models.V1NodeAffinity;
import io.kubernetes.client.openapi.models.V1NodeSelectorRequirement;
import io.kubernetes.client.openapi.models.V1NodeSelectorTerm;
import io.kubernetes.client.openapi.models.V1ObjectFieldSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1SecretKeySelector;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.prometheus.client.Counter;

public class TestPodScheduler implements Runnable {

    private static final String RAS_TOKEN_ENV = "GALASA_RAS_TOKEN";
    private static final String EVENT_TOKEN_ENV = "GALASA_EVENT_STREAMS_TOKEN";

    private static final String ENCRYPTION_KEYS_PATH_ENV = FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV;
    public static final String ENCRYPTION_KEYS_VOLUME_NAME = "encryption-keys";

    private final Log                        logger           = LogFactory.getLog(getClass());

    private final Settings                   settings;
    private final CoreV1Api                  api;
    private final IDynamicStatusStoreService dss;
    private final IFrameworkRuns             runs;
    private final QueuedComparator           queuedComparator = new QueuedComparator();

    private Counter                          submittedRuns;
    private Environment                      env              = new SystemEnvironment();


    public TestPodScheduler(IDynamicStatusStoreService dss, Settings settings, CoreV1Api api, IFrameworkRuns runs) {
        this(new SystemEnvironment(), dss, settings, api, runs);
    }

    public TestPodScheduler(Environment env, IDynamicStatusStoreService dss, Settings settings, CoreV1Api api, IFrameworkRuns runs) {
        this.env = env;
        this.settings = settings;
        this.api = api;
        this.runs = runs;
        this.dss = dss;

        // *** Create metrics

        this.submittedRuns = Counter.build().name("galasa_k8s_controller_submitted_runs")
                .help("The number of runs submitted by the Kubernetes controller").register();

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
                filterActiveRuns(pods);
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
                    Thread.sleep(600); // *** Slight delay to allow Kubernetes to catch up
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
        podSpec.setRestartPolicy("Never");

        String nodeArch = this.settings.getNodeArch();
        if (!nodeArch.isEmpty()) {
            HashMap<String, String> nodeSelector = new HashMap<>();
            nodeSelector.put("beta.kubernetes.io/arch", nodeArch);
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

        podSpec.setVolumes(createTestPodVolumes());
        podSpec.addContainersItem(createTestContainer(runName, engineName, isTraceEnabled));
        return newPod;
    }

    private V1Container createTestContainer(String runName, String engineName, boolean isTraceEnabled) {
        V1Container container = new V1Container();
        container.setName("engine");
        container.setImage(this.settings.getEngineImage());
        container.setImagePullPolicy("Always"); // TODO parameterise

        ArrayList<String> commands = new ArrayList<>();
        container.setCommand(commands);
        commands.add("java");

        ArrayList<String> args = new ArrayList<>();
        container.setArgs(args);
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

        V1ResourceRequirements resources = new V1ResourceRequirements();
        container.setResources(resources);

        // TODO reinstate
        // System.out.println("requests=" +
        // Integer.toString(this.settings.getEngineMemoryRequest()) + "Mi");
        // System.out.println("limit=" +
        // Integer.toString(this.settings.getEngineMemoryLimit()) + "Mi");
        // resources.putRequestsItem("memory", new
        // Quantity(Integer.toString(this.settings.getEngineMemoryRequest()) + "Mi"));
        // resources.putLimitsItem("memory", new
        // Quantity(Integer.toString(this.settings.getEngineMemoryLimit()) + "Mi"));

        container.setVolumeMounts(createTestContainerVolumeMounts());
        container.setEnv(createTestContainerEnvVariables());
        return container;
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
        envs.add(createValueEnv("MAX_HEAP", Integer.toString(this.settings.getEngineMemory()) + "m"));
        envs.add(createValueEnv(RAS_TOKEN_ENV, env.getenv(RAS_TOKEN_ENV)));
        envs.add(createValueEnv(EVENT_TOKEN_ENV, env.getenv(EVENT_TOKEN_ENV)));
        envs.add(createValueEnv(ENCRYPTION_KEYS_PATH_ENV, env.getenv(ENCRYPTION_KEYS_PATH_ENV)));
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

    private HashMap<String, Pool> getPools(@NotNull List<IRun> runs) {
        HashMap<String, Pool> pools = new HashMap<>();

        for (IRun run : runs) {
            String poolid = getPoolId(run);
            Pool pool = pools.get(poolid);
            if (pool == null) {
                pool = new Pool(poolid);
            }
            pool.runs.add(run);
        }

        return pools;
    }

    private String getPoolId(IRun run) {
        if (settings.getRequestorsByGroup().contains(run.getRequestor())) {
            return run.getRequestor() + "/" + run.getGroup();
        }

        return run.getRequestor();
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

    private static class Pool implements Comparable<Pool> {
        private String          id;
        private ArrayList<IRun> runs = new ArrayList<>();

        public Pool(String id) {
            this.id = id;
        }

        @Override
        public int compareTo(Pool o) {
            return runs.size() - o.runs.size();
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

    private V1EnvVar createConfigMapEnv(String name, String configMap, String key) {
        V1EnvVar env = new V1EnvVar();
        env.setName(name);

        V1EnvVarSource source = new V1EnvVarSource();
        env.setValueFrom(source);

        V1ConfigMapKeySelector configMapKeyRef = new V1ConfigMapKeySelector();
        source.setConfigMapKeyRef(configMapKeyRef);

        configMapKeyRef.setName(configMap);
        configMapKeyRef.setKey(key);

        return env;
    }

    private V1EnvVar createSecretEnv(String name, String secret, String key) {
        V1EnvVar env = new V1EnvVar();
        env.setName(name);

        V1EnvVarSource source = new V1EnvVarSource();
        env.setValueFrom(source);

        V1SecretKeySelector secretKeyRef = new V1SecretKeySelector();
        source.setSecretKeyRef(secretKeyRef);

        secretKeyRef.setName(secret);
        secretKeyRef.setKey(key);

        return env;
    }

    private V1EnvVar createFieldEnv(String name, String path) {
        V1EnvVar env = new V1EnvVar();
        env.setName(name);

        V1EnvVarSource source = new V1EnvVarSource();
        env.setValueFrom(source);

        V1ObjectFieldSelector fieldKeyRef = new V1ObjectFieldSelector();
        source.setFieldRef(fieldKeyRef);

        fieldKeyRef.setFieldPath(path);

        return env;
    }

}