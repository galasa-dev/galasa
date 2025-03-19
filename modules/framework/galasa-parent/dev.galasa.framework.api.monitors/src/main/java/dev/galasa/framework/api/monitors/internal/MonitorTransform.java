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
 */
public class MonitorTransform {

    public static final String CLEANUP_MONITOR_STREAM_ENV_VAR = "GALASA_CLEANUP_MONITOR_STREAM";
    public static final String MONITOR_INCLUDES_REGEXES_ENV_VAR = "GALASA_MONITOR_INCLUDES_REGEXES";
    public static final String MONITOR_EXCLUDES_REGEXES_ENV_VAR = "GALASA_MONITOR_EXCLUDES_REGEXES";

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
                case CLEANUP_MONITOR_STREAM_ENV_VAR:
                    cleanupData.setstream(envVar.getValue());
                    break;
                case MONITOR_INCLUDES_REGEXES_ENV_VAR:
                    String commaSeparatedIncludes = envVar.getValue();
                    if (commaSeparatedIncludes != null && !commaSeparatedIncludes.isBlank()) {
                        bundleFilters.setincludes(commaSeparatedIncludes.split(","));
                    }
                    break;
                case MONITOR_EXCLUDES_REGEXES_ENV_VAR:
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
