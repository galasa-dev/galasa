/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.security.KeyStore;
import java.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsKeyStore;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.framework.spi.creds.CredentialsKeyStore;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;
import dev.galasa.framework.spi.utils.GalasaGsonBuilder;

public class MacOsKeychainStoreTest {

    private MockCommandExecutor mockCommandExecutor;
    private MacOsKeychainStore store;
    private Gson gson;

    @Before
    public void setUp() {
        mockCommandExecutor = new MockCommandExecutor();
        SecurityCommand securityCommand = new SecurityCommand(mockCommandExecutor);
        store = new MacOsKeychainStore(securityCommand);
        gson = new GalasaGsonBuilder(false).getGson();
    }

    @After
    public void tearDown() {
        mockCommandExecutor.clear();
    }

    /**
     * Creates a valid base64-encoded keystore for testing.
     *
     * @param type The keystore type (JKS or PKCS12)
     * @param password The keystore password
     * @return Base64-encoded keystore bytes
     */
    private String createTestKeyStore(String type, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(type);
        keyStore.load(null, password.toCharArray());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        keyStore.store(baos, password.toCharArray());
        
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Creates a JSON object for KeyStore credentials and converts it to a string.
     *
     * @param keystoreBase64 The base64-encoded keystore content
     * @param password The keystore password
     * @param type The keystore type (JKS or PKCS12)
     * @return JSON string representation
     */
    private String createKeystoreJson(String keystoreBase64, String password, String type) {
        JsonObject json = new JsonObject();
        json.addProperty("keystore", keystoreBase64);
        json.addProperty("password", password);
        json.addProperty("type", type);
        return gson.toJson(json);
    }

    // ========== getCredentials() tests ==========

    @Test
    public void testGetCredentialsWithNullId() {
        assertThatThrownBy(() -> store.getCredentials(null))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testGetCredentialsWithInvalidId() {
        assertThatThrownBy(() -> store.getCredentials("galasa.credentials.ID\nmaliciouscode"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID contains invalid characters");
    }

    @Test
    public void testGetCredentialsWithEmptyId() {
        assertThatThrownBy(() -> store.getCredentials(""))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testGetCredentialsWithWhitespaceId() {
        assertThatThrownBy(() -> store.getCredentials("   "))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testGetCredentialsNotFound() throws Exception {
        ICredentials credentials = store.getCredentials("nonexistent");
        assertThat(credentials).as("Non-existent credentials should return null").isNull();
    }

    @Test
    public void testGetCredentialsReturnsUsernamePassword() throws Exception {
        // Given - manually add credentials to mock keychain
        String credsId = "SYSTEM1";
        String serviceName = "galasa.credentials." + credsId;
        String username = "testuser";
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, username, "mypassword");

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be UsernamePassword type").isInstanceOf(CredentialsUsernamePassword.class);
        
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        assertThat(creds.getUsername()).as("Username should be account name from keychain").isEqualTo(username);
        assertThat(creds.getPassword()).as("Password should match").isEqualTo("mypassword");
    }

    @Test
    public void testGetCredentialsUserCanceled() {
        // Given
        mockCommandExecutor.setShouldCancelAccess(true);

        // When/Then
        assertThatThrownBy(() -> store.getCredentials("test-creds"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("User cancelled keychain access");
    }

    @Test
    public void testGetCredentialsAuthFailed() {
        // Given
        mockCommandExecutor.setShouldFailAuth(true);

        // When/Then
        assertThatThrownBy(() -> store.getCredentials("test-creds"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Failed to retrieve credentials from keychain");
    }

    @Test
    public void testGetCredentialsReturnsUsername() throws Exception {
        // Given - manually add username-only credentials to mock keychain
        String credsId = "SYSTEM1";
        String serviceName = "galasa.credentials." + credsId;
        String username = "testuser";
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "username:" + username, "");

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be Username type").isInstanceOf(CredentialsUsername.class);
        
        CredentialsUsername creds = (CredentialsUsername) credentials;
        assertThat(creds.getUsername()).as("Username should be extracted from account name").isEqualTo(username);
    }

    @Test
    public void testGetCredentialsReturnsToken() throws Exception {
        // Given - manually add token-only credentials to mock keychain
        String credsId = "SYSTEM1";
        String serviceName = "galasa.credentials." + credsId;
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "token", "abc123token");

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be Token type").isInstanceOf(CredentialsToken.class);
        
        CredentialsToken creds = (CredentialsToken) credentials;
        assertThat(new String(creds.getToken())).as("Token should match").isEqualTo("abc123token");
    }

    @Test
    public void testGetCredentialsReturnsUsernameToken() throws Exception {
        // Given - manually add username+token credentials to mock keychain
        String credsId = "SYSTEM1";
        String serviceName = "galasa.credentials." + credsId;
        String username = "testuser";
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "username-token:" + username, "xyz789token");

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be UsernameToken type").isInstanceOf(CredentialsUsernameToken.class);
        
        CredentialsUsernameToken creds = (CredentialsUsernameToken) credentials;
        assertThat(creds.getUsername()).as("Username should be extracted from account name").isEqualTo(username);
        assertThat(new String(creds.getToken())).as("Token should match").isEqualTo("xyz789token");
    }

    @Test
    public void testGetCredentialsBackwardCompatibility() throws Exception {
        // Given - manually add credentials in old format (plain username as account)
        String credsId = "SYSTEM1";
        String serviceName = "galasa.credentials." + credsId;
        String username = "oldformatuser";
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, username, "oldpassword");

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then - should still work as UsernamePassword for backward compatibility
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be UsernamePassword type for backward compatibility")
            .isInstanceOf(CredentialsUsernamePassword.class);
        
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        assertThat(creds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(creds.getPassword()).as("Password should match").isEqualTo("oldpassword");
    }

    // ========== setCredentials() tests ==========

    @Test
    public void testSetCredentialsThrowsException() {
        assertThatThrownBy(() -> store.setCredentials("id", new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Setting credentials is not enabled for OS credentials stores");
    }

    // ========== deleteCredentials() tests ==========

    @Test
    public void testDeleteCredentialsThrowsException() {
        assertThatThrownBy(() -> store.deleteCredentials("id"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Deleting credentials is not enabled for OS credentials stores");
    }

    // ========== getAllCredentials() tests ==========

    @Test
    public void testGetAllCredentialsThrowsException() throws Exception {
        assertThatThrownBy(() -> store.getAllCredentials())
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Getting all credentials is not enabled for OS credentials stores");
    }

    // ========== shutdown() tests ==========

    @Test
    public void testShutdownDoesNotThrow() throws Exception {
        // When/Then - should not throw an exception
        store.shutdown();
    }

    @Test
    public void testManualKeychainEntry() throws Exception {
        // Simulate a user manually adding credentials via Keychain Access.app
        String credsId = "MANUAL_SYSTEM";
        String serviceName = "galasa.credentials." + credsId;
        String manualUsername = "user";

        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, manualUsername, "manual_password");

        // When - Galasa retrieves the credentials
        ICredentials retrieved = store.getCredentials(credsId);

        // Then - Should work seamlessly
        assertThat(retrieved).isNotNull();
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) retrieved;
        assertThat(creds.getUsername()).as("Username should be the account name from keychain").isEqualTo(manualUsername);
        assertThat(creds.getPassword()).isEqualTo("manual_password");
    }

    // ========== JSON-based credentials tests ==========

    @Test
    public void testGetCredentialsReturnsJsonKeyStore() throws Exception {
        // Given - JSON-based KeyStore credentials with valid keystore
        String credsId = "MYKEYSTORE";
        String serviceName = "galasa.credentials." + credsId;
        String keystorePassword = "keystorepass";
        String keystoreBase64 = createTestKeyStore("JKS", keystorePassword);
        String jsonPassword = createKeystoreJson(keystoreBase64, keystorePassword, "JKS");
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be KeyStore type").isInstanceOf(CredentialsKeyStore.class);
        
        ICredentialsKeyStore creds = (ICredentialsKeyStore) credentials;
        assertThat(creds.getKeyStorePassword()).as("KeyStore password should match").isEqualTo(keystorePassword);
        assertThat(creds.getKeyStoreType()).as("KeyStore type should match").isEqualTo("JKS");
        assertThat(creds.getKeyStore()).as("KeyStore should be loadable").isNotNull();
    }

    @Test
    public void testGetCredentialsJsonKeyStorePKCS12() throws Exception {
        // Given - JSON-based KeyStore credentials with PKCS12 type
        String credsId = "PKCS12STORE";
        String serviceName = "galasa.credentials." + credsId;
        String keystorePassword = "p12pass";
        String keystoreBase64 = createTestKeyStore("PKCS12", keystorePassword);
        String jsonPassword = createKeystoreJson(keystoreBase64, keystorePassword, "PKCS12");
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be KeyStore type").isInstanceOf(CredentialsKeyStore.class);
        
        ICredentialsKeyStore creds = (ICredentialsKeyStore) credentials;
        assertThat(creds.getKeyStorePassword()).as("KeyStore password should match").isEqualTo(keystorePassword);
        assertThat(creds.getKeyStoreType()).as("KeyStore type should match").isEqualTo("PKCS12");
        assertThat(creds.getKeyStore()).as("KeyStore should be loadable").isNotNull();
    }

    @Test
    public void testGetCredentialsJsonCaseInsensitive() throws Exception {
        // Given - JSON account name in different case
        String credsId = "CASETEST";
        String serviceName = "galasa.credentials." + credsId;
        String keystorePassword = "testpass";
        String keystoreBase64 = createTestKeyStore("JKS", keystorePassword);
        String jsonPassword = createKeystoreJson(keystoreBase64, keystorePassword, "JKS");
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "json", jsonPassword);

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be KeyStore type").isInstanceOf(CredentialsKeyStore.class);
    }

    @Test
    public void testGetCredentialsJsonEmptyPassword() {
        // Given - JSON account with empty password
        String credsId = "EMPTYTEST";
        String serviceName = "galasa.credentials." + credsId;
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", "");

        // When/Then
        assertThatThrownBy(() -> store.getCredentials(credsId))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("JSON credentials cannot be empty");
    }

    @Test
    public void testGetCredentialsJsonInvalidSyntax() {
        // Given - Invalid JSON syntax
        String credsId = "INVALIDJSON";
        String serviceName = "galasa.credentials." + credsId;
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", "{invalid json}");

        // When/Then
        assertThatThrownBy(() -> store.getCredentials(credsId))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Invalid JSON in credentials");
    }

    @Test
    public void testGetCredentialsJsonMissingKeystoreField() {
        // Given - JSON missing keystore field
        String credsId = "MISSINGFIELD";
        String serviceName = "galasa.credentials." + credsId;
        JsonObject json = new JsonObject();
        json.addProperty("password", "pass");
        json.addProperty("type", "JKS");
        String jsonPassword = gson.toJson(json);
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When/Then
        assertThatThrownBy(() -> store.getCredentials(credsId))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Unknown JSON credential structure");
    }

    @Test
    public void testGetCredentialsJsonMissingPasswordField() {
        // Given - JSON missing password field
        String credsId = "MISSINGPASS";
        String serviceName = "galasa.credentials." + credsId;
        JsonObject json = new JsonObject();
        json.addProperty("keystore", "abc");
        json.addProperty("type", "JKS");
        String jsonPassword = gson.toJson(json);
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When/Then
        assertThatThrownBy(() -> store.getCredentials(credsId))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("JSON KeyStore credential missing 'password' field");
    }

    @Test
    public void testGetCredentialsJsonUnknownStructure() {
        // Given - JSON with unknown structure (no recognized credential type)
        String credsId = "UNKNOWN";
        String serviceName = "galasa.credentials." + credsId;
        JsonObject json = new JsonObject();
        json.addProperty("unknown", "field");
        String jsonPassword = gson.toJson(json);
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When/Then
        assertThatThrownBy(() -> store.getCredentials(credsId))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Unknown JSON credential structure");
    }

    @Test
    public void testGetCredentialsJsonWithWhitespace() throws Exception {
        // Given - JSON with extra whitespace (Gson handles this automatically)
        String credsId = "WHITESPACE";
        String serviceName = "galasa.credentials." + credsId;
        String keystorePassword = "pass";
        String keystoreBase64 = createTestKeyStore("JKS", keystorePassword);
        String jsonPassword = createKeystoreJson(keystoreBase64, keystorePassword, "JKS");
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be KeyStore type").isInstanceOf(CredentialsKeyStore.class);
    }
    @Test
    public void testGetCredentialsJsonKeystoreDefaultsToPKCS12WhenTypeNotProvided() throws Exception {
        // Given - JSON-based KeyStore credentials without type field
        String credsId = "DEFAULTTYPE";
        String serviceName = "galasa.credentials." + credsId;
        String keystorePassword = "dummy";
        String keystoreBase64 = createTestKeyStore("PKCS12", keystorePassword);
        
        // Create JSON without type field
        JsonObject json = new JsonObject();
        json.addProperty("keystore", keystoreBase64);
        json.addProperty("password", keystorePassword);
        String jsonPassword = gson.toJson(json);
        
        mockCommandExecutor.setServiceName(serviceName);
        mockCommandExecutor.addPassword(serviceName, "JSON", jsonPassword);

        // When
        ICredentials credentials = store.getCredentials(credsId);

        // Then
        assertThat(credentials).as("Credentials should be found").isNotNull();
        assertThat(credentials).as("Should be KeyStore type").isInstanceOf(CredentialsKeyStore.class);
        
        ICredentialsKeyStore creds = (ICredentialsKeyStore) credentials;
        assertThat(creds.getKeyStorePassword()).as("KeyStore password should match").isEqualTo(keystorePassword);
        assertThat(creds.getKeyStoreType()).as("KeyStore type should default to PKCS12").isEqualTo("PKCS12");
        assertThat(creds.getKeyStore()).as("KeyStore should be loadable").isNotNull();
    }

}
