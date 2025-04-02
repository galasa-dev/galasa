/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.After;

import dev.galasa.framework.mocks.MockCPSStore;
import dev.galasa.framework.mocks.MockEnvironment;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockIFrameworkRuns;
import dev.galasa.framework.spi.creds.FrameworkEncryptionService;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1NodeSelectorRequirement;
import io.kubernetes.client.openapi.models.V1NodeSelectorTerm;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PreferredSchedulingTerm;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import io.prometheus.client.CollectorRegistry;

public class TestPodSchedulerTest {

    class MockSettings extends Settings {

        private V1ConfigMap mockConfigMap;

        public MockSettings(V1ConfigMap configMap, K8sController controller, CoreV1Api api) throws K8sControllerException {
            super(controller, api);
            this.mockConfigMap = configMap;
        }

        @Override
        V1ConfigMap retrieveConfigMap() {
            return mockConfigMap;
        }
    }

    class MockK8sController extends K8sController {
        @Override
        public void pollUpdated() {
            // Do nothing...
        }
    }

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
        Settings settings
    ) {
        checkPodMetadata(pod, expectedRunName, expectedPodName, settings);
        checkPodContainer(pod, expectedEncryptionKeysMountPath, settings);
        checkPodVolumes(pod, settings);
        checkPodSpec(pod, settings);
    }

    private void checkPodMetadata(V1Pod pod, String expectedRunName, String expectedPodName, Settings settings) {
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

    private void checkPodSpec(V1Pod pod, Settings settings) {

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
            String tolerationKey = nodeTolerationsString.split("=")[0];
            String tolerationOperator = nodeTolerationsString.split("=")[1].split(":")[0];
            String tolerationEffect = nodeTolerationsString.split("=")[1].split(":")[1];

            V1Toleration testToleration = new V1Toleration();

            testToleration.setKey(tolerationKey);
            testToleration.setOperator(tolerationOperator);
            testToleration.setEffect(tolerationEffect);

            assertThat(podSpec.getTolerations()).contains(testToleration);
        }
    }

    private void checkPodContainer(V1Pod pod, String expectedEncryptionKeysMountPath, Settings settings) {
        // Check that test container has been added
        V1PodSpec actualPodSpec = pod.getSpec();
        List<V1Container> actualContainers = actualPodSpec.getContainers();
        assertThat(actualContainers).hasSize(1);

        V1Container testContainer = actualContainers.get(0);
        assertThat(testContainer.getCommand()).containsExactly("java");
        assertThat(testContainer.getArgs()).contains("-jar", "boot.jar", "--run", settings.getBootstrap());

        // Check that the encryption keys have been mounted to the correct location
        List<V1VolumeMount> testContainerVolumeMounts = testContainer.getVolumeMounts();
        assertThat(testContainerVolumeMounts).hasSize(1);

        V1VolumeMount encryptionKeysVolumeMount = testContainerVolumeMounts.get(0);
        assertThat(encryptionKeysVolumeMount.getName()).isEqualTo(TestPodScheduler.ENCRYPTION_KEYS_VOLUME_NAME);
        assertThat(encryptionKeysVolumeMount.getMountPath()).isEqualTo(expectedEncryptionKeysMountPath);
        assertThat(encryptionKeysVolumeMount.getReadOnly()).isTrue();
    }

    private void checkPodVolumes(V1Pod pod, Settings settings) {
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

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns);

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPod(runName, podName, isTraceEnabled);

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

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns);

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPod(runName, podName, isTraceEnabled);

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

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns);

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPod(runName, podName, isTraceEnabled);

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

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler runPoll = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns);

        String runName = "run1";
        String podName = settings.getEngineLabel() + "-" + runName;
        boolean isTraceEnabled = false;

        // When...
        V1Pod pod = runPoll.createTestPod(runName, podName, isTraceEnabled);

        // Then...
        V1EnvVar credsEnvVarObject = new V1EnvVar();
        credsEnvVarObject.setName(CREDS_ENV_VAR);
        credsEnvVarObject.setValue(customCredsLocation);

        List<V1EnvVar> envs = pod.getSpec().getContainers().get(0).getEnv();
        assertThat(envs).contains(credsEnvVarObject);
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

        MockK8sController controller = new MockK8sController();
        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockIFrameworkRuns mockFrameworkRuns = new MockIFrameworkRuns(new ArrayList<>());

        V1ConfigMap mockConfigMap = createMockConfigMap();
        MockSettings settings = new MockSettings(mockConfigMap, controller, null);
        settings.init();
        MockCPSStore mockCPS = new MockCPSStore(null);

        TestPodScheduler podScheduler = new TestPodScheduler(mockEnvironment, mockDss, mockCPS, settings, null, mockFrameworkRuns);

        // When...
        ArrayList<String> args = podScheduler.createCommandLineArgs(settings, "myRunName", TRACE_IS_ENABLED);

        // Then...
        assertThat(args).containsOnly(
        "-Xmx:150m",
                "-jar",
                "boot.jar", 
                "--obr",
                "file:galasa.obr",
                "--bootstrap",
                "http://my.server/bootstrap",
                "--run",
                "myRunName",
                "--trace"
        );

    }
}
