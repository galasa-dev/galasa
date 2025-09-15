/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.k8s.controller.api.KubernetesEngineFacade;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

/**
 * A collection of settings obtained from a config map.
 * 
 * Every so often (see the REFRESH_DELAY constants in this class), the values will be re-loaded,
 * making the settings dynamically changeable if someone changes the config map settings.
 */
public class Settings implements Runnable, ISettings {

    public static final int MAX_TEST_POD_RETRY_LIMIT_DEFAULT = 5;
    public static final String MAX_TEST_POD_RETRY_LIMIT_CONFIG_MAP_PROPERTY_NAME = "max_test_pod_retry_limit";

    public static final int INTERRUPTED_RUN_CLEANUP_GRACE_PERIOD_SECS_DEFAULT = 300;
    public static final String INTERRUPTED_RUN_CLEANUP_GRACE_PERIOD_SECS_PROPERTY_NAME = "interrupted_test_run_cleanup_grace_period_seconds";

    public static final int ALLOCATED_TEST_RUN_TIMEOUT_MINUTES_DEFAULT = 30;
    public static final String ALLOCATED_TEST_RUN_TIMEOUT_MINUTES_PROPERTY_NAME = "allocated_test_run_timeout_minutes";

    private final Log         logger                      = LogFactory.getLog(getClass());

    private final K8sController controller;

    // How often does the run() method get called to update all the values from the configMap
    public static final long REFRESH_DELAY_SECS_INITIAL = 20;
    public static final long REFRESH_DELAY_SECS = 20;

    private String            namespace;
    private String            podname;
    private String            configMapName;
    private String            engineLabel                 = "none";
    private String            engineImage                 = "none";
    private int               engineMemoryHeapSizeMi      = 150;
    private int               engineMemoryRequestMi       = 150;
    private int               engineMemoryLimitMi         = 200;
    private int               engineCPURequestM           = 400;
    private int               engineCPULimitM             = 1000;
    private String            nodeArch                    = "";
    private String            nodePreferredAffinity       = "";
    private String            nodeTolerations             = "";

    // A fail-safe to make sure we never try to re-launch/re-create a pod for a testcase more than 
    // this number  of times.
    private int maxTestPodRetryLimit = MAX_TEST_POD_RETRY_LIMIT_DEFAULT ;

    private String            encryptionKeysSecretName;

    private HashSet<String>   requiredCapabilities        = new HashSet<>();
    private HashSet<String>   capableCapabilities         = new HashSet<>();
    private String            reportCapabilties           = null;

    private long              kubeLaunchIntervalMillisecs = 1000L;
    private long              interruptedTestRunCleanupGracePeriodSeconds = INTERRUPTED_RUN_CLEANUP_GRACE_PERIOD_SECS_DEFAULT;
    private long              allocatedTestRunTimeoutMinutes = ALLOCATED_TEST_RUN_TIMEOUT_MINUTES_DEFAULT;

    // Poll loop interval which is looking for queued test runs, so they can be launched in a pod.
    private int               runPollSeconds              = 60;
    private int               maxEngines                  = 0;

    private ArrayList<String> requestorsByScheduleID      = new ArrayList<>();

    private final KubernetesEngineFacade   kube;
    private String            oldConfigMapResourceVersion = "";

    public Settings(K8sController controller, KubernetesEngineFacade kube, String podName, String configMapName) throws K8sControllerException {
        this.kube = kube;
        this.controller = controller;
        this.podname = podName;
        this.configMapName = configMapName;
    }

    public void init() throws K8sControllerException {
        loadConfigMapProperties();
    }

    @Override
    public void run() {
        try {
            loadConfigMapProperties();
        } catch (K8sControllerException e) {
            logger.error("Poll for the ConfigMap " + configMapName + " failed", e);
        }
    }

    private void loadConfigMapProperties() throws K8sControllerException {
        V1ConfigMap configMap = kube.getConfigMap(configMapName);
        validateConfigMap(configMap);
        updateConfigMapProperties(configMap.getMetadata(), configMap.getData());
    }

    private String updateProperty(Map<String, String> configMapData, String key, String defaultValue, String oldValue) {
        String newValue = getPropertyFromData(configMapData, key, defaultValue);
        if (!newValue.equals(oldValue)) {
            logger.info("Setting " + key + " from '" + oldValue + "' to '" + newValue + "'");
        }
        return newValue;
    }

    private long updateProperty(Map<String, String> configMapData, String key, long defaultValue, long oldValue) {
        long newValue = defaultValue;
        String defaultValueAsStr = Long.toString(defaultValue);
        String rawValueFromConfig = getPropertyFromData(configMapData, key, defaultValueAsStr);
        String trimmedValueFromConfig = rawValueFromConfig.trim();

        try {
            newValue = Long.parseLong(trimmedValueFromConfig);
            if (newValue != oldValue) {
                logger.info("Setting " + key + " from '" + oldValue + "' to '" + newValue + "'");
            }
        } catch (NumberFormatException ex) {
            String msg = MessageFormat.format(
                "Info: Could not read an integer value from the engine controller ConfigMap property {0}. Using default value of {1}. ConfigMap Value '{2}' is not a number.",
                key,
                defaultValueAsStr,
                trimmedValueFromConfig
            );
            logger.info(msg);
        }
        return newValue;
    }

    private int updateProperty(Map<String, String> configMapData, String key, int defaultValue, int oldValue) throws K8sControllerException {
        int newValue = getPropertyFromData(configMapData, key, defaultValue);
        if (newValue != oldValue) {
            logger.info("Setting " + key + " from '" + oldValue + "' to '" + newValue + "'");
        }
        return newValue;
    }

    private String getPropertyFromData(Map<String, String> configMapData, String key, String defaultValue) {
        String value = configMapData.get(key);
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }

        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    private int getPropertyFromData(Map<String, String> configMapData, String key, int defaultValue) throws K8sControllerException {
        int returnValue = defaultValue;
        try {
            String valueStr = configMapData.get(key);
            if (valueStr != null && !valueStr.isBlank()) {
                returnValue = Integer.parseInt(valueStr);
            }
        } catch (NumberFormatException e) {
            throw new K8sControllerException("Invalid value provided for " + key + " in settings configmap");
        }
        return returnValue;
    }

    private void validateConfigMap(V1ConfigMap configMap) throws K8sControllerException {
        V1ObjectMeta configMapMetadata = configMap.getMetadata();
        Map<String, String> configMapData = configMap.getData();
        if (configMapMetadata == null || configMapData == null) {
            throw new K8sControllerException("Settings configmap is missing required metadata or data");
        }
    }

    private void updateConfigMapProperties(V1ObjectMeta configMapMetadata, Map<String, String> configMapData) throws K8sControllerException {
        String newResourceVersion = configMapMetadata.getResourceVersion();
        
        if (newResourceVersion != null && newResourceVersion.equals(oldConfigMapResourceVersion)) {
            // There is nothing to update, as the old config map version is the same as the current one.
            return;
        }

        oldConfigMapResourceVersion = newResourceVersion;

        logger.info("ConfigMap has been changed, reloading parameters");
        updateConfigMapProperties(configMapData);

        setRunPoll(configMapData);
        setRequestorsByScheduleId(configMapData);
        setEngineCapabilities(configMapData);
    }


    protected void updateConfigMapProperties(Map<String,String> configMapData) throws K8sControllerException { 

        this.maxEngines = updateProperty(configMapData, "max_engines", 1, this.maxEngines);
        this.engineLabel = updateProperty(configMapData, "engine_label", "k8s-standard-engine", this.engineLabel);
        this.engineImage = updateProperty(configMapData, "engine_image", "ghcr.io/galasa-dev/galasa-boot-embedded-amd64", this.engineImage);
        this.kubeLaunchIntervalMillisecs = updateProperty(configMapData, "kube_launch_interval_milliseconds", kubeLaunchIntervalMillisecs, this.kubeLaunchIntervalMillisecs);
        this.interruptedTestRunCleanupGracePeriodSeconds = updateProperty(configMapData, INTERRUPTED_RUN_CLEANUP_GRACE_PERIOD_SECS_PROPERTY_NAME, INTERRUPTED_RUN_CLEANUP_GRACE_PERIOD_SECS_DEFAULT, this.interruptedTestRunCleanupGracePeriodSeconds);
        this.allocatedTestRunTimeoutMinutes = updateProperty(configMapData, ALLOCATED_TEST_RUN_TIMEOUT_MINUTES_PROPERTY_NAME, ALLOCATED_TEST_RUN_TIMEOUT_MINUTES_DEFAULT, this.allocatedTestRunTimeoutMinutes);

        this.engineMemoryRequestMi = updateProperty(configMapData, "engine_memory_request", engineMemoryRequestMi, this.engineMemoryRequestMi);
        this.engineMemoryLimitMi = updateProperty(configMapData, "engine_memory_limit", engineMemoryLimitMi, this.engineMemoryLimitMi);
        this.engineCPURequestM = updateProperty(configMapData, "engine_cpu_request", engineCPURequestM, this.engineCPURequestM);
        this.engineCPULimitM = updateProperty(configMapData, "engine_cpu_limit", engineCPULimitM, this.engineCPULimitM);

        this.engineMemoryHeapSizeMi = updateProperty(configMapData, "engine_memory_heap", engineMemoryHeapSizeMi, this.engineMemoryHeapSizeMi);

        this.nodeArch = updateProperty(configMapData, "node_arch", "", this.nodeArch);
        this.nodePreferredAffinity = updateProperty(configMapData, "galasa_node_preferred_affinity", "", this.nodePreferredAffinity);
        this.nodeTolerations = updateProperty(configMapData, "galasa_node_tolerations", "", this.nodeTolerations);

        this.encryptionKeysSecretName = updateProperty(configMapData, "encryption_keys_secret_name", "", this.encryptionKeysSecretName);

        this.maxTestPodRetryLimit = updateProperty(configMapData, MAX_TEST_POD_RETRY_LIMIT_CONFIG_MAP_PROPERTY_NAME, MAX_TEST_POD_RETRY_LIMIT_DEFAULT, this.maxTestPodRetryLimit);
    }

    private void setRunPoll(Map<String,String> configMapData) throws K8sControllerException {
        int poll = getPropertyFromData(configMapData, "run_poll", 20);
        if (poll != runPollSeconds) {
            logger.info("Setting Run Poll from '" + runPollSeconds + "' to '" + poll + "'");
            runPollSeconds = poll;
            controller.pollUpdated();
        }
    }

    private void setRequestorsByScheduleId(Map<String, String> configMapData) {
        String newRequestors = getPropertyFromData(configMapData, "scheduled_requestors", null);
        ArrayList<String> newRequestorsByScheduleid = new ArrayList<>();

        if (newRequestors != null) {
            String requestors[] = newRequestors.split(",");
            for (String requestor : requestors) {
                newRequestorsByScheduleid.add(requestor);
            }

            if (!requestorsByScheduleID.equals(newRequestorsByScheduleid)) {
                logger.info("Setting Requestors by Schedule from '" + requestorsByScheduleID + "' to '"
                        + newRequestorsByScheduleid + "'");
                requestorsByScheduleID = newRequestorsByScheduleid;
            }
        }
    }

    private void setEngineCapabilities(Map<String, String> configMapData) {
        String newCapabilities = getPropertyFromData(configMapData, "engine_capabilities", null);
        ArrayList<String> newRequiredCapabilties = new ArrayList<>();
        ArrayList<String> newCapableCapabilties = new ArrayList<>();

        if (newCapabilities != null) {
            String capabilities[] = newCapabilities.split(",");
            for (String capability : capabilities) {
                capability = capability.trim();
                if (capability.startsWith("+")) {
                    capability = capability.substring(1);
                    if (!capability.isEmpty()) {
                        newRequiredCapabilties.add(capability);
                    }
                } else {
                    if (!capability.isEmpty()) {
                        newCapableCapabilties.add(capability);
                    }
                }
            }

            boolean changed = false;
            if (newRequiredCapabilties.size() != requiredCapabilities.size()
                    || newCapableCapabilties.size() != capableCapabilities.size()) {
                changed = true;
            } else {
                for (String cap : newCapableCapabilties) {
                    if (!capableCapabilities.contains(cap)) {
                        changed = true;
                        break;
                    }
                }
                for (String cap : newRequiredCapabilties) {
                    if (!requiredCapabilities.contains(cap)) {
                        changed = true;
                        break;
                    }
                }
            }

            if (changed) {
                capableCapabilities.clear();
                requiredCapabilities.clear();
                capableCapabilities.addAll(newCapableCapabilties);
                requiredCapabilities.addAll(newRequiredCapabilties);
                logger.info("Engine set with Required Capabilities - " + requiredCapabilities);
                logger.info("Engine set with Capabable Capabilities - " + capableCapabilities);

                StringBuilder report = new StringBuilder();
                for (String cap : requiredCapabilities) {
                    if (report.length() > 0) {
                        report.append(",");
                    }
                    report.append("+");
                    report.append(cap);
                }
                for (String cap : capableCapabilities) {
                    if (report.length() > 0) {
                        report.append(",");
                    }
                    report.append(cap);
                }
                if (report.length() > 0) {
                    reportCapabilties = report.toString();
                } else {
                    reportCapabilties = null;
                }
            }
        }
    }

    public String getPodName() {
        return this.podname;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getEngineLabel() {
        return this.engineLabel;
    }

    public int getMaxEngines() {
        return this.maxEngines;
    }

    public List<String> getRequestorsByGroup() {
        return this.requestorsByScheduleID;
    }

    public String getNodeArch() {
        return this.nodeArch;
    }

    public String getNodePreferredAffinity() {
        return this.nodePreferredAffinity;
    }

    public String getNodeTolerations() {
        return this.nodeTolerations;
    }


    public String getEngineImage() {
        return this.engineImage;
    }

    public int getEngineMemoryRequestMegabytes() {
        return this.engineMemoryRequestMi;
    }

    public int getEngineCPURequestM() {
        return this.engineCPURequestM;
    }

    public int getEngineMemoryLimitMegabytes() {
        return this.engineMemoryLimitMi;
    }

    public int getEngineMemoryHeapSizeMegabytes() {
        return this.engineMemoryHeapSizeMi;
    }

    public int getEngineCPULimitM() {
        return this.engineCPULimitM;
    }

    public long getPollSeconds() {
        return this.runPollSeconds;
    }

    public String getEncryptionKeysSecretName() {
        return encryptionKeysSecretName;
    }

    public long getKubeLaunchIntervalMillisecs() {
        return this.kubeLaunchIntervalMillisecs;
    }

    public int getMaxTestPodRetryLimit() {
        return this.maxTestPodRetryLimit;
    }

    public long getInterruptedTestRunCleanupGracePeriodSeconds() {
        return this.interruptedTestRunCleanupGracePeriodSeconds;
    }

    public long getAllocatedTestRunTimeoutMinutes() {
        return this.allocatedTestRunTimeoutMinutes;
    }
}
