package dev.galasa.framework.api.common;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;

public class TestResponseBuilder {

    @Test
    public void testBuildResponseWritesResponseOK() throws Exception {
        // Given...
        String origin = "https://my.server.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, origin);
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "text/plain";
        String content = "this is a response!";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, content, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getOutputStream().toString()).isEqualTo(content);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
    }

    @Test
    public void testBuildResponseHeadersSetsHeadersOK() throws Exception {
        // Given...
        String origin = "https://my.server.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, origin);
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
    }

    @Test
    public void testBuildResponseHeadersWithWildcardAllowedOriginSetsHeadersOK() throws Exception {
        // Given...
        String origin = "https://my.server.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "*.server.com");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
    }

    @Test
    public void testBuildResponseHeadersWithAllAllowedOriginSetsHeadersOK() throws Exception {
        // Given...
        String origin = "https://my.server.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "*");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
    }

    @Test
    public void testBuildResponseHeadersWithMultipleAllowedOriginSetsHeadersOK() throws Exception {
        // Given...
        String origin = "https://my.server2.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "https://server1.com,*.server2.com,*.server.com");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isEqualTo(origin);
    }

    @Test
    public void testBuildResponseHeadersWithNoMatchingAllowedOriginDoesNotSetCorsHeader() throws Exception {
        // Given...
        String origin = "https://my.server2.com";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "https://server1.com,another.server2.com,*.server.com");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    public void testBuildResponseHeadersWithInvalidOriginDoesNotSetCorsHeader() throws Exception {
        // Given...
        String origin = "not a valid origin";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "*");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    public void testBuildResponseHeadersWithHttpSplittingDoesNotSetCorsHeader() throws Exception {
        // Given...
        String origin = "http://my-server.com\n\rContent-Type: text/plain";

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setenv(EnvironmentVariables.GALASA_ALLOWED_ORIGINS, "*");
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    public void testBuildResponseHeadersWithNoAllowedOriginsDoesNotSetCorsHeader() throws Exception {
        // Given...
        String origin = "https://my.server2.com";

        MockEnvironment mockEnv = new MockEnvironment();
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        Map<String, String> reqHeaders = Map.of("Origin", origin);
        MockHttpServletRequest req = new MockHttpServletRequest("/", reqHeaders);
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isNull();
    }

    @Test
    public void testBuildResponseHeadersWithNullOriginDoesNotSetCorsHeader() throws Exception {
        // Given...
        MockEnvironment mockEnv = new MockEnvironment();
        ResponseBuilder responseBuilder = new ResponseBuilder(mockEnv);

        MockHttpServletRequest req = new MockHttpServletRequest("/");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        String contentType = "application/json";

        // When...
        HttpServletResponse actualResp = responseBuilder.buildResponse(req, resp, contentType, HttpServletResponse.SC_OK);

        // Then...
        assertThat(actualResp.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(actualResp.getContentType()).isEqualTo(contentType);
        assertThat(actualResp.getHeader("Access-Control-Allow-Origin")).isNull();
    }
}
