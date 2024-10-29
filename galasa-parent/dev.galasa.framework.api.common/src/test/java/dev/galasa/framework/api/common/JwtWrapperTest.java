/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.framework.api.common.mocks.MockEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;

public class JwtWrapperTest{

    
    private String mockJwt = BaseServletTest.DUMMY_JWT;// Mock JWT containing no data, not a secret //pragma: allowlist secret

    @Test
    public void testGetUsernameValidJwtReturnsOk() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("Jack Skellington");
    }

    @Test
    public void testGetUsernameValidJwtWithOnlySubClaimReturnsSubClaim() throws Exception {
        // Given...
        // This mock JWT only contains the following claims in its payload:
        // {
        // "sub": "validUserID",
        // "iat": 1516239022
        // }
        String mockJwt = BaseServletTest.DUMMY_JWT; // Mock JWT, not a secret //pragma: allowlist secret
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "name,sub";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("Jack Skellington");
    }

    @Test
    public void testGetUsernameValidJwtWithNoMatchingClaimThrowsError() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);
        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "non-existant-claim";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        InternalServletException thrown = catchThrowableOfType(() -> auth.getUsername(), InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5057E", "No JWT claim exists in the given JWT that matches the supplied claims:", userNameClaimOverrides);
    }

    @Test
    public void testGetUsernameValidJwtNoUserClaimsThrowsError() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "iat": 1516239022
        // }
        String mockJwt = BaseServletTest.DUMMY_JWT; // Mock JWT, not a secret //pragma: allowlist secret
        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        MockEnvironment mockEnv = new MockEnvironment();
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        InternalServletException thrown = catchThrowableOfType(() -> auth.getUsername(), InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5058E", "No JWT claims to retrieve a username from were provided");
    }

    @Test
    public void testGetUsernameValidJwtWithCustomClaimsReturnsUsername() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "sub": "subject-bob",
        // "custom_user_id": "UserId",
        // "iat": 1516239022,
        // "name": "Bob"
        // }
        String mockJwt = BaseServletTest.DUMMY_JWT;// Mock JWT, not a secret //pragma: allowlist secret

        // Provide a custom claim to override the default JWT claims used by the API
        // server
        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "non_existant_claim,another_claim,custom_user_id,name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("Jack Skellington");
    }

    @Test
    public void testGetUsernameValidJwtWithSpacedCustomClaimsReturnsUsername() throws Exception {
        // Given...
        // This mock JWT contains the following claims in its payload:
        // {
        // "sub": "subject-bob",
        // "custom_user_id": "UserId",
        // "iat": 1516239022,
        // "name": "Bob"
        // }
        String mockJwt = BaseServletTest.DUMMY_JWT; // Mock JWT, not a secret //pragma: allowlist secret

        // Provide a custom claim to override the default JWT claims used by the API
        // server
        MockEnvironment mockEnv = new MockEnvironment();
        String userNameClaimOverrides = "a_claim   ,    custom_user_id   ,   name";
        mockEnv.setenv(EnvironmentVariables.GALASA_USERNAME_CLAIMS, userNameClaimOverrides);

        Map<String, String> headers = Map.of("Authorization", "Bearer " + mockJwt);

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        JwtWrapper auth = new JwtWrapper(req, mockEnv);
        String user = auth.getUsername();

        // Then...
        assertThat(user).isEqualTo("Jack Skellington");
    }

    @Test
    public void testGetUsernameNoJwtReturnsError() throws Exception {
        // Given...
        MockHttpServletRequest req = new MockHttpServletRequest("", new HashMap<>());

        // When...
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }

    @Test
    public void testGetUsernameInvalidJwtReturnsError() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Bearer JzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc",
                                            "Galasa-Application", "galasactl");

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token was expected to have");
    }

    @Test
    public void testGetUsernameInvalidAuthorizationReturnsError() throws Exception {
        // Given...
        Map<String, String> headers = Map.of("Authorization", "Basic JzdWIiOiJ2YWxpZFVzZXJJRCIsIm5hbWUiOiJVc",
                                            "Galasa-Application", "galasactl");

        MockHttpServletRequest req = new MockHttpServletRequest("", headers);

        // When...
        Throwable thrown = catchThrowable( () -> {new JwtWrapper(req);});

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("The token is null.");
    }
}