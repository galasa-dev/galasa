/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.internal.routes;

import static org.assertj.core.api.Assertions.*;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonObject;

import dev.galasa.framework.api.authentication.mocks.MockAuthenticationServlet;
import dev.galasa.framework.api.authentication.mocks.MockOidcProvider;
import dev.galasa.framework.api.common.BaseServletTest;
import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.auth.spi.IDexGrpcClient;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.utils.GalasaGson;

public class AuthRouteTest extends BaseServletTest {

    private static final GalasaGson gson = new GalasaGson();

    private String buildRequestPayload(String clientId, String refreshToken, String authCode) {
        return buildRequestPayload(clientId, refreshToken, authCode, null);
    }

    private String buildRequestPayload(String clientId, String refreshToken, String authCode, String description) {
        JsonObject requestPayload = new JsonObject();
        if (clientId != null) {
            requestPayload.addProperty("client_id", clientId);
        }

        if (refreshToken != null) {
            requestPayload.addProperty("refresh_token", refreshToken);
        }

        if (authCode != null) {
            requestPayload.addProperty("code", authCode);
        }

        if (description != null) {
            requestPayload.addProperty("description", description);
        }

        String requestPayloadStr = gson.toJson(requestPayload);
        return requestPayloadStr;
    }

    private String buildTokenResponse(String jwt, String refreshToken) {
        JsonObject responseJson = new JsonObject();

        responseJson.addProperty("id_token", jwt);
        responseJson.addProperty("refresh_token", refreshToken);

        return gson.toJson(responseJson);
    }

    @Test
    public void testAuthPostRequestWithEmptyRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(null, "", "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5411E: ... The request body is empty."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(411);
        checkErrorStructure(outStream.toString(), 5411, "GAL5411E");
    }

    @Test
    public void testAuthPostRequestWithInvalidRequestPayloadReturnsBadRequestError() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        // Payload with a missing "refresh_token" field
        String payload = buildRequestPayload("dummy-id", null, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5062,
        // "error_message" : "GAL5062E: Invalid request body provided. ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5062, "GAL5062E");
    }

    @Test
    public void testAuthPostRequestWithValidRefreshTokenRequestPayloadReturnsJWT() throws Exception {
        // Given...
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String refreshToken = "here-is-a-token";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        IDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, clientSecret, "http://callback");

        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, refreshToken, null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));
        assertThat(mockAuthStoreService.getTokens()).isEmpty();
    }

    @Test
    public void testAuthPostRequestWithDescriptionInRequestReturnsJWTAndStoresTokenInAuthStore() throws Exception {
        // Given...
        String tokenDescription = "my new galasactl token for my macOS laptop";
        String userName = "JohnDoe";

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String dummyJwt = JWT.create()
            .withSubject("validUserId")
            .withIssuedAt(Instant.EPOCH)
            .withClaim("name", userName)
            .sign(mockJwtSigningAlgorithm);

        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);
        IDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode, tokenDescription);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "ey....",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));

        // A record of the newly-created token should have been added to the auth store
        List<IInternalAuthToken> tokens = mockAuthStoreService.getTokens();
        assertThat(tokens).hasSize(1);

        IInternalAuthToken newToken = tokens.get(0);
        assertThat(newToken.getDescription()).isEqualTo(tokenDescription);
        assertThat(newToken.getCreationTime()).isEqualTo(tokenCreationTime);
        assertThat(newToken.getOwner().getLoginId()).isEqualTo(userName);
    }

    @Test
    public void testAuthPostRequestWithValidAuthCodeRequestPayloadReturnsJWT() throws Exception {
        // Given...
        String dummyJwt = "this-is-a-jwt";
        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret";// Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        IDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "jwt": "this-is-a-jwt",
        //   "refresh_token": "this-is-a-refresh-token",
        // }
        JsonObject expectedJsonObject = new JsonObject();
        expectedJsonObject.addProperty("jwt", dummyJwt);
        expectedJsonObject.addProperty("refresh_token", dummyRefreshToken);

        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(gson.toJson(expectedJsonObject));

        // No new token records should have been added to the auth store since no token description was provided
        assertThat(mockAuthStoreService.getTokens()).isEmpty();
    }

    @Test
    public void testAuthPostRequestWithFailingAuthStoreReturnsError() throws Exception {
        // Given...
        String tokenDescription = "my new galasactl token for my macOS laptop";
        String userName = "JohnDoe";

        Algorithm mockJwtSigningAlgorithm = Algorithm.HMAC256("dummysecret");
        String dummyJwt = JWT.create()
            .withSubject("validUserId")
            .withIssuedAt(Instant.EPOCH)
            .withClaim("name", userName)
            .sign(mockJwtSigningAlgorithm);

        String dummyRefreshToken = "this-is-a-refresh-token";
        String mockResponseJson = buildTokenResponse(dummyJwt, dummyRefreshToken);

        HttpResponse<String> mockResponse = new MockHttpResponse<String>(mockResponseJson);

        String clientId = "dummy-id";
        String clientSecret = "asecret"; // Mock value, not a secret //pragma: allowlist secret
        String authCode = "thisisacode";

        String callbackUri = "http://api.host/auth/callback";
        String issuerUrl = "http://dummy.host";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        IDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient(issuerUrl, clientId, clientSecret, callbackUri);

        Instant tokenCreationTime = Instant.now();
        MockTimeService mockTimeService = new MockTimeService(tokenCreationTime);
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        mockAuthStoreService.setThrowException(true);

        MockFramework mockFramework = new MockFramework(mockAuthStoreService);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient, mockFramework);

        String payload = buildRequestPayload(clientId, null, authCode, tokenDescription);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5056, "GAL5056E", "Internal server error occurred when storing the new Galasa token with description", tokenDescription);
    }

    @Test
    public void testAuthPostRequestThrowsUnexpectedErrorReturnsServerError() throws Exception {
        // Given...
        MockOidcProvider mockOidcProvider = new MockOidcProvider();
        mockOidcProvider.setThrowException(true);

        String clientId = "myclient";
        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, "secret", "http://callback");

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
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
    public void testAuthPostRequestWithTokenErrorResponseReturnsServerError() throws Exception {
        // Given...
        JsonObject dummyErrorJson = new JsonObject();
        dummyErrorJson.addProperty("error", "oh no, something went wrong!");
        HttpResponse<String> mockResponse = new MockHttpResponse<String>(gson.toJson(dummyErrorJson));

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        String clientId = "myclient";
        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer", clientId, "secret", "http://callback");

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider, mockDexGrpcClient);

        String payload = buildRequestPayload("dummy-id", "here-is-a-token", null);
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", payload, "POST");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code": 5055,
        //   "error_message": "GAL5055E: ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5055, "GAL5055E", "Failed to get a JWT and a refresh token from the Galasa Dex server", "The Dex server did not respond with a JWT and refresh token");
    }

    @Test
    public void testAuthGetRequestWithClientIdAndCallbackUrlRedirectsToConnector() throws Exception {
        // Given...
        String redirectLocation = "http://my.connector/auth";
        String clientIp = "123.456.789.010";
        String clientId = "my-client";
        String clientCallbackUrl = "http://my.app";

        MockOidcProvider mockOidcProvider = new MockOidcProvider(redirectLocation);

        MockIDynamicStatusStoreService mockDss = new MockIDynamicStatusStoreService();
        MockFramework mockFramework = new MockFramework(mockDss);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider);
        servlet.setFramework(mockFramework);

        Map<String, String[]> queryParams = Map.of(
                "client_id", new String[] { clientId }, "callback_url", new String[] { clientCallbackUrl }
        );

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        mockRequest.setRemoteAddr(clientIp);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(302);
        assertThat(servletResponse.getHeader("Location")).isEqualTo(redirectLocation);
        assertThat(mockDss.data).containsValue(clientCallbackUrl);
    }

    @Test
    public void testAuthGetRequestWithEmptyReturnedLocationHeaderReturnsError() throws Exception {
        // Given...
        String clientIp = "123.456.789.010";

        // No "Location" returned from the issuer, will not be able to redirect anywhere to authenticate
        Map<String, List<String>> headers = new HashMap<>();
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<String> mockResponse = new MockHttpResponse<String>("", HttpHeaders.of(headers, defaultFilter));

        MockOidcProvider mockOidcProvider = new MockOidcProvider(mockResponse);

        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockOidcProvider);

        Map<String, String[]> queryParams = Map.of(
                "client_id", new String[] { "my-client" }, "callback_url", new String[] { "http://my.app" }
        );

        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        mockRequest.setRemoteAddr(clientIp);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        //   "error_code": 5054,
        //   "error_message": "GAL5054E: ..."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5054, "GAL5054E", "Internal server error", "The REST API server could not get the URL of the authentication provider (e.g. GitHub/LDAP) from the Galasa Dex component");
    }

    @Test
    public void testAuthGetRequestWithMissingClientIdReturnsBadRequest() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        Map<String, String[]> queryParams = Map.of("callback_url", new String[] { "http://my.callback.url" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occurred when trying to execute request
        // "/auth". Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occurred when trying to execute request");
    }

    @Test
    public void testAuthGetRequestWithMissingCallbackUrlReturnsBadRequest() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        Map<String, String[]> queryParams = Map.of("client_id", new String[] { "my-client-id" });
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occurred when trying to execute request
        // "/auth". Check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occurred when trying to execute request");
    }

    @Test
    public void testAuthGetRequestWithMissingParamsReturnsBadRequest() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        Map<String, String[]> queryParams = new HashMap<>();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occurred when trying to execute request
        // "/auth". Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occurred when trying to execute request");
    }

    @Test
    public void testAuthGetRequestWithBadCallbackUrlReturnsBadRequest() throws Exception {
        // Given...
        MockAuthenticationServlet servlet = new MockAuthenticationServlet();

        Map<String, String[]> queryParams = Map.of(
            "client_id", new String[] { "my-client-id" },
            "callback_url", new String[] { "!!!not-a-valid-url?!" }
        );
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(queryParams, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        // Expecting this json:
        // {
        // "error_code" : 5400,
        // "error_message" : "GAL5400E: Error occurred when trying to execute request
        // "/auth". Please check your request parameters or report the problem to your Galasa Ecosystem owner."
        // }
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5400, "GAL5400E", "Error occurred when trying to execute request");
    }

    @Test
    public void testAuthServletInitWithMissingRequiredEnvVarsThrowsServletException() throws Exception {
        // Given...
        MockOidcProvider mockOidcProvider = new MockOidcProvider();

        IDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://issuer");
        MockEnvironment mockEnv = new MockEnvironment();

        MockRBACService mockRBACService = FilledMockRBACService.createTestRBACService();
        MockFramework mockFramework = new MockFramework(mockRBACService);
        MockAuthenticationServlet servlet = new MockAuthenticationServlet(mockEnv, mockOidcProvider, mockDexGrpcClient, mockFramework, mockRBACService);

        // When...
        Throwable thrown = catchThrowable(() -> {
            servlet.init();
        });

        // Then...
        assertThat(thrown).isInstanceOf(ServletException.class);
    }
}