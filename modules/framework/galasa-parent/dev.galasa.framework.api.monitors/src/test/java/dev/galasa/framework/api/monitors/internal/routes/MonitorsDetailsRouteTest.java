/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.monitors.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.beans.generated.GalasaMonitor;
import dev.galasa.framework.api.beans.generated.UpdateGalasaMonitorRequest;
import dev.galasa.framework.api.beans.generated.UpdateGalasaMonitorRequestdata;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.monitors.internal.MonitorsServletTest;
import dev.galasa.framework.api.monitors.mocks.MockKubernetesApiClient;
import dev.galasa.framework.api.monitors.mocks.MockMonitorsServlet;
import io.kubernetes.client.openapi.models.V1Deployment;

public class MonitorsDetailsRouteTest extends MonitorsServletTest {

    private String createUpdateRequestJson(boolean isEnabled) {
        UpdateGalasaMonitorRequest updateRequest = new UpdateGalasaMonitorRequest();
        UpdateGalasaMonitorRequestdata requestData = new UpdateGalasaMonitorRequestdata();

        requestData.setIsEnabled(isEnabled);
        updateRequest.setdata(requestData);

        return gson.toJson(updateRequest);
    }

    @Test
    public void testMonitorsDetailsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new MonitorsDetailsRoute(null, null, null, null).getPathRegex();

        // Then...
        assertThat(routePattern.matcher("/MyCustomMonitor").matches()).isTrue();
        assertThat(routePattern.matcher("/MYMONITOR/").matches()).isTrue();
        assertThat(routePattern.matcher("/mymonitor").matches()).isTrue();
        assertThat(routePattern.matcher("/mymon1t3r").matches()).isTrue();
        assertThat(routePattern.matcher("/My-Monitor_1").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("/my-monitor.456").matches()).isFalse();
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("").matches()).isFalse();
        assertThat(routePattern.matcher("/my monitor").matches()).isFalse();
        assertThat(routePattern.matcher("/<html>thisisbad</html>").matches()).isFalse();
        assertThat(routePattern.matcher("/javascript:thisisbad;").matches()).isFalse();
    }

    @Test
    public void testGetMonitorByNameReturnsCorrectResponse() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 0;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        String monitorName2 = "anothermonitor";
        String stream2 = "myOtherStream";
        int replicas2 = 1;
        List<String> includes2 = List.of("*");
        List<String> excludes2 = List.of("*MonitorClassToExclude", "dev.galasa.*");

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        V1Deployment deployment2 = createMockDeployment(monitorName2, stream2, replicas2, includes2, excludes2);
        mockApiClient.addMockDeployment(deployment);
        mockApiClient.addMockDeployment(deployment2);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + monitorName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        GalasaMonitor expectedMonitor = generateExpectedMonitor(monitorName, stream, false, includes, excludes);
        String expectedJsonString = gson.toJson(expectedMonitor);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        assertThat(outStream.toString()).isEqualTo(expectedJsonString);
    }

    @Test
    public void testGetNonExistantMonitorByNameReturnsCorrectError() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 0;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        mockApiClient.addMockDeployment(deployment);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        String nonExistantMonitorName = "NON_EXISTANT_MONITOR";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + nonExistantMonitorName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5422, "GAL5422E", "Unable to retrieve a monitor with the given name");
    }

    @Test
    public void testEnableMonitorUpdatesDeploymentReplicasOk() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 0;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        mockApiClient.addMockDeployment(deployment);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        boolean isEnabled = true;
        String requestBodyJson = createUpdateRequestJson(isEnabled);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + monitorName, requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        GalasaMonitor expectedMonitor = generateExpectedMonitor(monitorName, stream, true, includes, excludes);
        String expectedJsonString = gson.toJson(expectedMonitor);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        assertThat(deployment.getSpec().getReplicas()).isEqualTo(1);
        assertThat(outStream.toString()).isEqualTo(expectedJsonString);
    }

    @Test
    public void testEnableAlreadyEnabledMonitorDoesNotErrorOut() throws Exception {
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

        boolean isEnabled = true;
        String requestBodyJson = createUpdateRequestJson(isEnabled);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + monitorName, requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        GalasaMonitor expectedMonitor = generateExpectedMonitor(monitorName, stream, true, includes, excludes);
        String expectedJsonString = gson.toJson(expectedMonitor);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        assertThat(deployment.getSpec().getReplicas()).isEqualTo(1);
        assertThat(outStream.toString()).isEqualTo(expectedJsonString);
    }

    @Test
    public void testDisableMonitorUpdatesDeploymentReplicasOk() throws Exception {
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

        boolean isEnabled = false;
        String requestBodyJson = createUpdateRequestJson(isEnabled);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + monitorName, requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        GalasaMonitor expectedMonitor = generateExpectedMonitor(monitorName, stream, false, includes, excludes);
        String expectedJsonString = gson.toJson(expectedMonitor);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        assertThat(deployment.getSpec().getReplicas()).isEqualTo(0);
        assertThat(outStream.toString()).isEqualTo(expectedJsonString);
    }

    @Test
    public void testGetMonitorReturnsCorrectErrorOnFailedKubernetesRequest() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();
        mockApiClient.setThrowErrorEnabled(true);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        String monitorName = "system";
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + monitorName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5421, "GAL5421E", "Error occurred when getting the Galasa monitor deployments from Kubernetes");
    }

    @Test
    public void testUpdateMonitorsStatusReturnsCorrectErrorOnFailedKubernetesRequest() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();
        mockApiClient.setThrowErrorEnabled(true);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        boolean isEnabled = true;
        String requestBodyJson = createUpdateRequestJson(isEnabled);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/myMonitor", requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(
            outStream.toString(),
            5421,
            "GAL5421E", "Error occurred when getting the Galasa monitor deployments from Kubernetes"
        );
    }

    @Test
    public void testEnableNonExistantMonitorReturnsCorrectError() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 0;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        mockApiClient.addMockDeployment(deployment);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        boolean isEnabled = true;
        String requestBodyJson = createUpdateRequestJson(isEnabled);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/non-existant-monitor", requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5422, "GAL5422E", "No such monitor exists");
    }

    @Test
    public void testEnableMonitorWithNoDataReturnsCorrectError() throws Exception {
        // Given...
        MockFramework mockFramework = new MockFramework();

        MockKubernetesApiClient mockApiClient = new MockKubernetesApiClient();

        String monitorName = "system";
        String stream = "myStream";
        int replicas = 0;
        List<String> includes = List.of("*");
        List<String> excludes = new ArrayList<>();

        V1Deployment deployment = createMockDeployment(monitorName, stream, replicas, includes, excludes);
        mockApiClient.addMockDeployment(deployment);

        MockMonitorsServlet servlet = new MockMonitorsServlet(mockFramework, mockApiClient);

        // Pass in an empty request body
        String requestBodyJson = "{}";

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/non-existant-monitor", requestBodyJson, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5425, "GAL5425E", "Invalid request payload");
    }
}
