/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.streams.internal.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import dev.galasa.framework.api.beans.generated.Stream;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.HttpMethod;
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

public class StreamsByNameRouteTest extends BaseServletTest {

    @Test
    public void testGetStreamsByNameStreamReturnsNotFound() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockStreamsService mockStreamsService = new MockStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/"+streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(output, 5420, "GAL5420E");
    }

    @Test
    public void testGetStreamsByNameStreamReturnsNotFoundWithAStreamPresent() throws Exception {

        // Given...
        String streamName = "streamThatIsNotPresent";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
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
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(output, 5420, "GAL5420E");
    }

    @Test
    public void testGetStreamsByAMatchingNameReturnsSingleStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName(streamName);
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();
        Stream streamGotBack = gson.fromJson(output, Stream.class);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        {
            assertThat(streamGotBack.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
            assertThat(streamGotBack.getmetadata().getdescription()).isEqualTo("This is a dummy test stream");
            assertThat(streamGotBack.getmetadata().getname()).isEqualTo(streamName);
        }
    }

    @Test
    public void testGetStreamsByAMatchingNameWithMultipleStreamsReturnsRequiredStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream2";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        MockStream mockStream2 = new MockStream();
        mockStream2.setName("fakeStream2");
        mockStream2.setDescription("This is a dummy test stream for stream 2");
        mockStream2.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream2.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream2.setObrs(List.of(mockObr));

        MockStream mockStream3 = new MockStream();
        mockStream3.setName("fakeStream3");
        mockStream3.setDescription("This is a dummy test stream for stream 3");
        mockStream3.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream3.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream3.setObrs(List.of(mockObr));

        mockStreams.add(mockStream);
        mockStreams.add(mockStream2);
        mockStreams.add(mockStream3);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();
        Stream streamGotBack = gson.fromJson(output, Stream.class);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        {
            assertThat(streamGotBack.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
            assertThat(streamGotBack.getmetadata().getdescription()).isEqualTo("This is a dummy test stream for stream 2");
            assertThat(streamGotBack.getmetadata().getname()).isEqualTo(streamName);
        }

    }

    @Test
    public void testGetStreamsByAMatchingNameButMissingMavenUrlReturnsRequiredStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl(null);
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();
        Stream streamGotBack = gson.fromJson(output, Stream.class);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        {
            assertThat(streamGotBack.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
            assertThat(streamGotBack.getmetadata().getdescription()).isEqualTo("This is a dummy test stream");
            assertThat(streamGotBack.getmetadata().getname()).isEqualTo(streamName);
            assertThat(streamGotBack.getdata().getrepository().geturl()).isNull();
        }

    }

    @Test
    public void testGetStreamsByAMatchingNameButMissingTestCatalogUrlReturnsRequiredStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl(null);
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();
        Stream streamGotBack = gson.fromJson(output, Stream.class);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        {
            assertThat(streamGotBack.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
            assertThat(streamGotBack.getmetadata().getdescription()).isEqualTo("This is a dummy test stream");
            assertThat(streamGotBack.getmetadata().getname()).isEqualTo(streamName);
            assertThat(streamGotBack.getdata().getTestCatalog().geturl()).isNull();
        }

    }

    @Test
    public void testGetStreamsByAMatchingNameButMissingObrsReturnsRequiredStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl(null);
        mockStream.setObrs(null);

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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        String output = outStream.toString();
        Stream streamGotBack = gson.fromJson(output, Stream.class);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        {
            assertThat(streamGotBack.getApiVersion()).isEqualTo("galasa-dev/v1alpha1");
            assertThat(streamGotBack.getmetadata().getdescription()).isEqualTo("This is a dummy test stream");
            assertThat(streamGotBack.getmetadata().getname()).isEqualTo(streamName);
        }

    }

    @Test
    public void testGetStreamsByNameRouteThrowsInternalServletException() throws Exception {

        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<IStream> mockStreams = new ArrayList<>();
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doGet(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(outStream.toString()).contains("GAL5000E", "Error occurred when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");

    }


//      DELETE STREAM BY NAME TESTS     ////

    @Test
    public void testDeleteStreamWithMultipleStreamsDeletesCorrectStream() throws Exception {

        // Given...
        String streamName = "fakeStream2";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        MockStream mockStream2 = new MockStream();
        mockStream2.setName("fakeStream2");
        mockStream2.setDescription("This is a dummy test stream for stream 2");
        mockStream2.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream2.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream2.setObrs(List.of(mockObr));

        MockStream mockStream3 = new MockStream();
        mockStream3.setName("fakeStream3");
        mockStream3.setDescription("This is a dummy test stream for stream 3");
        mockStream3.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream3.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream3.setObrs(List.of(mockObr));

        mockStreams.add(mockStream);
        mockStreams.add(mockStream2);
        mockStreams.add(mockStream3);

        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockIConfigurationPropertyStoreService mockIConfigurationPropertyStoreService = new MockIConfigurationPropertyStoreService(
                "framework");

        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");

        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env,
                mockIConfigurationPropertyStoreService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        mockServlet.init();
        mockServlet.doDelete(mockRequest, servletResponse);

        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(mockStreams).hasSize(2);

        //Iterate over list of streams and check that deleted stream does not exists anymore
        for (IStream stream : mockStreams) {
            assertThat(stream.getName()).isNotEqualTo(streamName);
        }

    }

    @Test
    public void testDeleteStreamsByAMatchingNameNotFound() throws Exception {

        // Given...
        String streamName = "someunkownstream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        String OBR_GROUP_ID = "dev.galasa";
        String OBR_ARTIFACT_ID = "dev.galasa.ivts.obr";
        String OBR_VERSION = "0.41.0";
        MockOBR mockObr = new MockOBR(OBR_GROUP_ID, OBR_ARTIFACT_ID, OBR_VERSION);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        mockStream.setTestCatalogUrl("http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml");
        mockStream.setObrs(List.of(mockObr));

        MockStream mockStream2 = new MockStream();
        mockStream2.setName("fakeStream2");
        mockStream2.setDescription("This is a dummy test stream for stream 2");
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

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        mockServlet.init();
        mockServlet.doDelete(mockRequest, servletResponse);
        String output = outStream.toString();

        assertThat(servletResponse.getStatus()).isEqualTo(404);
        checkErrorStructure(output, 5420, "GAL5420E");
    }

    @Test
        public void testDeleteStreamsByNameRouteThrowsInternalServletException() throws Exception {
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);
        
        // Assume the deletion code uses a prefix constant like "testStream.".
        String testStreamPrefix = "test.stream."+ streamName + ".";
        
        // Create and pre-populate the CPS mock with 5 properties for the stream.
        MockIConfigurationPropertyStoreService cps = new MockIConfigurationPropertyStoreService("framework");
        cps.setProperty(testStreamPrefix + "name", "fakeStream");
        cps.setProperty(testStreamPrefix + "description", "This is a dummy test stream");
        cps.setProperty(testStreamPrefix + "location", "http://mymavenrepo.host/testmaterial");

        // Configure the mock CPS to throw an exception when attempting to delete property "description"
        cps.setThrowOnDeleteForKey(testStreamPrefix + "description", true);
        
        MockStream mockStream = new MockStream();
        mockStream.setName("fakeStream");
        mockStream.setDescription("This is a dummy test stream");
        mockStream.setMavenRepositoryUrl("http://mymavenrepo.host/testmaterial");
        
        // Set up the remaining mocks
        List<IStream> mockStreams = new ArrayList<>();
        mockStreams.add(mockStream);
        MockStreamsService mockStreamsService = new MockStreamsService(mockStreams);
        
        mockStreamsService.setThrowException(true);
        
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockStreamsService);
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, "preferred_username");
        
        // Create the servlet with our CPS mock that already contains the test properties.
        MockStreamsServlet mockServlet = new MockStreamsServlet(mockFramework, env, cps);
        
        // Create a DELETE request
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + streamName, headerMap);
        mockRequest.setMethod(HttpMethod.DELETE.toString());
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();
        
        // When...
        mockServlet.init();
        mockServlet.doDelete(mockRequest, servletResponse);
        
        // Assert that the DELETE route returns an error response.
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        assertThat(outStream.toString())
            .contains("GAL5000E", "Error occurred when trying to access the endpoint. Report the problem to your Galasa Ecosystem owner.");
        
        // Finally, verify that the CPS properties were restored after rollback.
        Map<String, String> restoredProperties = cps.getPrefixedProperties(testStreamPrefix);
        assertThat(restoredProperties).containsEntry(testStreamPrefix + "name", "fakeStream");
        assertThat(restoredProperties).containsEntry(testStreamPrefix + "description", "This is a dummy test stream");
        assertThat(restoredProperties).containsEntry(testStreamPrefix + "location", "http://mymavenrepo.host/testmaterial");
  }

}
