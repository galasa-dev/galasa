/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.resources.processors;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.api.common.resources.ResourceAction.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64.Encoder;

import org.junit.Test;

import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.mocks.MockCredentialsService;
import dev.galasa.framework.api.resources.ResourcesServletTest;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;

public class GalasaSecretProcessorTest extends ResourcesServletTest {

	private JsonObject generateSecretJson(String secretName, String type, String encoding, String username, String password) {
        return generateSecretJson(secretName, type, encoding, username, password, null);
	}

	private JsonObject generateSecretJson(
        String secretName,
        String type,
        String encoding,
        String username,
        String password,
        String token
    ) {
        JsonObject secretJson = new JsonObject();
        secretJson.addProperty("apiVersion", "galasa-dev/v1alpha1");
        secretJson.addProperty("kind", "GalasaSecret");

        JsonObject secretMetadata = new JsonObject();
        secretMetadata.addProperty("name", secretName);
        secretMetadata.addProperty("type", type);

        if (encoding != null) {
            secretMetadata.addProperty("encoding", encoding);
        }

        JsonObject secretData = new JsonObject();
        if (username != null) {
            secretData.addProperty("username", username);
        }

        if (password != null) {
            secretData.addProperty("password", password);
        }

        if (token != null) {
            secretData.addProperty("token", token);
        }

        secretJson.add("metadata", secretMetadata);
        secretJson.add("data", secretData);

        // Expecting a JSON structure in the form:
        // {
        //     "apiVersion": "galasa-dev/v1alpha1",
        //     "kind": "GalasaSecret",
        //     "metadata": {
        //         "name": "SECRET1",
        //         "type": "Username",
        //         "encoding": "base64"
        //     },
        //     "data": {
        //         "username": "a-username"
        //     }
        // }
		return secretJson;
	}

    @Test
    public void testApplySecretWithMissingNameReturnsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the secret's name from the JSON payload to force an error
        secretJson.get("metadata").getAsJsonObject().remove("name");

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(1);
        checkErrorStructure(errors.get(0), 5070, "GAL5070E",
            "Invalid GalasaSecret provided. One or more of the following mandatory fields are missing from the 'metadata' field:",
            "[name, type]");
    }

    @Test
    public void testApplySecretWithMissingSecretTypeReturnsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the secret's type from the JSON payload to force an error
        secretJson.get("metadata").getAsJsonObject().remove("type");

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(1);
        checkErrorStructure(errors.get(0), 5070, "GAL5070E",
            "Invalid GalasaSecret provided. One or more of the following mandatory fields are missing from the 'metadata' field:",
            "[name, type]");
    }

    @Test
    public void testApplySecretWithMissingDataThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the data from the JSON payload to force an error
        secretJson.remove("data");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, APPLY);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5069, "GAL5069E",
            "Invalid request body provided. The following mandatory fields are missing from the request body",
            "[data]");
    }

    @Test
    public void testApplySecretWithMissingMetadataThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the metadata from the JSON payload to force an error
        secretJson.remove("metadata");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, APPLY);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5069, "GAL5069E",
        "Invalid request body provided. The following mandatory fields are missing from the request body",
        "[metadata]");
    }

    @Test
    public void testApplySecretWithMissingApiVersionThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the apiVersion from the JSON payload to force an error
        secretJson.remove("apiVersion");

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, APPLY);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5069, "GAL5069E",
        "Invalid request body provided. The following mandatory fields are missing from the request body",
        "[apiVersion]");
    }

    @Test
    public void testApplySecretWithMissingUsernamePasswordFieldsReturnsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = null;
        String password = null;
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(1);
        checkErrorStructure(errors.get(0), 5072,
            "The 'UsernamePassword' type was provided but the following fields are missing from the 'data' field:",
            "[username, password]");
    }

    @Test
    public void testApplySecretWithUnsupportedEncodingReturnsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = "UNKNOWN!!!";
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(1);
        checkErrorStructure(errors.get(0), 5073,
            "GAL5073E: Unsupported data encoding scheme provided");
    }

    @Test
    public void testApplySecretWithUnknownSecretTypeReturnsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UNKNOWN TYPE!";
        String encoding = null;
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(1);
        checkErrorStructure(errors.get(0), 5074,
            "GAL5074E: Unknown GalasaSecret type provided");
    }

    @Test
    public void testApplySecretWithNoNameAndUnknownSecretTypeAndUnknownEncodingReturnsMultipleErrors() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UNKNOWN TYPE!";
        String encoding = "UNKNOWN ENCODING!";
        String username = "a-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // Remove the secret's type from the JSON payload to force an error
        secretJson.get("metadata").getAsJsonObject().remove("name");

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, APPLY);

        // Then...
        assertThat(errors).hasSize(3);
        checkErrorStructure(errors.get(0), 5070,
            "GAL5070E: Invalid GalasaSecret provided",
            "One or more of the following mandatory fields are missing from the 'metadata' field: [name, type].");
        checkErrorStructure(errors.get(1), 5074,
            "GAL5074E: Unknown GalasaSecret type provided");
        checkErrorStructure(errors.get(2), 5073,
            "GAL5073E: Unsupported data encoding scheme provided");
    }

    @Test
    public void testCreateUsernamePasswordSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";
        String encoding = null;
        String username = "my-username";
        String password = "a-password";
        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, password);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, CREATE);

        // Then...
        assertThat(errors).isEmpty();

        CredentialsUsernamePassword credentials = (CredentialsUsernamePassword) mockCreds.getCredentials(secretName);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @Test
    public void testCreateEncodedUsernamePasswordSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernamePassword";

        // Base64-encode the credentials
        Encoder encoder = Base64.getEncoder();
        String encoding = "base64";
        String username = "abc";
        String password = "123";
        String encodedUsername = encoder.encodeToString(username.getBytes());
        String encodedPassword = encoder.encodeToString(password.getBytes());

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, encodedUsername, encodedPassword);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, CREATE);

        // Then...
        assertThat(errors).isEmpty();

        // The credentials should have been decoded, so that they can be encrypted by the creds store
        CredentialsUsernamePassword credentials = (CredentialsUsernamePassword) mockCreds.getCredentials(secretName);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getPassword()).isEqualTo(password);
    }

    @Test
    public void testCreateEncodedTokenSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Token";

        // Base64-encode the credentials
        Encoder encoder = Base64.getEncoder();
        String encoding = "base64";
        String token = "my-token";
        String encodedToken = encoder.encodeToString(token.getBytes());

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, null, null, encodedToken);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, CREATE);

        // Then...
        assertThat(errors).isEmpty();

        // The credentials should have been decoded, so that they can be encrypted by the creds store
        CredentialsToken credentials = (CredentialsToken) mockCreds.getCredentials(secretName);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testCreateEncodedUsernameTokenSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "UsernameToken";

        // Base64-encode the credentials
        Encoder encoder = Base64.getEncoder();
        String encoding = "base64";
        String username = "my-username";
        String token = "my-token";
        String encodedUsername = encoder.encodeToString(username.getBytes());
        String encodedToken = encoder.encodeToString(token.getBytes());

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, encodedUsername, null, encodedToken);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, CREATE);

        // Then...
        assertThat(errors).isEmpty();

        // The credentials should have been decoded, so that they can be encrypted by the creds store
        CredentialsUsernameToken credentials = (CredentialsUsernameToken) mockCreds.getCredentials(secretName);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
        assertThat(credentials.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testCreateEncodedUsernameSecretSetsCredentialsOk() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Username";

        // Base64-encode the credentials
        Encoder encoder = Base64.getEncoder();
        String encoding = "base64";
        String username = "my-username";
        String encodedUsername = encoder.encodeToString(username.getBytes());

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, encodedUsername, null, null);

        // When...
        List<String> errors = secretProcessor.processResource(secretJson, CREATE);

        // Then...
        assertThat(errors).isEmpty();

        // The credentials should have been decoded, so that they can be encrypted by the creds store
        CredentialsUsername credentials = (CredentialsUsername) mockCreds.getCredentials(secretName);
        assertThat(credentials).isNotNull();
        assertThat(credentials.getUsername()).isEqualTo(username);
    }

    @Test
    public void testDeleteSecretDeletesCredentialsOk() throws Exception {
        // Given...
        String secretName = "ABC";
        String username = "my-username";
        Map<String, ICredentials> existingCreds = new HashMap<>();
        existingCreds.put(secretName, new CredentialsUsername(username));
        existingCreds.put("another-secret", new CredentialsUsername("another-username"));

        MockCredentialsService mockCreds = new MockCredentialsService(existingCreds);
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String type = "Username";
        String encoding = null;

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, null, null);

        // When...
        assertThat(mockCreds.getAllCredentials()).hasSize(2);
        assertThat(mockCreds.getCredentials(secretName)).isNotNull();
        List<String> errors = secretProcessor.processResource(secretJson, DELETE);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(mockCreds.getAllCredentials()).hasSize(1);
        assertThat(mockCreds.getCredentials(secretName)).isNull();
    }

    @Test
    public void testDeleteSecretDoesNotInsistOnData() throws Exception {
        // Given...
        String secretName = "ABC";
        String username = "my-username";
        Map<String, ICredentials> existingCreds = new HashMap<>();
        existingCreds.put(secretName, new CredentialsUsername(username));
        existingCreds.put("another-secret", new CredentialsUsername("another-username"));

        MockCredentialsService mockCreds = new MockCredentialsService(existingCreds);
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String type = "Username";
        String encoding = null;

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, null, null);

        // The data section is not required for deleting secrets
        secretJson.remove("data");

        // When...
        assertThat(mockCreds.getAllCredentials()).hasSize(2);
        assertThat(mockCreds.getCredentials(secretName)).isNotNull();
        List<String> errors = secretProcessor.processResource(secretJson, DELETE);

        // Then...
        assertThat(errors).isEmpty();
        assertThat(mockCreds.getAllCredentials()).hasSize(1);
        assertThat(mockCreds.getCredentials(secretName)).isNull();
    }

    @Test
    public void testCreateSecretThatAlreadyExistsThrowsError() throws Exception {
        // Given...
        Map<String, ICredentials> credsMap = new HashMap<>();
        credsMap.put("ABC", new CredentialsUsername("my-username"));

        MockCredentialsService mockCreds = new MockCredentialsService(credsMap);
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Username";
        String encoding = null;
        String username = "another-username";

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, username, null, null);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, CREATE);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5075, "GAL5075E",
            "A secret with the provided ID already exists.");
    }

    @Test
    public void testUpdateSecretThatDoesNotExistThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Token";
        String encoding = null;
        String token = "another-token";

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, null, null, token);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, UPDATE);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5076, "GAL5076E",
            "A secret with the provided ID does not exist");
    }

    @Test
    public void testApplySecretWithFailingCredsServiceThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        mockCreds.setThrowError(true);

        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Token";
        String encoding = null;
        String token = "a-token";

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, null, null, token);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, APPLY);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5079, "GAL5079E",
            "Failed to retrieve the secret with the given ID from the credentials store");
    }

    @Test
    public void testDeleteSecretWithFailingCredsServiceThrowsError() throws Exception {
        // Given...
        MockCredentialsService mockCreds = new MockCredentialsService(new HashMap<>());
        mockCreds.setThrowError(true);

        GalasaSecretProcessor secretProcessor = new GalasaSecretProcessor(mockCreds);
        String secretName = "ABC";
        String type = "Token";
        String encoding = null;
        String token = "a-token";

        JsonObject secretJson = generateSecretJson(secretName, type, encoding, null, null, token);

        // When...
        InternalServletException thrown = catchThrowableOfType(() -> {
            secretProcessor.processResource(secretJson, DELETE);
        }, InternalServletException.class);

        // Then...
        assertThat(thrown).isNotNull();
        checkErrorStructure(thrown.getMessage(), 5078, "GAL5078E",
            "Failed to delete a secret with the given ID from the credentials store");
    }
}
