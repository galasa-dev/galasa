/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.secrets.internal.routes;

import static dev.galasa.framework.spi.rbac.BuiltInAction.*;
import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.secrets.internal.routes.AbstractSecretsRoute.*;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.HttpMethod;
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

public class SecretsRouteTest extends SecretsServletTest {

    @Test
    public void testSecretsRouteRegexMatchesExpectedPaths() throws Exception {
        // Given...
        Pattern routePattern = new SecretsRoute(null, null, null, null).getPathRegex();

        // Then...
        // The servlet's whiteboard pattern will match /secrets, so the secrets route
        // should only allow an optional / or an empty string (no suffix after "/secrets")
        assertThat(routePattern.matcher("/").matches()).isTrue();
        assertThat(routePattern.matcher("").matches()).isTrue();

        // The route should not accept the following
        assertThat(routePattern.matcher("////").matches()).isFalse();
        assertThat(routePattern.matcher("/wrongpath!").matches()).isFalse();
    }

    @Test
    public void testGetSecretsWithMissingPermissionsRedactsSecretValues() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName1 = "BOB";
        String username1 = "my-username";
        String password1 = "not-a-password";
        String description1 = "this is my first secret";
        String lastUser1 = "user1";
        Instant lastUpdated1 = Instant.EPOCH;

        String secretName2 = "ITS_BOB_AGAIN";
        String username2 = "another-username";
        String description2 = "this is my second secret";
        String lastUser2 = "user2";
        Instant lastUpdated2 = Instant.EPOCH.plusMillis(1);

        String secretName3 = "not-b0b";
        String token3 = "this-is-a-token";

        String secretName4 = "new-bob";
        String username4 = "this-is-yet-another-username";
        String token4 = "this-is-another-token";
        String lastUser4 = "user4";
        Instant lastUpdated4 = Instant.EPOCH.plusMillis(4);

        ICredentials secret1 = new CredentialsUsernamePassword(username1, password1);
        secret1.setDescription(description1);
        secret1.setLastUpdatedByUser(lastUser1);
        secret1.setLastUpdatedTime(lastUpdated1);

        ICredentials secret2 = new CredentialsUsername(username2);
        secret2.setDescription(description2);
        secret2.setLastUpdatedByUser(lastUser2);
        secret2.setLastUpdatedTime(lastUpdated2);

        ICredentials secret4 = new CredentialsUsernameToken(username4, token4);
        secret4.setLastUpdatedByUser(username4);
        secret4.setLastUpdatedByUser(lastUser4);
        secret4.setLastUpdatedTime(lastUpdated4);

        creds.put(secretName1, secret1);
        creds.put(secretName2, secret2);
        creds.put(secretName3, new CredentialsToken(token3));
        creds.put(secretName4, secret4);

        MockCredentialsService credsService = new MockCredentialsService(creds);

        List<Action> actions = List.of(GENERAL_API_ACCESS.getAction());
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(JWT_USERNAME, actions);

        MockFramework mockFramework = new MockFramework(credsService);
        mockFramework.setRBACService(rbacService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        JsonArray expectedJson = new JsonArray();
        expectedJson.add(generateUsernameTokenSecretJson(secretName4, REDACTED_SECRET_VALUE, REDACTED_SECRET_VALUE, null, null, lastUser4, lastUpdated4));
        expectedJson.add(generateUsernameSecretJson(secretName2, REDACTED_SECRET_VALUE, null, description2, lastUser2, lastUpdated2));
        expectedJson.add(generateUsernamePasswordSecretJson(secretName1, REDACTED_SECRET_VALUE, REDACTED_SECRET_VALUE, null, description1, lastUser1, lastUpdated1));
        expectedJson.add(generateTokenSecretJson(secretName3, REDACTED_SECRET_VALUE, null, null, null, null));

        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));
    }

    @Test
    public void testGetSecretsReturnsAllSecretsOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName1 = "BOB";
        String username1 = "my-username";
        String password1 = "not-a-password";
        String description1 = "this is my first secret";
        String lastUser1 = "user1";
        Instant lastUpdated1 = Instant.EPOCH;

        String secretName2 = "ITS_BOB_AGAIN";
        String username2 = "another-username";
        String description2 = "this is my second secret";
        String lastUser2 = "user2";
        Instant lastUpdated2 = Instant.EPOCH.plusMillis(1);

        String secretName3 = "not-b0b";
        String token3 = "this-is-a-token";

        String secretName4 = "new-bob";
        String username4 = "this-is-yet-another-username";
        String token4 = "this-is-another-token";
        String lastUser4 = "user4";
        Instant lastUpdated4 = Instant.EPOCH.plusMillis(4);

        ICredentials secret1 = new CredentialsUsernamePassword(username1, password1);
        secret1.setDescription(description1);
        secret1.setLastUpdatedByUser(lastUser1);
        secret1.setLastUpdatedTime(lastUpdated1);

        ICredentials secret2 = new CredentialsUsername(username2);
        secret2.setDescription(description2);
        secret2.setLastUpdatedByUser(lastUser2);
        secret2.setLastUpdatedTime(lastUpdated2);

        ICredentials secret4 = new CredentialsUsernameToken(username4, token4);
        secret4.setLastUpdatedByUser(username4);
        secret4.setLastUpdatedByUser(lastUser4);
        secret4.setLastUpdatedTime(lastUpdated4);

        creds.put(secretName1, secret1);
        creds.put(secretName2, secret2);
        creds.put(secretName3, new CredentialsToken(token3));
        creds.put(secretName4, secret4);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        JsonArray expectedJson = new JsonArray();
        expectedJson.add(generateUsernameTokenSecretJson(secretName4, username4, token4, BASE64_ENCODING, null, lastUser4, lastUpdated4));
        expectedJson.add(generateUsernameSecretJson(secretName2, username2, BASE64_ENCODING, description2, lastUser2, lastUpdated2));
        expectedJson.add(generateUsernamePasswordSecretJson(secretName1, username1, password1, BASE64_ENCODING, description1, lastUser1, lastUpdated1));
        expectedJson.add(generateTokenSecretJson(secretName3, token3, BASE64_ENCODING, null, null, null));

        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(200);
        assertThat(output).isEqualTo(gson.toJson(expectedJson));
    }

    @Test
    public void testGetSecretsWithUnknownSecretTypeReturnsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName1 = "BOB";
        creds.put(secretName1, new MockCredentials());

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doGet(mockRequest, servletResponse);

        // Then...
        assertThat(servletResponse.getStatus()).isEqualTo(500);
        checkErrorStructure(outStream.toString(), 5101, "GAL5101E",
            "Unknown secret type detected");
    }

    @Test
    public void testCreateUsernamePasswordSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsernamePassword createdCredentials = (CredentialsUsernamePassword) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getUsername()).isEqualTo(username);
        assertThat(createdCredentials.getPassword()).isEqualTo(password);
    }

    @Test
    public void testCreateTokenSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB_TOKEN";
        String token = "my-token";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsToken createdCredentials = (CredentialsToken) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testCreateBase64EncodedSecretCreatesSecretOk() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "my-username";
        String encoding = "base64";
        String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes());

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(encodedUsername, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(201);
        assertThat(output).isEmpty();

        assertThat(credsService.getAllCredentials()).hasSize(1);
        CredentialsUsername createdCredentials = (CredentialsUsername) credsService.getCredentials(secretName);
        assertThat(createdCredentials).isNotNull();
        assertThat(createdCredentials.getUsername()).isEqualTo(username);
    }

    @Test
    public void testCreateBase64EncodedSecretWithBadlyEncodedDataThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String username = "this isn't base64 encoded!";
        String encoding = "base64";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5097, "GAL5097E",
            "Expected the value to be encoded in 'base64' format but it was not.");
    }

    @Test
    public void testCreateSecretWithPasswordAndTokenThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "BOB";
        String token = "my-token";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5095, "GAL5095E",
            "The 'password' and 'token' fields are mutually exclusive");
    }

    @Test
    public void testCreateSecretWithMissingSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5092, "GAL5092E",
            "The name of a Galasa secret cannot be empty, contain only spaces or tabs, or contain dots (.), and must only contain characters in the Latin-1 character set");
    }

    @Test
    public void testCreateSecretWithBlankSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "      ";
        String username = "my-username";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5092, "GAL5092E",
            "The name of a Galasa secret cannot be empty, contain only spaces or tabs, or contain dots (.), and must only contain characters in the Latin-1 character set");
    }

    @Test
    public void testCreateSecretWithMissingUsernameValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = null;
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithMissingPasswordValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String password = null;

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithMissingTokenValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String token = null;

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankUsernameValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = " ";
        String token = "my-token";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankPasswordValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String username = "my-username";
        String password = " ";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson(username));
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithBlankTokenValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String token = " ";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5096, "GAL5096E",
            "One or more secret fields in your request payload are missing a 'value'");
    }

    @Test
    public void testCreateSecretWithUnknownEncodingValueThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String token = "my-token";
        String encoding = "UNKNOWN ENCODING!";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("token", createSecretJson(token, encoding));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5073, "GAL5073E",
            "Unsupported data encoding scheme provided");
    }

    @Test
    public void testCreateSecretWithPasswordAndMissingUsernameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-SECRET";
        String password = "not-a-password";

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("password", createSecretJson(password));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5098, "GAL5098E",
            "A 'password' field was provided but the 'username' field was missing");
    }

    @Test
    public void testCreateSecretWithExistingSecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();
        String secretName = "MY-EXISTING-SECRET";
        creds.put(secretName, new CredentialsUsername("my-username"));

        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson("my-new-username"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(409);
        checkErrorStructure(output, 5075, "GAL5075E",
            "A secret with the provided name already exists");
    }

    @Test
    public void testCreateSecretWithNonLatin1SecretNameThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();

        // Latin-1 characters are in the range 0-255, so get one that is outside this range
        char nonLatin1Character = (char)300;
        String secretName = "MY-EXISTING-SECRET" + nonLatin1Character;


        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.add("username", createSecretJson("my-new-username"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5092, "GAL5092E",
            "Invalid secret name provided");
    }

    @Test
    public void testCreateSecretWithNonLatin1DescriptionThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> creds = new HashMap<>();

        // Latin-1 characters are in the range 0-255, so get one that is outside this range
        char nonLatin1Character = (char)300;
        String description = Character.toString(nonLatin1Character) + " more text here!";
        String secretName = "MY-EXISTING-SECRET";


        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("name", secretName);
        secretJson.addProperty("description", description);
        secretJson.add("username", createSecretJson("my-new-username"));
        String secretJsonStr = gson.toJson(secretJson);

        MockCredentialsService credsService = new MockCredentialsService(creds);
        MockFramework mockFramework = new MockFramework(credsService);

        MockTimeService timeService = new MockTimeService(Instant.EPOCH);
        MockSecretsServlet servlet = new MockSecretsServlet(mockFramework, timeService);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest("/", secretJsonStr, HttpMethod.POST.toString(), REQUEST_HEADERS);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ServletOutputStream outStream = servletResponse.getOutputStream();

        // When...
        servlet.init();
        servlet.doPost(mockRequest, servletResponse);

        // Then...
        String output = outStream.toString();
        assertThat(servletResponse.getStatus()).isEqualTo(400);
        checkErrorStructure(output, 5102, "GAL5102E",
            "Invalid secret description provided");
    }
}
