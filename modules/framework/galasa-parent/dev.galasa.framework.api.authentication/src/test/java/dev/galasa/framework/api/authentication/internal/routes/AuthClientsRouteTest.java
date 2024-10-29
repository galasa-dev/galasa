/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.spi.utils.GalasaGson;

public class AuthClientsRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    @Test
    public void testAuthClientsPostRequestWithNoCreatedClientReturnsError() throws Exception {
        // Given...
        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer.url");

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockDexGrpcClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/clients", "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5000,
        // "error_message" : "GAL5000E: Error occurred when trying to access the
        // endpoint. Report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5000, "GAL5000E", "Error occurred when trying to access the endpoint");
    }

    @Test
    public void testAuthClientsPostRequestReturnsClient() throws Exception {
        // Given...
        String clientId = "my-client-id";
        String clientSecret = "my-client-secret"; // Mock value, not a secret //pragma: allowlist secret
        String redirectUri = "http://my.app/callback";

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer.url", clientId, clientSecret, redirectUri);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockDexGrpcClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/clients", "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "client_id": "my-client-id",
        // }
        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("client_id", clientId);

        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJson));
    }

    @Test
    public void testAuthClientsPostRequestWithGoodAcceptHeaderReturnsClient() throws Exception {
        // Given...
        String clientId = "my-client-id";
        String clientSecret = "my-client-secret"; // Mock value, not a secret //pragma: allowlist secret
        String redirectUri = "http://my.app/callback";

        DexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer.url", clientId, clientSecret, redirectUri);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockDexGrpcClient);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/clients", "", "POST");

        mockRequest.setHeader("Accept", "text/*, application/json");

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "client_id": "my-client-id",
        // }
        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("client_id", clientId);

        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJson));
    }
}
