/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64.Encoder;
import java.util.function.BiPredicate;

import javax.servlet.ServletException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

import dev.galasa.framework.api.authentication.internal.OidcProvider;
import dev.galasa.framework.api.authentication.internal.beans.JsonWebKey;
import dev.galasa.framework.api.common.mocks.MockHttpClient;
import dev.galasa.framework.api.common.mocks.MockHttpResponse;
import dev.galasa.framework.api.common.mocks.MockHttpSession;
import dev.galasa.framework.api.common.mocks.MockTimeService;
import dev.galasa.framework.spi.utils.GalasaGson;

public class OidcProviderTest {

    private static final GalasaGson gson = new GalasaGson();

    //-------------------------------------------------------------------------
    // Helper methods
    //-------------------------------------------------------------------------
    private KeyPair generateMockRsaKeyPair() throws NoSuchAlgorithmException {
        // Generate a small key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(512);

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Creates and returns a JSON object representing a JSON Web Key. The format of
     * the created JSON object is as follows:
     * {
     *   "kid": "key-id",
     *   "kty": "RSA",
     *   "alg": "RS256",
     *   "use": "sig",
     * }
     */
    private JsonObject createMockJwkObject(String keyId) {
        JsonObject jwkJson = new JsonObject();
        jwkJson.addProperty("kid", keyId);
        jwkJson.addProperty("kty", "RSA");
        jwkJson.addProperty("alg", "RS256");
        jwkJson.addProperty("use", "sig");

        return jwkJson;
    }

    /**
     * Creates and returns a JSON object representing a JSON Web Key, including the
     * RSA public key exponent ('e') and modulus ('n') used to sign the key.
     */
    private JsonObject createMockJwkObject(String keyId, RSAPublicKey publicKey) {
        JsonObject jwkJson = createMockJwkObject(keyId);

        // Both the modulus and exponent need to be Base64-URL-encoded in JWKs
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        jwkJson.addProperty("n", encoder.encodeToString(publicKey.getModulus().toByteArray()));
        jwkJson.addProperty("e", encoder.encodeToString(publicKey.getPublicExponent().toByteArray()));
        return jwkJson;
    }

    /**
     * Creates and returns a HttpResponse containing a set of JSON Web keys.
     * The format of the created response content is as follows:
     * {
     *   "keys": [
     *     {
     *       "kid": "key-id",
     *       "kty": "RSA",
     *       "alg": "RS256",
     *       "use": "sig",
     *     },
     *   ],
     * }
     */
    private HttpResponse<Object> createMockJwksResponse(String... keyIds) {
        List<JsonObject> jsonWebKeys = new ArrayList<>();

        for (String keyId : keyIds) {
            jsonWebKeys.add(createMockJwkObject(keyId));
        }

        return createMockJwksResponse(jsonWebKeys.toArray(new JsonObject[0]));
    }

    private HttpResponse<Object> createMockJwksResponse(JsonObject... jsonWebKeys) {
        JsonObject mockJwks = new JsonObject();
        JsonArray keysArray = new JsonArray();

        for (JsonObject key : jsonWebKeys) {
            keysArray.add(key);
        }
        mockJwks.add("keys", keysArray);

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockJwks));
        return mockResponse;
    }

    private HttpResponse<Object> createMockOidcDiscoveryResponse() {
        JsonObject mockOidcConfig = new JsonObject();
        mockOidcConfig.addProperty("authorization_endpoint", "http://my-issuer/auth");
        mockOidcConfig.addProperty("token_endpoint", "http://my-issuer/token");
        mockOidcConfig.addProperty("jwks_uri", "http://my-issuer/keys");

        HttpResponse<Object> mockOidcDiscoveryResponse = new MockHttpResponse<Object>(gson.toJson(mockOidcConfig));
        return mockOidcDiscoveryResponse;
    }

    //-------------------------------------------------------------------------
    // Test methods
    //-------------------------------------------------------------------------
    @Test
    public void testCreateOidcProviderWithInvalidIssuerUrlThrowsError() throws Exception {
        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("not a valid issuer url", null), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5059", "Invalid Galasa Dex server URL provided");
    }

    @Test
    public void testCreateOidcProviderWithInvalidAuthEndpointThrowsError() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "not a valid url");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("http://my.server", mockHttpClient), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5060E", "Invalid OpenID Connect URL");
    }

    @Test
    public void testCreateOidcProviderWithAuthEndpointDifferentSchemeThrowsError() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "nothttp://my.server/auth");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("http://my.server", mockHttpClient), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5061E", "does not match the expected Dex server scheme or host");
    }

    @Test
    public void testCreateOidcProviderWithAuthEndpointOnDifferentHostThrowsError() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "http://my-malicious-server/auth");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("http://my.server", mockHttpClient), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5061E", "does not match the expected Dex server scheme or host");
    }

    @Test
    public void testCreateOidcProviderWithTokensEndpointOnDifferentHostThrowsError() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "http://my.server/auth");
        mockOpenIdConfig.addProperty("token_endpoint", "http://my-malicious-server/tokens");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("http://my.server", mockHttpClient), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5061E", "does not match the expected Dex server scheme or host");
    }

    @Test
    public void testCreateOidcProviderWithJwksEndpointOnDifferentHostThrowsError() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "http://my.server/auth");
        mockOpenIdConfig.addProperty("token_endpoint", "http://my.server/tokens");
        mockOpenIdConfig.addProperty("jwks_uri", "http://my-malicious-server/keys");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        // When...
        ServletException thrown = catchThrowableOfType(() -> new OidcProvider("http://my.server", mockHttpClient), ServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5061E", "does not match the expected Dex server scheme or host");
    }

    @Test
    public void testTokenPostWithRefreshTokenValidRequestReturnsValidResponse() throws Exception {
        // Given...
        JsonObject mockJwtJson = new JsonObject();
        mockJwtJson.addProperty("id_token", "this-is-a-jwt");

        HttpResponse<Object> mockTokenResponse = new MockHttpResponse<Object>(gson.toJson(mockJwtJson));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        String clientId = "galasa";
        String clientSecret = "abc";
        String refreshToken = "thisisarefreshtoken";

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockTokenResponse);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(clientId, clientSecret, refreshToken);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response.body()).isEqualTo(mockTokenResponse.body());
    }

    @Test
    public void testTokenPostWithAuthCodeValidRequestReturnsValidResponse() throws Exception {
        // Given...
        JsonObject mockJwtJson = new JsonObject();
        mockJwtJson.addProperty("id_token", "this-is-a-jwt");

        HttpResponse<Object> mockTokenResponse = new MockHttpResponse<Object>(gson.toJson(mockJwtJson));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        String clientId = "galasa";
        String clientSecret = "abc";
        String authCode = "thisisacode";
        String redirectUri = "http://mock.galasa.server/auth/callback";

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockTokenResponse);

        // When...
        HttpResponse<String> response = oidcProvider.sendTokenPost(clientId, clientSecret, authCode, redirectUri);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response.body()).isEqualTo(mockTokenResponse.body());
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithCachedKeysReturnsCachedKey() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";
        HttpResponse<Object> mockResponse = createMockJwksResponse("thisisakey", "thisisanotherkey", targetKeyId);

        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockResponse);

        // When...
        oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Change the set of keys returned by the issuer
        mockHttpClient.setMockResponse(createMockJwksResponse("adifferentkey"));

        // Other calls shouldn't send a request to the issuer as long as the refresh interval hasn't elapsed
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key.getKeyId()).isEqualTo(targetKeyId);
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithCachedKeysRefreshesCache() throws Exception {
        // Given...
        String firstTargetKeyId = "iwantthiskey";
        String secondTargetKeyId = "iwantthiskeynow";

        HttpResponse<Object> mockResponse = createMockJwksResponse("thisisakey", "thisisanotherkey", firstTargetKeyId);

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        // First cache refresh
        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockResponse);

        // When...
        assertThat(oidcProvider.getJsonWebKeyByKeyId(firstTargetKeyId).getKeyId()).isEqualTo(firstTargetKeyId);

        // The second target key isn't returned in the first response, so should be null
        assertThat(oidcProvider.getJsonWebKeyByKeyId(secondTargetKeyId)).isNull();

        // Bump up the current time by a day to force a cache refresh and update the mock response
        mockTimeService.setCurrentTime(Instant.now().plus(1, ChronoUnit.DAYS));
        mockHttpClient.setMockResponse(createMockJwksResponse(secondTargetKeyId));

        // Then...
        assertThat(oidcProvider.getJsonWebKeyByKeyId(secondTargetKeyId).getKeyId()).isEqualTo(secondTargetKeyId);
        assertThat(oidcProvider.getJsonWebKeyByKeyId(firstTargetKeyId)).isNull();
    }

    @Test
    public void testGetJsonWebKeyByKeyIdReturnsOk() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        HttpResponse<Object> mockResponse = createMockJwksResponse("thisisakey", targetKeyId, "thisisanotherkey");

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockResponse);

        // When...
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key).isNotNull();
        assertThat(key.getKeyId()).isEqualTo(targetKeyId);
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithBadKeysResponseReturnsNull() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("{}", 200);

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        MockTimeService mockTimeService = new MockTimeService(Instant.now());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockResponse);

        // When...
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key).isNull();
    }

    @Test
    public void testGetJsonWebKeyByKeyIdWithNoMatchingKeyReturnsNull() throws Exception {
        // Given...
        String targetKeyId = "iwantthiskey";

        HttpResponse<Object> mockResponse = createMockJwksResponse("thisisakey", "thisisanotherkey");

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockResponse);

        // When...
        JsonWebKey key = oidcProvider.getJsonWebKeyByKeyId(targetKeyId);

        // Then...
        assertThat(key).isNull();
    }

    @Test
    public void testIsJwtValidWithBadTokenFormatReturnsFalse() throws Exception {
        // Given...
        // A JWT that does not have 3 parts (should be of the form "header.payload.signature")
        String testJwt = "thisisabadjwt";

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        boolean result = oidcProvider.isJwtValid(testJwt);

        // Then...
        assertThat(result).isFalse();
    }

    @Test
    public void testIsJwtValidWithExpiredJwtReturnsFalse() throws Exception {
        // Given...
        String issuer = "http://dummy-issuer";
        String keyId = "mock-key";

        // Generate an RSA key pair to sign the mock JWT
        KeyPair mockKeyPair = generateMockRsaKeyPair();
        RSAPublicKey mockPublicKey =  (RSAPublicKey) mockKeyPair.getPublic();
        RSAPrivateKey mockPrivateKey =  (RSAPrivateKey) mockKeyPair.getPrivate();

        // Create the JSON Web Key that will be returned from the issuer
        JsonObject mockJwk = createMockJwkObject(keyId, mockPublicKey);
        HttpResponse<Object> mockJwkResponse = createMockJwksResponse(mockJwk);

        // Create the expired JWT
        String expiredJwt = JWT.create()
            .withIssuer(issuer)
            .withKeyId(keyId)
            .withExpiresAt(Instant.EPOCH)
            .sign(Algorithm.RSA256(mockPublicKey, mockPrivateKey));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        OidcProvider oidcProvider = new OidcProvider(issuer, mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockJwkResponse);

        // When...
        boolean result = oidcProvider.isJwtValid(expiredJwt);

        // Then...
        assertThat(result).isFalse();
    }

    @Test
    public void testIsJwtValidWithInvalidSignatureReturnsFalse() throws Exception {
        // Given...
        String issuer = "http://dummy-issuer";
        String targetKeyId = "i-want-this-key";
        String existingKeyId = "not-this-key";

        // Generate an RSA key pair to sign the mock JWT
        KeyPair mockKeyPair = generateMockRsaKeyPair();
        RSAPublicKey mockPublicKey =  (RSAPublicKey) mockKeyPair.getPublic();
        RSAPrivateKey mockPrivateKey =  (RSAPrivateKey) mockKeyPair.getPrivate();

        // Create the JSON Web Key that will be returned from the issuer
        JsonObject mockJwk = createMockJwkObject(existingKeyId, mockPublicKey);
        HttpResponse<Object> mockJwkResponse = createMockJwksResponse(mockJwk);

        // Create a JWT that was signed by an unknown key
        String invalidJwt = JWT.create()
            .withIssuer(issuer)
            .withKeyId(targetKeyId)
            .withExpiresAt(Instant.MAX)
            .sign(Algorithm.RSA256(mockPublicKey, mockPrivateKey));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        OidcProvider oidcProvider = new OidcProvider(issuer, mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockJwkResponse);

        // When...
        boolean result = oidcProvider.isJwtValid(invalidJwt);

        // Then...
        assertThat(result).isFalse();
    }

    @Test
    public void testIsJwtValidWithValidJwtReturnsTrue() throws Exception {
        // Given...
        String issuer = "http://dummy-issuer";
        String keyId = "mock-key";

        // Generate an RSA key pair to sign the mock JWT
        KeyPair mockKeyPair = generateMockRsaKeyPair();
        RSAPublicKey mockPublicKey =  (RSAPublicKey) mockKeyPair.getPublic();
        RSAPrivateKey mockPrivateKey =  (RSAPrivateKey) mockKeyPair.getPrivate();

        // Create the JSON Web Key that will be returned from the issuer
        JsonObject mockJwk = createMockJwkObject(keyId, mockPublicKey);
        HttpResponse<Object> mockJwkResponse = createMockJwksResponse(mockJwk);

        // Create a valid JWT that has not expired and was signed by a known key
        String validJwt = JWT.create()
            .withIssuer(issuer)
            .withKeyId(keyId)
            .withExpiresAt(Instant.MAX)
            .sign(Algorithm.RSA256(mockPublicKey, mockPrivateKey));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        OidcProvider oidcProvider = new OidcProvider(issuer, mockHttpClient, mockTimeService);

        mockHttpClient.setMockResponse(mockJwkResponse);

        // When...
        boolean result = oidcProvider.isJwtValid(validJwt);

        // Then...
        assertThat(result).isTrue();
    }

    @Test
    public void testGetOpenIdConfigurationReturnsValidConfig() throws Exception {
        // Given...
        JsonObject mockOpenIdConfig = new JsonObject();
        mockOpenIdConfig.addProperty("authorization_endpoint", "http://my.server/auth");
        mockOpenIdConfig.addProperty("token_endpoint", "http://my.server/token");
        mockOpenIdConfig.addProperty("jwks_uri", "http://my.server/keys");

        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(gson.toJson(mockOpenIdConfig), 200);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        OidcProvider oidcProvider = new OidcProvider("http://my.server", mockHttpClient);

        // When...
        JsonObject openIdConfig = oidcProvider.getOpenIdConfiguration();

        // Then...
        assertThat(openIdConfig).isNotNull();
        assertThat(openIdConfig).isEqualTo(mockOpenIdConfig);
    }


    @Test
    public void testGetOpenIdConfigurationWithErrorResponseReturnsNull() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(null, 500);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        // When...
        JsonObject openIdConfig = oidcProvider.getOpenIdConfiguration();

        // Then...
        assertThat(openIdConfig).isNull();
    }

    @Test
    public void testAuthorizationGetReturnsValidResponse() throws Exception {
        // Given...
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", 302);

        MockHttpClient mockHttpClient = new MockHttpClient(mockResponse);

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        HttpResponse<String> response = oidcProvider.sendAuthorizationGet("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(response).isNotNull();
        assertThat(response).isEqualTo(mockResponse);

        // Ensure the "state" parameter has been set as a session attribute
        String stateAttribute = (String) mockSession.getAttribute("state");
        assertThat(stateAttribute).isNotNull();
        assertThat(stateAttribute).hasSize(32);
    }

    @Test
    public void testGetConnectorRedirectUrlWithMissingLocationHeaderReturnsNull() throws Exception {
        // Given...
        Map<String, List<String>> headers = new HashMap<>();
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", HttpHeaders.of(headers, defaultFilter));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNull();
    }

    @Test
    public void testGetConnectorRedirectUrlWithLocationHeaderReturnsUrl() throws Exception {
        // Given...
        String mockRedirectUrl = "http://my.connector/auth";
        Map<String, List<String>> headers = Map.of("location", List.of(mockRedirectUrl));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>("", HttpHeaders.of(headers, defaultFilter));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNotNull();
        assertThat(redirectUrl).isEqualTo(mockRedirectUrl);
    }

    @Test
    public void testGetConnectorRedirectUrlWithRelativeLocationHeaderReturnsAbsoluteUrl() throws Exception {
        // Given...
        String mockRedirectUrl = "http://my.issuer/auth";
        Map<String, List<String>> headers = Map.of("location", List.of("/auth"));
        BiPredicate<String, String> defaultFilter = (a, b) -> true;
        HttpResponse<Object> mockResponse = new MockHttpResponse<Object>(URI.create(mockRedirectUrl), HttpHeaders.of(headers, defaultFilter));

        MockHttpClient mockHttpClient = new MockHttpClient(createMockOidcDiscoveryResponse());

        OidcProvider oidcProvider = new OidcProvider("http://dummy-issuer", mockHttpClient);

        mockHttpClient.setMockResponse(mockResponse);

        MockHttpSession mockSession = new MockHttpSession();

        // When...
        String redirectUrl = oidcProvider.getConnectorRedirectUrl("my-client-id", "http://my.server/callback", mockSession);

        // Then...
        assertThat(redirectUrl).isNotNull();
        assertThat(redirectUrl).isEqualTo(mockRedirectUrl);
    }
}