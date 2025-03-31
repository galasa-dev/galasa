/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

public class Settings implements Runnable {

    private final Log         logger                      = LogFactory.getLog(getClass());

    private final K8sController controller;

    private String            namespace;
    private String            bootstrap                   = "http://bootstrap";
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

    private String            encryptionKeysSecretName;

    private HashSet<String>   requiredCapabilities        = new HashSet<>();
    private HashSet<String>   capableCapabilities         = new HashSet<>();
    private String            reportCapabilties           = null;

    private int               runPoll                     = 60;
    private int               maxEngines                  = 0;

    private ArrayList<String> requestorsByScheduleID      = new ArrayList<>();

    private final CoreV1Api   api;
    private String            oldConfigMapResourceVersion = "";

    public Settings(K8sController controller, CoreV1Api api) throws K8sControllerException {
        this.api = api;
        this.controller = controller;
    }

    public void init() throws K8sControllerException {
        loadEnvironmentProperties();
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
        V1ConfigMap configMap = retrieveConfigMap();
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

    V1ConfigMap retrieveConfigMap() throws K8sControllerException {
        V1ConfigMap configMap = null;
        try {
            configMap = api.readNamespacedConfigMap(configMapName, namespace).pretty("true").execute();
        } catch (ApiException e) {
            throw new K8sControllerException("Failed to read configmap '" + configMapName + "' in namespace '" + namespace + "'", e);
        }
        return configMap;
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
            return;
        }

        oldConfigMapResourceVersion = newResourceVersion;

        logger.info("ConfigMap has been changed, reloading parameters");

        this.bootstrap = updateProperty(configMapData, "bootstrap", "http://bootstrap", this.bootstrap);
        this.maxEngines = updateProperty(configMapData, "max_engines", 1, this.maxEngines);
        this.engineLabel = updateProperty(configMapData, "engine_label", "k8s-standard-engine", this.engineLabel);
        this.engineImage = updateProperty(configMapData, "engine_image", "ghcr.io/galasa-dev/galasa-boot-embedded-amd64", this.engineImage);

        this.engineMemoryRequestMi = updateProperty(configMapData, "engine_memory_request", engineMemoryRequestMi, this.engineMemoryRequestMi);
        this.engineMemoryLimitMi = updateProperty(configMapData, "engine_memory_limit", engineMemoryLimitMi, this.engineMemoryLimitMi);
        this.engineCPURequestM = updateProperty(configMapData, "engine_cpu_request", engineCPURequestM, this.engineCPURequestM);
        this.engineCPULimitM = updateProperty(configMapData, "engine_cpu_limit", engineCPULimitM, this.engineCPULimitM);

        this.nodeArch = updateProperty(configMapData, "node_arch", "", this.nodeArch);
        this.nodePreferredAffinity = updateProperty(configMapData, "galasa_node_preferred_affinity", "", this.nodePreferredAffinity);
        this.nodeTolerations = updateProperty(configMapData, "galasa_node_tolerations", "", this.nodeTolerations);

        this.encryptionKeysSecretName = updateProperty(configMapData, "encryption_keys_secret_name", "", this.encryptionKeysSecretName);

        int poll = getPropertyFromData(configMapData, "run_poll", 20);
        if (poll != runPoll) {
            logger.info("Setting Run Poll from '" + runPoll + "' to '" + poll + "'");
            runPoll = poll;
            controller.pollUpdated();
        }

        setRequestorsByScheduleId(configMapData);
        setEngineCapabilities(configMapData);
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

    private String getEnvironmentVariableOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        return value.trim();
    }

    private void loadEnvironmentProperties() {

        namespace = getEnvironmentVariableOrDefault("NAMESPACE", "default");
        logger.info("Setting Namespace to '" + namespace + "'");

        podname = getEnvironmentVariableOrDefault("PODNAME", "k8s-controller");
        logger.info("Setting Pod Name to '" + podname + "'");

        configMapName = getEnvironmentVariableOrDefault("CONFIG", "config");
        logger.info("Setting ConfigMap to '" + configMapName + "'");
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

    public String getBootstrap() {
        return this.bootstrap;
    }

    public long getPoll() {
        return this.runPoll;
    }

    public String getEncryptionKeysSecretName() {
        return encryptionKeysSecretName;
    }
}