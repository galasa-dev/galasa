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
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.common.mocks.MockIConfigurationPropertyStoreService;
import dev.galasa.framework.api.streams.mocks.MockStreamsServlet;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockIStreamsService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockStream;
import dev.galasa.framework.spi.streams.IStream;

public class StreamsByNameRouteTest extends BaseServletTest {

    @Test
    public void testGetStreamsByNameStreamReturnsNotFound() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        MockIStreamsService mockIStreamsService = new MockIStreamsService(new ArrayList<>());
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockIStreamsService);
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

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockstream = new MockStream("fakeStream", "This is a dummy test stream",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);

        mockStreams.add(mockstream);

        MockIStreamsService mockIStreamsService = new MockIStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockIStreamsService);
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
    public void testGetStreamsByAMtachingNameReturnsSingleStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockstream = new MockStream("fakeStream", "This is a dummy test stream",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);

        mockStreams.add(mockstream);

        MockIStreamsService mockIStreamsService = new MockIStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockIStreamsService);
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
    public void testGetStreamsByAMtachingNameWithMultipleStreamsReturnsRequiredStreamOK() throws Exception {

        // Given...
        String streamName = "fakeStream2";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockstream = new MockStream("fakeStream", "This is a dummy test stream",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);
        MockStream mockstream2 = new MockStream("fakeStream2", "This is a dummy test stream for stream 2",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);
        MockStream mockstream3 = new MockStream("fakeStream3", "This is a dummy test stream for stream 3",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);

        mockStreams.add(mockstream);
        mockStreams.add(mockstream2);
        mockStreams.add(mockstream3);

        MockIStreamsService mockIStreamsService = new MockIStreamsService(mockStreams);
        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockIStreamsService);
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
    public void testGetSTreamsByNameRouteThrowsInternalServletException() throws Exception {

        String streamName = "fakeStream";
        Map<String, String> headerMap = Map.of("Authorization", "Bearer " + BaseServletTest.DUMMY_JWT);

        List<IStream> mockStreams = new ArrayList<>();
        MockStream mockstream = new MockStream("fakeStream", "This is a dummy test stream",
                "http://mymavenrepo.host/testmaterial",
                "http://mymavenrepo.host/testmaterial/com.ibm.zosadk.k8s/com.ibm.zosadk.k8s.obr/0.1.0-SNAPSHOT/testcatalog.yaml",
                "mvn:dev.galasa/dev.galasa.ivts.obr/0.41.0/obr", true);

        mockStreams.add(mockstream);

        MockIStreamsService mockIStreamsService = new MockIStreamsService(mockStreams);

        mockIStreamsService.setThrowException(true);

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME);
        MockFramework mockFramework = new MockFramework(mockRBACService, mockIStreamsService);
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

}
