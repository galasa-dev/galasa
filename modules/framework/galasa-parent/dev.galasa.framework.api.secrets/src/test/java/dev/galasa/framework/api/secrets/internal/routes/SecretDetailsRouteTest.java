/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.resources.GalasaSecretType.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;
import static dev.galasa.framework.api.secrets.internal.routes.AbstractSecretsRoute.*;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.HttpMethod;
import dev.galasa.framework.api.common.MimeType;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.common.mocks.MockFramework;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.api.common.mocks.MockHttpServletResponse;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockCredentials;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.api.secrets.internal.SecretsServletTest;
import dev.galasa.framework.api.secrets.mocks.MockSecretsServlet;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.rbac.Action;

public class SecretDetailsRouteTest extends SecretsServletTest {

    @Test
    public void testSecretDetailsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new SecretDetailsRoute(null, null, null, null).getPathRegex();

        // Then...
        // The servlet's whiteboard pattern will match /secrets, so this route should
        // allow the name of a secret to be provided
        assertThat(routePattern.matcher("/MYSECRET").matches()).isTrue();
        assertThat(routePattern.matcher("/MYSECRET/").matches()).isTrue();
        assertThat(routePattern.matcher("/mysecret").matches()).isTrue();
        assertThat(routePattern.matcher("/myS3cret123").matches()).isTrue();
        assertThat(routePattern.matcher("/My-Secret_456").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("/My-Secret.456").matches()).isFalse();
        assertThat(routePattern.matcher("/123My.Secret.456").matches()).isFalse();
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("").matches()).isFalse();
        assertThat(routePattern.matcher("/my secret").matches()).isFalse();
        assertThat(routePattern.matcher("/<html>thisisbad</html>").matches()).isFalse();
        assertThat(routePattern.matcher("/javascript:thisisbad;").matches()).isFalse();
    }

    @Test
    public void testGetSecretByNameReturnsSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-user";
        creds.put(secretName, new CredentialsUsername(username));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        String expectedJson = gson.toJson(generateUsernameSecretJson(secretName, username, BASE64_ENCODING, null, null, null));
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetSecretByNameWithMissingPermissionsReturnsRedactedSecret() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-user";
        creds.put(secretName, new CredentialsUsername(username));

        MockCredentialsService credsService = new MockCredentialsService(creds);

        List<Action> actions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, actions);

        MockFramework mockFramework = new MockFramework(credsService);
        mockFramework.setRBACService(rbacService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");

        String expectedJson = gson.toJson(generateUsernameSecretJson(secretName, REDACTED_SECRET_VALUE, null, null, null, null));
        assertThat(outStream.toString()).isEqualTo(expectedJson);
    }

    @Test
    public void testGetNonExistantSecretByNameReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "NON_EXISTANT_SECRET";
        creds.put("BOB", new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5093, "GAL5093E",
            "Unable to retrieve a secret with the given name");
    }

    @Test
    public void testGetSecretByNameWithFailingCredsStoreReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        MockCredentialsService credsService = new MockCredentialsService(creds);

        // Force a server error from the creds service
        credsService.setThrowError(true);

        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5094, "GAL5094E",
            "Failed to retrieve a secret with the given name from the credentials store");
    }

    @Test
    public void testDeleteSecretDeletesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        creds.put(secretName, new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();
    }

    @Test
    public void testDeleteNonExistantSecretReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "NON_EXISTANT_SECRET";
        creds.put("BOB", new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(404);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5076, "GAL5076E",
            "Error occurred. A secret with the provided name does not exist.");
    }

    @Test
    public void testDeleteSecretWithFailingCredsStoreReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        MockCredentialsService credsService = new MockCredentialsService(creds);

        // Force a server error from the creds service
        credsService.setThrowError(true);

        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5078, "GAL5078E",
            "Failed to delete a secret with the given ID from the credentials store");
    }

    @Test
    public void testUpdateSecretUsernameUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String oldPassword = "not-a-password";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernamePassword(oldUsername, oldPassword));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernamePassword updatedCredentials = (CredentialsUsernamePassword) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
        assertThat(updatedCredentials.getPassword()).isEqualTo(oldPassword);
    }

    @Test
    public void testUpdateSecretUsernamePasswordUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String oldPassword = "not-a-password";
        String newUsername = "my-new-username";
        String newPassword = "my-new-password";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernamePassword(oldUsername, oldPassword));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        secretJson.add("password", createSecretJson(newPassword));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernamePassword updatedCredentials = (CredentialsUsernamePassword) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
        assertThat(updatedCredentials.getPassword()).isEqualTo(newPassword);
    }

    @Test
    public void testUpdateTokenSecretUpdatesValueOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateUsernameTokenSecretUpdatesValueOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-old-username";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernameToken(oldUsername, oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernameToken updatedCredentials = (CredentialsUsernameToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(oldUsername);
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateUsernameSecretUpdatesValueOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-old-username";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsername updatedCredentials = (CredentialsUsername) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
    }

    @Test
    public void testUpdateSecretToTokenChangesSecretTypeOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newToken = "my-new-token";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("type", TOKEN.toString());
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateSecretWithUnknownTypeReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("type", "UNKNOWN TYPE");
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5074, "GAL5074E",
            "Unknown GalasaSecret type provided");
    }

    @Test
    public void testUpdateSecretWithTypeAndMissingFieldsReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        // Create a request to change a secret into a UsernameToken, but is missing a token
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("type", USERNAME_TOKEN.toString());
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5099, "GAL5099E",
            "The 'UsernameToken' type was provided but the required 'token' field was missing");
    }

    @Test
    public void testUpdateSecretWithPasswordAndTokenPayloadReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";
        String newPassword = "my-new-password";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("password", createSecretJson(newPassword));
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5095, "GAL5095E",
            "The 'password' and 'token' fields are mutually exclusive");
    }

    @Test
    public void testUpdateSecretWithMixedEncodingUpdatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String newUsername = "my-new-username";
        String newToken = "my-new-token";
        String newTokenEncoded = Base64.getEncoder().encodeToString(newToken.getBytes());

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("type", USERNAME_TOKEN.toString());
        secretJson.add("username", createSecretJson(newUsername));
        secretJson.add("token", createSecretJson(newTokenEncoded, "base64"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernameToken updatedCredentials = (CredentialsUsernameToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getUsername()).isEqualTo(newUsername);
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateNonExistantSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        // Expect a 201 Created
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
    }

    @Test
    public void testUpdateNonExistantSecretWithPasswordAndTokenPayloadReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";
        String newPassword = "my-new-password";

        JsonObject secretJson = new JsonObject();
        secretJson.add("password", createSecretJson(newPassword));
        secretJson.add("token", createSecretJson(newToken));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5095, "GAL5095E",
            "The 'password' and 'token' fields are mutually exclusive");
    }

    @Test
    public void testUpdateNonExistantSecretWithPasswordOnlyPayloadReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newPassword = "my-new-password";

        JsonObject secretJson = new JsonObject();
        secretJson.add("password", createSecretJson(newPassword));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        assertThat(servletResponse.getContentType()).isEqualTo("application/json");
        checkErrorStructure(outStream.toString(), 5098, "GAL5098E",
            "A 'password' field was provided but the 'username' field was missing");
    }

    @Test
    public void testUpdateSecretWithUnknownTokenEncodingReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken, "UNKNOWN ENCODING!"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }

    @Test
    public void testUpdateSecretWithUnknownUsernameEncodingReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newUsername = "my-new-username";

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername, "UNKNOWN ENCODING!"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }

    @Test
    public void testUpdateSecretWithUnexpectedFieldsReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-old-username";
        creds.put(secretName, new CredentialsUsername(oldUsername));

        JsonObject secretJson = new JsonObject();

        // "password" isn't a valid field in the Username type, so this should throw an error
        secretJson.add("password", createSecretJson("bad"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5100, "GAL5100E",
            "An unexpected field was given to update a 'Username' secret");
    }

    @Test
    public void testUpdateSecretWithTooManyFieldsReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";
        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        
        // "username" isn't a valid field in the Token type, so this should throw an error
        secretJson.add("username", createSecretJson("my-username"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5100, "GAL5100E",
            "An unexpected field was given to update a 'Token' secret");
    }

    @Test
    public void testUpdateSecretWithUnsupportedTypeReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String newToken = "my-new-token";

        // The mock credentials type is not a supported type, so this should cause an error
        creds.put(secretName, new MockCredentials());

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));

        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5101, "GAL5101E",
            "Unknown secret type detected");
    }

    @Test
    public void testUpdateSecretWithBlankDescriptionReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";
        String newDescription = "   ";

        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        secretJson.addProperty("description", newDescription);

        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5102, "GAL5102E",
            "Invalid secret description provided");
    }

    @Test
    public void testUpdateSecretWithNonLatin1DescriptionReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";

        // Latin-1 characters are in the range 0-255, so get one that is outside this range
        char nonLatin1Character = (char)300;
        String description = Character.toString(nonLatin1Character) + " more text here!";

        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        secretJson.addProperty("description", description);

        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(outStream.toString(), 5102, "GAL5102E",
            "Invalid secret description provided");
    }

    @Test
    public void testUpdateSecretWithValidLatin1DescriptionUpdatesSecret() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldToken = "my-old-token";
        String newToken = "my-new-token";

        Instant lastUpdatedTime = Instant.EPOCH;

        // Latin-1 characters are in the range 0-255
        char latin1Character = (char)255;
        String description = Character.toString(latin1Character) + " more text here!";

        creds.put(secretName, new CredentialsToken(oldToken));

        JsonObject secretJson = new JsonObject();
        secretJson.add("token", createSecretJson(newToken));
        secretJson.addProperty("description", description);

        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(lastUpdatedTime);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(204);
        assertThat(outStream.toString()).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken updatedCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(updatedCredentials).isNotNull();
        assertThat(updatedCredentials.getToken()).isEqualTo(newToken.getBytes());
        assertThat(updatedCredentials.getDescription()).isEqualTo(description);
        assertThat(updatedCredentials.getLastUpdatedTime()).isEqualTo(lastUpdatedTime);
        assertThat(updatedCredentials.getLastUpdatedByUser()).isEqualTo(JWT_USERNAME);
    }

    @Test
    public void testUpdateSecretWithMissingSecretsSetPermissionReturnsForbidden() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String oldUsername = "my-username";
        String oldPassword = "not-a-password";
        String newUsername = "my-new-username";

        // Put an existing secret into the credentials store
        creds.put(secretName, new CredentialsUsernamePassword(oldUsername, oldPassword));

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(newUsername));
        String secretJsonStr = gson.toJson(secretJson);

        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);
        mockFramework.setRBACService(mockRbacService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, secretJsonStr, HttpMethod.PUT.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPut(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(403);
        assertThat(servletResponse.getContentType()).isEqualTo(MimeType.APPLICATION_JSON.toString());
        checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "SECRETS_SET");
    }

    @Test
    public void testDeleteSecretWithMissingSecretsDeletePermissionReturnsForbidden() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        creds.put(secretName, new CredentialsUsername("my-user"));

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, permittedActions);
        mockFramework.setRBACService(mockRbacService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/" + secretName, REQUEST_HEADERS);
        mockRequest.setMethod(HttpMethod.DELETE.toString());

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doDelete(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(403);
        checkErrorStructure(outStream.toString(), 5125, "GAL5125E", "SECRETS_DELETE");
    }
}

