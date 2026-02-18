/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.streams.mocks.MockStreamsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockOBR;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.spi.streams.IStream;

public class StreamsRouteTest extends BaseServletTest {

    @Test
    public void testGetStreamsRouteReturnsEmptyStreamsArrayOK() throws Exception {

        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "empty");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        Stream[] streamsGotBack = gson.fromJson(output, Stream[].class);
        assertThat(streamsGotBack).hasSize(0);
    }

    @Test
    public void testGetStreamsRouteReturnsStreamsArrayWithSingleStreamOK() throws Exception {

        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "my.group";
        String OBR_ARTIFACT_ID = "my.group.obr";
        String OBR_VERSION = "0.0.1";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("testStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        Stream[] streamsGotBack = gson.fromJson(output, Stream[].class);
        assertThat(streamsGotBack).hasSize(1);
    }

    @Test
    public void testGetStreamsRouteReturnsStreamsArrayWithMultipleStreamsOK() throws Exception {

        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "my.group";
        String OBR_ARTIFACT_ID = "my.group.obr";
        String OBR_VERSION = "0.0.1";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("testStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        MockStream mockStream2 = new MockStream();
        mockStream2.setName("testStream2");
        mockStream2.setDescription("This is a second dummy test stream");
        mockStream2.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream2.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream2.setObrs(List.of(mockObr));

        mockStreams.add(mockStream);
        mockStreams.add(mockStream2);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        Stream[] streamsGotBack = gson.fromJson(output, Stream[].class);
        assertThat(streamsGotBack).hasSize(2);
    }

    @Test
    public void testGetSTreamsByNameRouteThrowsInternalServletException() throws Exception {

        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "my.group";
        String OBR_ARTIFACT_ID = "my.group.obr";
        String OBR_VERSION = "0.0.1";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        mockStreamsService.setThrowException(true);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(outStream.toString()).contains("GAL5000E", "Error occurred when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");

    }

    @Test
    public void testPostStreamsCreatesNewStream() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("name", "newStream");
        requestJson.addProperty("description", "A new test stream");

        JsonObject repository = new JsonObject();
        repository.addProperty("url", "http://myrepo.com/maven");

        JsonObject testCatalog = new JsonObject();
        testCatalog.addProperty("url", "http://myrepo.com/testcatalog.yaml");

        JsonObject obr = new JsonObject();
        obr.addProperty("group-id", "dev.galasa");
        obr.addProperty("artifact-id", "dev.galasa.obr");
        obr.addProperty("version", "0.1.0");

        JsonArray obrs = new JsonArray();
        obrs.add(obr);

        requestJson.add("repository", repository);
        requestJson.add("testCatalog", testCatalog);
        requestJson.add("obrs", obrs);

        String requestBody = gson.toJson(requestJson);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("", requestBody, "POST", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        Stream createdStream = gson.fromJson(output, Stream.class);
        assertThat(createdStream.getmetadata().getname()).isEqualTo("newStream");
        assertThat(createdStream.getmetadata().getdescription()).isEqualTo("A new test stream");
    }

    @Test
    public void testPostStreamsWithExistingStreamThrowsConflictError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream existingStream = new MockStream();
        existingStream.setName("existingStream");
        existingStream.setDescription("An existing stream");
        existingStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        existingStream.setTestCatalogUrl("http://myrepo.com/testcatalog.yaml");
        existingStream.setObrs(List.of(mockObr));
        mockStreams.add(existingStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("name", "existingStream");
        requestJson.addProperty("description", "Trying to create duplicate");

        JsonObject repository = new JsonObject();
        repository.addProperty("url", "http://myrepo.com/maven");

        JsonObject testCatalog = new JsonObject();
        testCatalog.addProperty("url", "http://myrepo.com/testcatalog.yaml");

        JsonObject obr = new JsonObject();
        obr.addProperty("group-id", "dev.galasa");
        obr.addProperty("artifact-id", "dev.galasa.obr");
        obr.addProperty("version", "0.1.0");

        JsonArray obrs = new JsonArray();
        obrs.add(obr);

        requestJson.add("repository", repository);
        requestJson.add("testCatalog", testCatalog);
        requestJson.add("obrs", obrs);

        String requestBody = gson.toJson(requestJson);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("", requestBody, "POST", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(409);
        checkErrorStructure(output, 5429, "GAL5429E");
    }

    @Test
    public void testPostStreamsWithInvalidStreamNameThrowsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("name", "invalid-name-with-special-chars!");
        requestJson.addProperty("description", "Invalid stream name");

        JsonObject repository = new JsonObject();
        repository.addProperty("url", "http://myrepo.com/maven");

        JsonObject testCatalog = new JsonObject();
        testCatalog.addProperty("url", "http://myrepo.com/testcatalog.yaml");

        JsonObject obr = new JsonObject();
        obr.addProperty("mvnGroupId", "dev.galasa");
        obr.addProperty("mvnArtifactId", "dev.galasa.obr");
        obr.addProperty("mvnVersion", "0.1.0");

        JsonArray obrs = new JsonArray();
        obrs.add(obr);

        requestJson.add("repository", repository);
        requestJson.add("testCatalog", testCatalog);
        requestJson.add("obrs", obrs);

        String requestBody = gson.toJson(requestJson);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("", requestBody, "POST", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5418, "GAL5418E");
    }

    @Test
    public void testPostStreamsWithEmptyOBRsThrowsError() throws Exception {
        // Given...
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService);

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("name", "testStream");
        requestJson.addProperty("description", "Test stream with no OBRs");

        JsonObject repository = new JsonObject();
        repository.addProperty("url", "http://myrepo.com/maven");

        JsonObject testCatalog = new JsonObject();
        testCatalog.addProperty("url", "http://myrepo.com/testcatalog.yaml");

        JsonArray obrs = new JsonArray();

        requestJson.add("repository", repository);
        requestJson.add("testCatalog", testCatalog);
        requestJson.add("obrs", obrs);

        String requestBody = gson.toJson(requestJson);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("", requestBody, "POST", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doPost(mockRequest, servletResponse);

        String output = outStream.toString();

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5437, "GAL5437E");
    }
}
