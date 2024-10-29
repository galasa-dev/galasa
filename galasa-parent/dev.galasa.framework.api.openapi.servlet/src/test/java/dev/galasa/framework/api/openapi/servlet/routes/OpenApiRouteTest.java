/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.openapi.servlet.routes;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.common.EnvironmentVariables;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.api.openapi.servlet.OpenApiServlet;
import dev.galasa.framework.api.openapi.servlet.mocks.MockOpenApiServlet;
import dev.galasa.framework.spi.utils.GalasaGson;

public class OpenApiRouteTest {

    private GalasaGson gson = new GalasaGson();

    private void checkJsonContents(String jsonContents, String apiServerUrl) {
        JsonObject openApiJson = gson.fromJson(jsonContents, JsonObject.class);
        assertThat(openApiJson.has("openapi")).isTrue();
        assertThat(openApiJson.has("info")).isTrue();
        assertThat(openApiJson.has("paths")).isTrue();
        assertThat(openApiJson.has("servers")).isTrue();

        JsonArray openApiServers = openApiJson.get("servers").getAsJsonArray();
        assertThat(openApiServers.size()).isEqualTo(1);

        JsonObject server = openApiServers.get(0).getAsJsonObject();
        assertThat(server.has("url")).isTrue();
        assertThat(server.get("url").getAsString()).isEqualTo(apiServerUrl);

        JsonObject openApiInfo = openApiJson.get("info").getAsJsonObject();
        assertThat(openApiInfo.has("version")).isTrue();
        assertThat(openApiInfo.has("title")).isTrue();
        assertThat(openApiInfo.get("title").getAsString()).isEqualTo("Galasa Ecosystem API");

        JsonObject openApiPaths = openApiJson.get("paths").getAsJsonObject();
        assertThat(openApiPaths.has("/bootstrap")).isTrue();
        assertThat(openApiPaths.has("/auth/tokens")).isTrue();
        assertThat(openApiPaths.has("/openapi")).isTrue();
    }

    private void checkYamlContents(String yamlContents, String apiServerUrl) {
        assertThat(yamlContents).contains(
            "openapi:",
            "version",
            "title: Galasa Ecosystem API",
            "url: '" + apiServerUrl + "'",
            "paths:",
            "/bootstrap:",
            "/auth/tokens:"
        );
    }

    @Test
    public void testGetOpenApiServesYamlSpecificationOk() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "application/yaml");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkYamlContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithJsonContentTypeServesJsonDocumentOk() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);

        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "application/json");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);

        // Check a few key parts of the OpenAPI specification were returned and that the JSON string can be serialised into a JSON object
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithoutContentTypeReturnsJsonByDefault() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);

        // No "Accept" header is set
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithBlankContentTypeThrowsError() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Interpret an empty "Accept" header as accepting nothing, which is unsupported
        request.setHeader("Accept", "");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(406);
        assertThat(outputStream.toString()).contains("GAL5406E", "Unsupported 'Accept' header value set");
    }

    @Test
    public void testGetOpenApiWithInvalidContentTypeThrowsError() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "this-is-not-a-valid-content-type!");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(406);
        assertThat(outputStream.toString()).contains("GAL5406E", "Unsupported 'Accept' header value set");
    }

    @Test
    public void testGetOpenApiWithMultipleAcceptedContentTypesReturnsHighestPriorityType() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Set application/json to have the higher quality value, so a JSON response should be returned
        request.setHeader("Accept", "application/yaml;q=0.1, application/json;q=0.7");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithMultipleAcceptedContentTypesReturnsValidContentType() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // application/yaml is the only valid type, so it should be returned
        request.setHeader("Accept", "application/yaml;q=0.1, not-a-valid-type!;q=0.7");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkYamlContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithApplicationWildcardTypeReturnsJsonByDefault() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "application/*");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithWildcardTypeReturnsJsonByDefault() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "*/*");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithEqualContentTypePrioritiesReturnsFirstMatch() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Both priorities are 1 if quality values aren't given, in this case a YAML response should be returned
        request.setHeader("Accept", "application/yaml, application/json");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkYamlContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithExplicitEqualContentTypePrioritiesReturnsFirstMatch() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setHeader("Accept", "application/json;q=0.5, application/yaml;q=0.5");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithInvalidContentTypeQualityValueDefaultsToOne() throws Exception {
        // Given...
        String apiServerUrl = "https://my-api-server";
        MockEnvironment env = new MockEnvironment();
        env.setenv(EnvironmentVariables.GALASA_EXTERNAL_API_URL, apiServerUrl);

        OpenApiServlet servlet = new MockOpenApiServlet(env);
        MockHttpServletRequest request = new MockHttpServletRequest("");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // The quality value for application/yaml is invalid, so it will become 0
        request.setHeader("Accept", "application/yaml;q=this-will-become-0, application/json;q=0.1");

        // When...
        servlet.init();
        servlet.doGet(request, response);

        // Then...
        ServletOutputStream outputStream = response.getOutputStream();
        assertThat(response.getStatus()).isEqualTo(200);
        checkJsonContents(outputStream.toString(), apiServerUrl);
    }

    @Test
    public void testGetOpenApiWithMissingApiServerUrlThrowsError() throws Exception {
        // Given...
        // Don't set the API server URL environment variable
        MockEnvironment env = new MockEnvironment();

        OpenApiServlet servlet = new MockOpenApiServlet(env);

        // When...
        ServletException thrown = catchThrowableOfType(() -> {
            servlet.init();
        }, ServletException.class);

        // Then...
        assertThat(thrown.getMessage()).contains("Required environment variable", EnvironmentVariables.GALASA_EXTERNAL_API_URL, "not set");
    }
}
