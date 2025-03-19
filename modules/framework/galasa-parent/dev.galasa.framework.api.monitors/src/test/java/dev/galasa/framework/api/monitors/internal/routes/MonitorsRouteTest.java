/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.GalasaMonitor;
import dev.galasa.framework.api.beans.generated.GalasaMonitordata;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataResourceCleanupData;
import dev.galasa.framework.api.beans.generated.GalasaMonitordataresourceCleanupDatafilters;
import dev.galasa.framework.api.beans.generated.GalasaMonitormetadata;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.resources.GalasaResourceValidator;
import dev.galasa.framework.api.monitors.internal.MonitorTransform;
import dev.galasa.framework.api.monitors.mocks.MockKubernetesApiClient;
import dev.galasa.framework.api.monitors.mocks.MockMonitorsServlet;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;

public class MonitorsRouteTest extends BaseServletTest {

    protected static final Map<String, String> REQUEST_HEADERS = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

    private GalasaMonitor generateExpectedMonitor(
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

    private V1Deployment createMockDeployment(String name, String stream, int replicas, List<String> includes, List<String> excludes) {
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

        addMockEnvVar(MonitorTransform.CLEANUP_MONITOR_STREAM_ENV_VAR, stream, container);
        addMockEnvVar(MonitorTransform.MONITOR_INCLUDES_REGEXES_ENV_VAR, commaSeparatedIncludes, container);
        addMockEnvVar(MonitorTransform.MONITOR_EXCLUDES_REGEXES_ENV_VAR, commaSeparatedExcludes, container);

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

    @Test
    public void testMonitorsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new MonitorsRoute(null, null, null, null).getPathRegex();

        // Then...
        // The servlet's whiteboard pattern will match /secrets, so the secrets route
        // should only allow an optional / or an empty string (no suffix after "/secrets")
        assertThat(routePattern.matcher("/").matches()).isTrue();
        assertThat(routePattern.matcher("").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("/wrongpath!").matches()).isFalse();
    }

    @Test
    public void testGetMonitorsReturnsAllMonitors() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 1;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        mockApiClient.addMockDeployment(deployment);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        GalasaMonitor expectedMonitor = generateExpectedMonitor(monitorName, stream, true, includes, excludes);
        JsonElement expectedJson = gson.toJsonTree(expectedMonitor);
        JsonArray expectedJsonArray = new JsonArray();
        expectedJsonArray.add(expectedJson);

        String expectedJsonString = gson.toJson(expectedJsonArray);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(expectedJsonString);
    }
}
