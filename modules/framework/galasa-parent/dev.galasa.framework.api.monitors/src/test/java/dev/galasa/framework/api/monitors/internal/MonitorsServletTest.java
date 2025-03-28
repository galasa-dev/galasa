/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal;

import java.util.List;
import java.util.Map;

import dev.galasa.framework.api.beans.generated.GalasaMonitor;
import dev.galasa.framework.api.beans.generated.GalasaMonitordata;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataResourceCleanupData;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataresourceCleanupDatafilters;
import dev.galasa.framework.api.beans.generated.GalasaMonitormetadata;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;

public class MonitorsServletTest extends BaseServletTest {

    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);


    protected GalasaMonitor generateExpectedMonitor(
        String name,
        String stream,
        boolean isEnabled,
        List<String> includes,
        List<String> excludes
    ) {
        GalasaMonitor monitor = new GalasaMonitor();
        monitor.setApiVersion(GalasaResourceValidator.DEFAULT_API_VERSION);

        GalasaMonitormetadata metadata = new GalasaMonitormetadata();
        metadata.setname(name);

        GalasaMonitordata data = new GalasaMonitordata();
        data.setIsEnabled(isEnabled);

        GalasaMonitordataResourceCleanupData cleanupData = new GalasaMonitordataResourceCleanupData();
        cleanupData.setstream(stream);
        
        GalasaMonitordataresourceCleanupDatafilters filters = new GalasaMonitordataresourceCleanupDatafilters();

        if (!includes.isEmpty()) {
            filters.setincludes(includes.toArray(new String[0]));
        }

        if (!excludes.isEmpty()) {
            filters.setexcludes(excludes.toArray(new String[0]));
        }

        cleanupData.setfilters(filters);
        data.setResourceCleanupData(cleanupData);

        monitor.setmetadata(metadata);
        monitor.setdata(data);
        return monitor;
    }

    protected V1Deployment createMockDeployment(String name, String stream, int replicas, List<String> includes, List<String> excludes) {
        V1Deployment deployment = new V1Deployment();

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setName(name);

        V1DeploymentSpec spec = new V1DeploymentSpec();
        spec.setReplicas(replicas);

        V1PodTemplateSpec template = new V1PodTemplateSpec();
        spec.setTemplate(template);

        V1PodSpec podSpec = new V1PodSpec();
        template.setSpec(podSpec);

        V1Container container = new V1Container();
        podSpec.addContainersItem(container);

        String commaSeparatedIncludes = String.join(",", includes);
        String commaSeparatedExcludes = String.join(",", excludes);

        addMockEnvVar(MonitorTransform.MONITOR_STREAM_ENV_VAR, stream, container);
        addMockEnvVar(MonitorTransform.MONITOR_INCLUDES_GLOB_PATTERNS_ENV_VAR, commaSeparatedIncludes, container);
        addMockEnvVar(MonitorTransform.MONITOR_EXCLUDES_GLOB_PATTERNS_ENV_VAR, commaSeparatedExcludes, container);

        deployment.setMetadata(metadata);
        deployment.setSpec(spec);
        return deployment;
    }

    private void addMockEnvVar(String name, String value, V1Container container) {
        V1EnvVar envVar = new V1EnvVar();
        envVar.setName(name);
        envVar.setValue(value);
        container.addEnvItem(envVar);
    }
}
