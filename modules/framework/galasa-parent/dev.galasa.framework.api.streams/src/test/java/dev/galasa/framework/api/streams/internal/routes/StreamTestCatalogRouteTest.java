/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.MockCredentialsService;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.mocks.FilledMockEnvironment;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpClient;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.streams.mocks.MockStreamsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockOBR;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.mocks.MockStreamsService;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.streams.IStream;

public class StreamTestCatalogRouteTest extends BaseServletTest {

    private String createTestCatalogJson() {
        JsonObject testCatalogJson = new JsonObject();
        
        JsonObject metadataJson = new JsonObject();
        metadataJson.addProperty("generated", "2026-05-15T10:45:13.922391Z");
        metadataJson.addProperty("name", "dev.galasa.ivts.obr");
        testCatalogJson.add("metadata", metadataJson);

        JsonObject classesJson = new JsonObject();
        JsonObject classJson = new JsonObject();
        classJson.addProperty("name", "dev.galasa.ivts.core.CoreManagerIVT");
        classJson.addProperty("bundle", "dev.galasa.ivts");
        classJson.addProperty("shortName", "CoreManagerIVT");
        classJson.addProperty("package", "dev.galasa.ivts.core");
        classJson.addProperty("summary", "Ensure the basic functions are working in the Core Manager");
        classesJson.add("dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT", classJson);
        testCatalogJson.add("classes", classesJson);

        JsonObject packagesJson = new JsonObject();
        JsonArray corePackageArray = new JsonArray();
        corePackageArray.add("dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT");
        packagesJson.add("dev.galasa.ivts.core", corePackageArray);
        testCatalogJson.add("packages", packagesJson);

        JsonObject bundlesJson = new JsonObject();
        JsonObject ivtsBundleJson = new JsonObject();
        JsonObject bundlePackagesJson = new JsonObject();
        JsonArray bundleCorePackageArray = new JsonArray();
        bundleCorePackageArray.add("dev.galasa.ivts/dev.galasa.ivts.core.CoreManagerIVT");
        bundlePackagesJson.add("dev.galasa.ivts.core", bundleCorePackageArray);
        ivtsBundleJson.add("packages", bundlePackagesJson);
        bundlesJson.add("dev.galasa.ivts", ivtsBundleJson);
        testCatalogJson.add("bundles", bundlesJson);

        testCatalogJson.add("sharedEnvironments", new JsonObject());
        testCatalogJson.add("gherkin", new JsonObject());
        testCatalogJson.addProperty("name", "dev.galasa.ivts.obr");
        testCatalogJson.addProperty("build", "gradle");
        testCatalogJson.addProperty("version", "0.48.0");

        return gson.toJson(testCatalogJson);
    }

    @Test
    public void testGetTestCatalogWithValidStreamReturnsOK() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockCredentialsService mockCredentialsService = new MockCredentialsService(new HashMap<>());
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        mockFramework.setRBACService(mockRBACService);
        mockFramework.setStreamsService(mockStreamsService);
        mockFramework.setCredentialsService(mockCredentialsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        String testCatalogContent = createTestCatalogJson();
        InputStream testCatalogContentStream = new ByteArrayInputStream(testCatalogContent.getBytes(StandardCharsets.UTF_8));

        Map<String, List<String>> headers = Map.of("Content-Type", List.of(MimeType.APPLICATION_JSON.toString()));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;

        MockHttpResponse<InputStream> mockResponse = new MockHttpResponse<>(testCatalogContentStream, HttpHeaders.of(headers, defaultFilter));
        mockResponse.setStatusCode(200);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(outStream.toString()).isEqualTo(testCatalogContent);
    }

    @Test
    public void testGetTestCatalogWithNonExistentStreamReturnsNotFound() throws Exception {
        // Given...
        String streamName = "nonExistentStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("{}", 200);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(outStream.toString(), 5420, "GAL5420E");
    }

    @Test
    public void testGetTestCatalogWithStreamMissingTestCatalogUrlReturnsNotFound() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("{}", 200);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(outStream.toString(), 5455, "GAL5455E");
    }

    @Test
    public void testGetTestCatalogWithInvalidUrlReturnsBadRequest() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");

        // file:// is not supported for test streams
        mockStream.setTestCatalogUrl("file://myrepo.com/invalid.txt");

        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("{}", 200);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5456, "GAL5456E");
    }

    @Test
    public void testGetTestCatalogWithRedirectResponseReturnsBadGateway() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("{}", 302);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(502);
        checkErrorStructure(outStream.toString(), 5458, "GAL5458E");
    }

    @Test
    public void testGetTestCatalogWithNonOKResponseReturnsBadGateway() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("", 500);
        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(502);
        checkErrorStructure(outStream.toString(), 5459, "GAL5459E");
    }

    @Test
    public void testGetTestCatalogWithInvalidContentTypeReturnsBadGateway() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        Map<String, List<String>> headers = Map.of("Content-Type", List.of(MimeType.TEXT_PLAIN.toString()));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;

        MockHttpResponse<String> mockResponse = new MockHttpResponse<>("text", HttpHeaders.of(headers, defaultFilter));
        mockResponse.setStatusCode(200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(502);
        checkErrorStructure(outStream.toString(), 5461, "GAL5461E");
    }

    @Test
    public void testGetTestCatalogWithMavenCredentialsIncludesAuthHeader() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setMavenSecretName("maven-creds");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        
        Map<String, ICredentials> credsMap = new HashMap<>();
        credsMap.put("maven-creds", new CredentialsUsernamePassword("testuser", "testpass"));
        MockCredentialsService mockCredentialsService = new MockCredentialsService(credsMap);
        
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        mockFramework.setCredentialsService(mockCredentialsService);

        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        String testCatalogContent = createTestCatalogJson();
        InputStream testCatalogContentStream = new ByteArrayInputStream(testCatalogContent.getBytes(StandardCharsets.UTF_8));

        Map<String, List<String>> headers = Map.of("Content-Type", List.of(MimeType.APPLICATION_JSON.toString()));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;

        MockHttpResponse<InputStream> mockResponse = new MockHttpResponse<>(testCatalogContentStream, HttpHeaders.of(headers, defaultFilter));
        mockResponse.setStatusCode(200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
    }

    @Test
    public void testGetTestCatalogWithNonExistentMavenCredentialsThrowsError() throws Exception {
        // Given...
        String streamName = "testStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockOBR mockObr = new MockOBR("dev.galasa", "dev.galasa.obr", "0.1.0");
        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
        mockStream.setDescription("Test stream");
        mockStream.setMavenRepositoryUrl("http://myrepo.com/maven");
        mockStream.setTestCatalogUrl("http://myrepo.com/testcatalog.json");
        mockStream.setMavenSecretName("maven-creds");
        mockStream.setObrs(List.of(mockObr));
        mockStreams.add(mockStream);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        
        Map<String, ICredentials> credsMap = new HashMap<>();
        MockCredentialsService mockCredentialsService = new MockCredentialsService(credsMap);
        
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        mockFramework.setCredentialsService(mockCredentialsService);

        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService("framework");

        MockEnvironment env = FilledMockEnvironment.createTestEnvironment();

        String testCatalogContent = createTestCatalogJson();
        InputStream testCatalogContentStream = new ByteArrayInputStream(testCatalogContent.getBytes(StandardCharsets.UTF_8));

        Map<String, List<String>> headers = Map.of("Content-Type", List.of(MimeType.APPLICATION_JSON.toString()));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;

        MockHttpResponse<InputStream> mockResponse = new MockHttpResponse<>(testCatalogContentStream, HttpHeaders.of(headers, defaultFilter));
        mockResponse.setStatusCode(200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, mockIConfigurationPropertyStoreService, mockHttpClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName + "/testcatalog", headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        // Then...
        ServletOutputStream outStream = servletResponse.getOutputStream();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5093, "GAL5093E");
    }
}
