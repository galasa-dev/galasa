/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import java.util.List;

import dev.galasa.framework.api.beans.generated.GalasaMonitor;
import dev.galasa.framework.api.beans.generated.GalasaMonitordata;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataResourceCleanupData;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataresourceCleanupDatafilters;
import dev.galasa.framework.api.beans.generated.GalasaMonitormetadata;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

/**
 * Converts a Kubernetes deployment into an external GalasaMonitor bean.
 * Kubernetes deployments for Galasa monitors are expected to look similar to the example below.
 * 
 * The fields of interest from a Kubernetes deployment are:
 * 
 * - The name of the deployment (the 'metadata.name' field)
 * - The number of replicas that are set (the 'spec.replicas' field)
 * - The values of the environment variables 'GALASA_CLEANUP_MONITOR_STREAM',
 *   'GALASA_MONITOR_INCLUDES_GLOB_PATTERNS', and 'GALASA_MONITOR_EXCLUDES_GLOB_PATTERNS'
 *   in the 'resource-monitor' container
 * 
 * apiVersion: apps/v1
 * kind: Deployment
 * metadata:
 *   name: my-custom-resource-monitor
 *   labels:
 *     galasa-monitor: custom
 *     app: my-custom-resource-monitor
 * spec:
 *   replicas: 0
 *   strategy:
 *     type: Recreate
 *   selector:
 *     matchLabels:
 *       app: my-custom-resource-monitor
 *   template:
 *     metadata:
 *       name: my-custom-resource-monitor
 *       labels:
 *         app: my-custom-resource-monitor
 *     spec:
 *       serviceAccountName: galasa
 *       nodeSelector:
 *         kubernetes.io/arch: amd64
 *       initContainers:
 *         - name: wait-for-api
 *           ...
 *       containers:
 *       - name: resource-monitor
 *         ...
 *         env:
 *         - name: NAMESPACE
 *           valueFrom: ...
 *         - name: GALASA_CONFIG_STORE
 *           value: ...
 *         - name: GALASA_DYNAMICSTATUS_STORE
 *           value: ...
 *         - name: GALASA_RESULTARCHIVE_STORE
 *           value: ...
 *         - name: GALASA_CREDENTIALS_STORE
 *           value: ...
 *         - name: GALASA_RAS_TOKEN
 *           valueFrom: ...
 *         - name: GALASA_CLEANUP_MONITOR_STREAM
 *           value: "myStream"
 *         - name: GALASA_MONITOR_INCLUDES_GLOB_PATTERNS
 *           value: "my.company*,*MyResourceMonitorClass"
 *         - name: GALASA_MONITOR_EXCLUDES_GLOB_PATTERNS
 *           value: "my.company.exclude*,*MyResourceMonitorToExclude"
 *         ports:
 *         ...
 *         livenessProbe:
 *         ...
 *         readinessProbe:
 *         ...
 */
public class MonitorTransform {

    public static final String MONITOR_STREAM_ENV_VAR = "GALASA_MONITOR_STREAM";
    public static final String MONITOR_INCLUDES_GLOB_PATTERNS_ENV_VAR = "GALASA_MONITOR_INCLUDES_GLOB_PATTERNS";
    public static final String MONITOR_EXCLUDES_GLOB_PATTERNS_ENV_VAR = "GALASA_MONITOR_EXCLUDES_GLOB_PATTERNS";

    public GalasaMonitor createGalasaMonitorBeanFromDeployment(V1Deployment monitorDeployment) {
        GalasaMonitor monitor = new GalasaMonitor();
        monitor.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);

        GalasaMonitormetadata metadata = createMonitorMetadata(monitorDeployment.getMetadata());

        V1DeploymentSpec deploymentSpec = monitorDeployment.getSpec();
        GalasaMonitordata data = createMonitorData(deploymentSpec);

        monitor.setmetadata(metadata);
        monitor.setdata(data);
        return monitor;
    }

    private GalasaMonitormetadata createMonitorMetadata(V1ObjectMeta deploymentMetadata) {
        GalasaMonitormetadata metadata = new GalasaMonitormetadata();
        metadata.setname(deploymentMetadata.getName());
        return metadata;
    }

    private GalasaMonitordata createMonitorData(V1DeploymentSpec deploymentSpec) {
        GalasaMonitordata data = new GalasaMonitordata();

        boolean isEnabled = deploymentSpec.getReplicas() != 0;
        data.setIsEnabled(isEnabled);
    
        List<V1Container> containers = deploymentSpec.getTemplate().getSpec().getContainers();
        if (!containers.isEmpty()) {
            V1Container monitorContainer = containers.get(0);
            List<V1EnvVar> monitorEnvVars = monitorContainer.getEnv();

            GalasaMonitordataResourceCleanupData cleanupData = createCleanupData(monitorEnvVars);
            data.setResourceCleanupData(cleanupData);
        }
        return data;
    }

    private GalasaMonitordataResourceCleanupData createCleanupData(List<V1EnvVar> monitorEnvVars) {
        GalasaMonitordataResourceCleanupData cleanupData = new GalasaMonitordataResourceCleanupData();
        GalasaMonitordataresourceCleanupDatafilters bundleFilters = new GalasaMonitordataresourceCleanupDatafilters();

        for (V1EnvVar envVar : monitorEnvVars) {
            switch (envVar.getName()) {
                case MONITOR_STREAM_ENV_VAR:
                    cleanupData.setstream(envVar.getValue());
                    break;
                case MONITOR_INCLUDES_GLOB_PATTERNS_ENV_VAR:
                    String commaSeparatedIncludes = envVar.getValue();
                    if (commaSeparatedIncludes != null && !commaSeparatedIncludes.isBlank()) {
                        bundleFilters.setincludes(commaSeparatedIncludes.split(","));
                    }
                    break;
                case MONITOR_EXCLUDES_GLOB_PATTERNS_ENV_VAR:
                    String commaSeparatedExcludes = envVar.getValue();
                    if (commaSeparatedExcludes != null && !commaSeparatedExcludes.isBlank()) {
                        bundleFilters.setexcludes(commaSeparatedExcludes.split(","));
                    }
                    break;
            }
        }
        cleanupData.setfilters(bundleFilters);
        return cleanupData;
    }
}
