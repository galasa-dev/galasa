/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.Pair;
import io.kubernetes.client.openapi.models.AuthenticationV1TokenRequest;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.CoreV1EventList;
import io.kubernetes.client.openapi.models.V1APIResourceList;
import io.kubernetes.client.openapi.models.V1Binding;
import io.kubernetes.client.openapi.models.V1ComponentStatus;
import io.kubernetes.client.openapi.models.V1ComponentStatusList;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1EndpointsList;
import io.kubernetes.client.openapi.models.V1Eviction;
import io.kubernetes.client.openapi.models.V1LimitRange;
import io.kubernetes.client.openapi.models.V1LimitRangeList;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1PersistentVolume;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodTemplate;
import io.kubernetes.client.openapi.models.V1PodTemplateList;
import io.kubernetes.client.openapi.models.V1ReplicationController;
import io.kubernetes.client.openapi.models.V1ReplicationControllerList;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.openapi.models.V1ResourceQuotaList;
import io.kubernetes.client.openapi.models.V1Scale;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.openapi.models.V1ServiceAccountList;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1Status;

import java.io.IOException;
import java.lang.reflect.Type;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

import org.junit.Test;
import org.junit.After;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import dev.galasa.framework.k8s.controller.mocks.MockISettings;
import dev.galasa.framework.k8s.controller.mocks.MockKubernetesApiClient;
import dev.galasa.framework.k8s.controller.mocks.MockSettings;
import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRun;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.mocks.MockFrameworkRuns;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api.APIcreateNamespacedPodRequest;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1NodeSelectorRequirement;
import io.kubernetes.client.openapi.models.V1NodeSelectorTerm;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.prometheus.client.CollectorRegistry;

public class TestPodSchedulerTest {


    private V1ConfigMap createMockConfigMap() {
        V1ConfigMap configMap = new V1ConfigMap();

        V1ObjectMeta metadata = new V1ObjectMeta().resourceVersion("mockVersion");
        configMap.setMetadata(metadata);

        Map<String, String> data = new HashMap<>();
        data.put("bootstrap", "http://my.server/bootstrap");
        data.put("max_engines", "10");
        data.put("engine_label", "my-test-engine");
        data.put("node_arch", "arch");
        data.put("run_poll", "5");
        data.put("encryption_keys_secret_name", "service-encryption-keys-secret");
        data.put("galasa_node_preferred_affinity", "galasa-engines=schedule");
        data.put("galasa_node_tolerations", "galasa-engines=Exists:NoSchedule,galasa-engines2=Exists:NoSchedule");
        configMap.setData(data);

        return configMap;
    }

    private void assertPodDetailsAreCorrect(
        V1Pod pod,
        String expectedRunName,
        String expectedPodName,
        String expectedEncryptionKeysMountPath,
        ISettings settings
    ) {
        checkPodMetadata(pod, expectedRunName, expectedPodName, settings);
        checkPodContainer(pod, expectedEncryptionKeysMountPath, settings);
        checkPodVolumes(pod, settings);
        checkPodSpec(pod, settings);
    }

    private void checkPodMetadata(V1Pod pod, String expectedRunName, String expectedPodName, ISettings settings) {
        V1ObjectMeta expectedMetadata = new V1ObjectMeta()
            .labels(Map.of("galasa-run", expectedRunName, "galasa-engine-controller", settings.getEngineLabel()))
            .name(expectedPodName);

        // Check the pod's metadata is as expected
        assertThat(pod).isNotNull();
        assertThat(pod.getApiVersion()).isEqualTo("v1");
        assertThat(pod.getKind()).isEqualTo("Pod");

        V1ObjectMeta actualMetadata = pod.getMetadata();
        assertThat(actualMetadata.getLabels()).containsExactlyInAnyOrderEntriesOf(expectedMetadata.getLabels());
        assertThat(actualMetadata.getName()).isEqualTo(expectedPodName);
    }

    private V1PreferredSchedulingTerm createSchedulingTerm(String nodePreferredAffinity) {

        String[] nodePreferredAffinitySplit = nodePreferredAffinity.split("=");

        V1PreferredSchedulingTerm preferred = new V1PreferredSchedulingTerm();
        preferred.setWeight(1);

        V1NodeSelectorTerm selectorTerm = new V1NodeSelectorTerm();
        preferred.setPreference(selectorTerm);

        V1NodeSelectorRequirement requirement = new V1NodeSelectorRequirement();
        selectorTerm.addMatchExpressionsItem(requirement);
        requirement.setKey(nodePreferredAffinitySplit[0]);
        requirement.setOperator("In");
        requirement.addValuesItem(nodePreferredAffinitySplit[1]);

        return preferred;
    }

    private void checkPodSpec(V1Pod pod, ISettings settings) {

        // Check the pod's spec is as expected
        V1PodSpec podSpec = pod.getSpec();
        assertThat(podSpec).isNotNull();

        // Check the podspec's node affinity is as expected
        V1PreferredSchedulingTerm preferredSchedulingTerm = createSchedulingTerm(settings.getNodePreferredAffinity());
        List<V1PreferredSchedulingTerm> terms = podSpec.getAffinity().getNodeAffinity().getPreferredDuringSchedulingIgnoredDuringExecution();

        assertThat(terms.contains(preferredSchedulingTerm));

        // Check the podspec's node tolerances are as expected
        String[] nodeTolerationsStringList = settings.getNodeTolerations().split(",");

        for(String nodeTolerationsString : nodeTolerationsStringList) {
            String[] tolerationParts = nodeTolerationsString.split("=");
            String tolerationKey = tolerationParts[0];
            String[] tolerationValueParts = tolerationParts[1].split(":");
            String tolerationOperator = tolerationValueParts[0];
            String tolerationEffect = tolerationValueParts[1];

            V1Toleration testToleration = new V1Toleration();

            testToleration.setKey(tolerationKey);
            testToleration.setOperator(tolerationOperator);
            testToleration.setEffect(tolerationEffect);

            assertThat(podSpec.getTolerations()).contains(testToleration);
        }
    }

    private void checkPodContainer(V1Pod pod, String expectedEncryptionKeysMountPath, ISettings settings) {
        // Check that test container has been added
        V1PodSpec actualPodSpec = pod.getSpec();
        List<V1Container> actualContainers = actualPodSpec.getContainers();
        assertThat(actualContainers).hasSize(1);

        V1Container testContainer = actualContainers.get(0);
        assertThat(testContainer.getCommand()).containsExactly("java");
        assertThat(testContainer.getArgs()).contains("-jar", "boot.jar", "--run");

        // Check that the encryption keys have been mounted to the correct location
        List<V1VolumeMount> testContainerVolumeMounts = testContainer.getVolumeMounts();
        assertThat(testContainerVolumeMounts).hasSize(1);

        V1VolumeMount encryptionKeysVolumeMount = testContainerVolumeMounts.get(0);
        assertThat(encryptionKeysVolumeMount.getName()).isEqualTo(TestPodScheduler.ENCRYPTION_KEYS_VOLUME_NAME);
        assertThat(encryptionKeysVolumeMount.getMountPath()).isEqualTo(expectedEncryptionKeysMountPath);
        assertThat(encryptionKeysVolumeMount.getReadOnly()).isTrue();
    }

    private void checkPodVolumes(V1Pod pod, ISettings settings) {
        // Check that the encryption keys volume has been added
        V1PodSpec actualPodSpec = pod.getSpec();
        List<V1Volume> actualVolumes = actualPodSpec.getVolumes();
        assertThat(actualVolumes).hasSize(1);

        V1Volume encryptionKeysVolume = actualVolumes.get(0);
        assertThat(encryptionKeysVolume.getName()).isEqualTo(TestPodScheduler.ENCRYPTION_KEYS_VOLUME_NAME);
        assertThat(encryptionKeysVolume.getSecret().getSecretName()).isEqualTo(settings.getEncryptionKeysSecretName());
    }

    @Test
    public void testCanCreateTestPodOk() throws Exception {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String encryptionKeysMountPath = "/encryption/encryption-keys.yaml";
        mockEnvironment.setenv(FrameworkEncryptionService.ENCRYPTION_KEYS_PATH_ENV, encryptionKeysMountPath);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));;

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPodDefinition(runName, podName, isTraceEnabled);

        // Then...
        String expectedEncryptionKeysMountPath = "/encryption";
        assertPodDetailsAreCorrect(pod, runName, podName, expectedEncryptionKeysMountPath, settings);
    }

    @Test
    public void testCanCreatePodWithOverriddenDSSOK() throws K8sControllerException {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String DSS_ENV_VAR = "GALASA_DYNAMICSTATUS_STORE";
        String customDssLocation = "etcd:http://myetcdstore-etcd:2379";

        mockEnvironment.setenv(DSS_ENV_VAR, customDssLocation);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPodDefinition(runName, podName, isTraceEnabled);

        // Then...
        V1EnvVar dssEnvVarObject = new V1EnvVar();
        dssEnvVarObject.setName(DSS_ENV_VAR);
        dssEnvVarObject.setValue(customDssLocation);

        List<V1EnvVar> envs = pod.getSpec().getContainers().get(0).getEnv();
        assertThat(envs).contains(dssEnvVarObject);
    }

    @Test
    public void testCanCreatePodWithOverriddenCPSOK() throws K8sControllerException {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String CPS_ENV_VAR = "GALASA_CONFIG_STORE";
        String customCpsLocation = "etcd:http://myetcdstore-etcd:2379";

        mockEnvironment.setenv(CPS_ENV_VAR, customCpsLocation);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();

        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPodDefinition(runName, podName, isTraceEnabled);

        // Then...
        V1EnvVar cpsEnvVarObject = new V1EnvVar();
        cpsEnvVarObject.setName(CPS_ENV_VAR);
        cpsEnvVarObject.setValue(customCpsLocation);

        List<V1EnvVar> envs = pod.getSpec().getContainers().get(0).getEnv();
        assertThat(envs).contains(cpsEnvVarObject);
    }

    @Test
    public void testCanCreatePodWithOverriddenCREDSOK() throws K8sControllerException {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String CREDS_ENV_VAR = "GALASA_CREDENTIALS_STORE";
        String customCredsLocation = "etcd:http://myetcdstore-etcd:2379";

        mockEnvironment.setenv(CREDS_ENV_VAR, customCredsLocation);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();

        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPodDefinition(runName, podName, isTraceEnabled);

        // Then...
        V1EnvVar credsEnvVarObject = new V1EnvVar();
        credsEnvVarObject.setName(CREDS_ENV_VAR);
        credsEnvVarObject.setValue(customCredsLocation);

        List<V1EnvVar> envs = pod.getSpec().getContainers().get(0).getEnv();
        assertThat(envs).contains(credsEnvVarObject);
    }

    @Test
    public void testCanCreatePodWithExtraBundles() throws K8sControllerException {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        String EXTRA_BUNDLES_ENV_VAR = "GALASA_EXTRA_BUNDLES";
        String extraBundles = "my.first.bundle,my.other.bundle";

        mockEnvironment.setenv(EXTRA_BUNDLES_ENV_VAR, extraBundles);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());


        List<V1Pod> mockPods = new ArrayList<V1Pod>();
        MockKubernetesApiClient api = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(api, "myNamespace");

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPodDefinition(runName, podName, isTraceEnabled);

        // Then...
        V1EnvVar extraBundlesEnvVar = new V1EnvVar();
        extraBundlesEnvVar.setName(EXTRA_BUNDLES_ENV_VAR);
        extraBundlesEnvVar.setValue(extraBundles);

        List<V1EnvVar> envs = pod.getSpec().getContainers().get(0).getEnv();
        assertThat(envs).contains(extraBundlesEnvVar);
    }

    @After
    public void clearCounters() {
        CollectorRegistry.defaultRegistry.clear();
    }

    public static final boolean TRACE_IS_ENABLED = true;

    @Test
    public void testMaxHeapSizeGetsSet() throws Exception {
        // Given...
        MockEnvironment mockEnvironment = new MockEnvironment();

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        List<V1Pod> mockPods = new ArrayList<V1Pod>();
        MockKubernetesApiClient api = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(api, "myNamespace");

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler podScheduler = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns, new MockTimeService(Instant.now()));

        // When...
        ArrayList<String> args = podScheduler.createCommandLineArgs(settings, "myRunName", TRACE_IS_ENABLED);

        // Then...
        assertThat(args).containsOnly(
        "-Xmx400m",
                "-jar",
                "boot.jar", 
                "--obr",
                "file:galasa.obr",
                "--run",
                "myRunName",
                "--trace"
        );

    }

    private MockRun createMockRun( String testRunName ) {
        // Create a mock run structure.
        String testBundleName = "my.bundleName";
        String testClassName = "my.className";
        String testStream = "myTestStream";
        String testStreamOBR = "myTestStreamOBR";
        String testStreamRepoUrl = "http://myTestStreamRepoUrl";
        String requestorName = "myRequestorName";
        boolean isRunLocal = false;

        MockRun run = new MockRun(testBundleName,testClassName, testRunName, testStream, testStreamOBR, testStreamRepoUrl, requestorName, isRunLocal);
        return run;
    }

    @Test
    public void testThatPodGetsScheduled() throws Exception {
        // Given...
        String testRunName = "U12345";
        MockRun run = createMockRun(testRunName);

        MockEnvironment mockEnvironment = new MockEnvironment();

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        mockDss.put("run."+testRunName+".status","queued");

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        List<V1Pod> mockPods = new ArrayList<V1Pod>();
        MockKubernetesApiClient api = new MockKubernetesApiClient(mockPods);
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(api, "myNamespace");

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);
    
        TestPodScheduler podScheduler = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, kubeEngineFacade, mockFrameworkRuns, new MockTimeService(Instant.now()));
        
        // // When...
        podScheduler.startPod(run);

        // Then...
        assertThat(api.podsLaunched).hasSize(1);
        assertThat(mockDss.get("run."+testRunName+".status")).isEqualTo("allocated");
    }


    @Test
    public void testThatPodGetsScheduledEvenIfPodWithSameNameExistsAlready() throws Exception {
        // Given...
        String testRunName = "U12345";
        MockRun run = createMockRun(testRunName);

        MockEnvironment mockEnvironment = new MockEnvironment();

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        mockDss.put("run."+testRunName+".status","queued");

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        // Simulating a pod which is already running...
        V1Pod alreadyRunningPod = new V1Pod();
        String engineName = testRunName ;

        List<V1Pod> mockPods = new ArrayList<V1Pod>();
        mockPods.add(alreadyRunningPod);
        MockKubernetesApiClient api = new MockKubernetesApiClient(mockPods);
        api.failToLaunchPodCount = 1; // Fail the first pod create with already exists!
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(api, "myNamespace");

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler podScheduler = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, kubeEngineFacade, mockFrameworkRuns, new MockTimeService(Instant.now()));
        
        // // When...
        podScheduler.startPod(run);

        // Then...
        assertThat(api.podsLaunched).hasSize(1);
        assertThat(api.podsFailedToLaunch).hasSize(1);

        // The code should have re-tried the launch, successfully launching the pod with a -1 suffix.
        assertThat(api.podsFailedToLaunch.get(0).getMetadata().getName()).isEqualTo("myEngineLabel-u12345");
        assertThat(api.podsLaunched.get(0).getMetadata().getName()).isEqualTo("myEngineLabel-u12345-1");
        assertThat(mockDss.get("run."+testRunName+".status")).isEqualTo("allocated");
    }
    
    

    @Test
    public void testThatPodCannotScheduleBecauseTooManyExistingPodsBeforeLaunchLimitReached() throws Exception {
        // Given...
        String testRunName = "U12345";
        MockRun run = createMockRun(testRunName);

        MockEnvironment mockEnvironment = new MockEnvironment();

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        mockDss.put("run."+testRunName+".status","queued");

        MockFrameworkRuns mockFrameworkRuns = new MockFrameworkRuns(new ArrayList<>());

        // Simulating a pod which is already running...
        V1Pod alreadyRunningPod = new V1Pod();
        String engineName = testRunName ;

        List<V1Pod> mockPods = new ArrayList<V1Pod>();
        mockPods.add(alreadyRunningPod);
        MockKubernetesApiClient api = new MockKubernetesApiClient(mockPods);
        api.failToLaunchPodCount = 50; // Always fail the pod create with already exists error!
        KubernetesEngineFacade kubeEngineFacade = new KubernetesEngineFacade(api, "myNamespace");

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockISettings settings = new MockISettings();
        settings.maxTestPodRetriesLimit = 5;
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler podScheduler = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, kubeEngineFacade, mockFrameworkRuns, new MockTimeService(Instant.now()));
        
        // // When...
        podScheduler.startPod(run);

        // Then...
        assertThat(api.podsLaunched).hasSize(0);
        assertThat(api.podsFailedToLaunch).hasSize(5);

        // The code should have re-tried the launch, successfully launching the pod with a -1 suffix.
        assertThat(api.podsFailedToLaunch.get(0).getMetadata().getName()).isEqualTo("myEngineLabel-u12345");
        assertThat(api.podsFailedToLaunch.get(1).getMetadata().getName()).isEqualTo("myEngineLabel-u12345-1");
        assertThat(mockDss.get("run."+testRunName+".status")).isEqualTo("finished");
        assertThat(mockDss.get("run."+testRunName+".result")).isEqualTo("EnvFail");
    }
}
