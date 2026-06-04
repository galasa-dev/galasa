/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

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

/**
 * Unit tests for WindowsCredentialManagerStore.
 */
public class WindowsCredentialManagerStoreTest {

    private MockCredentialManager mockCredentialManager;
    private WindowsCredentialManagerStore store;
    private Gson gson;

    @Before
    public void setUp() {
        mockCredentialManager = new MockCredentialManager();
        store = new WindowsCredentialManagerStore(mockCredentialManager);
        gson = new GalasaGsonBuilder(false).getGson();
    }

    @After
    public void tearDown() {
        mockCredentialManager.clear();
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
    public void testGetCredentialsWithEmptyId() {
        assertThatThrownBy(() -> store.getCredentials(""))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testGetCredentialsNotFound() throws Exception {
        // When...
        ICredentials credentials = store.getCredentials("NONEXISTENT");

        // Then...
        assertThat(credentials).isNull();
    }

    @Test
    public void testGetCredentialsUsernamePassword() throws Exception {
        // Given...
        String targetName = "galasa.credentials.SIMBANK";
        mockCredentialManager.addCredential(targetName, "IBMUSER", "SYS1");

        // When...
        ICredentials credentials = store.getCredentials("SIMBANK");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsUsernamePassword.class);

        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        assertThat(creds.getUsername()).isEqualTo("IBMUSER");
        assertThat(creds.getPassword()).isEqualTo("SYS1");
    }

    @Test
    public void testGetCredentialsUsernameOnly() throws Exception {
        // Given...
        String targetName = "galasa.credentials.MYUSER";
        mockCredentialManager.addCredential(targetName, "username:IBMUSER", "");

        // When...
        ICredentials credentials = store.getCredentials("MYUSER");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsUsername.class);

        CredentialsUsername creds = (CredentialsUsername) credentials;
        assertThat(creds.getUsername()).isEqualTo("IBMUSER");
    }

    @Test
    public void testGetCredentialsTokenOnly() throws Exception {
        // Given...
        String targetName = "galasa.credentials.GITHUB";
        mockCredentialManager.addCredential(targetName, "token", "ghp_abc123xyz789");

        // When...
        ICredentials credentials = store.getCredentials("GITHUB");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsToken.class);

        CredentialsToken creds = (CredentialsToken) credentials;
        assertThat(creds.getToken()).isEqualTo("ghp_abc123xyz789".getBytes());
    }

    @Test
    public void testGetCredentialsUsernameToken() throws Exception {
        // Given...
        String targetName = "galasa.credentials.GITLAB";
        mockCredentialManager.addCredential(targetName, "username-token:myuser", "glpat-abc123xyz789");

        // When...
        ICredentials credentials = store.getCredentials("GITLAB");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsUsernameToken.class);

        CredentialsUsernameToken creds = (CredentialsUsernameToken) credentials;
        assertThat(creds.getUsername()).isEqualTo("myuser");
        assertThat(creds.getToken()).isEqualTo("glpat-abc123xyz789".getBytes());
    }

    @Test
    public void testGetCredentialsJsonKeyStoreJKS() throws Exception {
        // Given...
        String keystoreBase64 = createTestKeyStore("JKS", "keystorepass");
        String jsonPassword = createKeystoreJson(keystoreBase64, "keystorepass", "JKS");

        String targetName = "galasa.credentials.MYKEYSTORE";
        mockCredentialManager.addCredential(targetName, "JSON", jsonPassword);

        // When...
        ICredentials credentials = store.getCredentials("MYKEYSTORE");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsKeyStore.class);

        ICredentialsKeyStore creds = (ICredentialsKeyStore) credentials;
        assertThat(creds.getKeyStore()).isNotNull();
        assertThat(creds.getKeyStore().getType()).isEqualTo("JKS");
    }

    @Test
    public void testGetCredentialsJsonKeyStorePKCS12() throws Exception {
        // Given...
        String keystoreBase64 = createTestKeyStore("PKCS12", "keystorepass");
        String jsonPassword = createKeystoreJson(keystoreBase64, "keystorepass", "PKCS12");

        String targetName = "galasa.credentials.MYKEYSTORE";
        mockCredentialManager.addCredential(targetName, "json", jsonPassword); // Test case-insensitive

        // When...
        ICredentials credentials = store.getCredentials("MYKEYSTORE");

        // Then...
        assertThat(credentials).isNotNull();
        assertThat(credentials).isInstanceOf(CredentialsKeyStore.class);

        ICredentialsKeyStore creds = (ICredentialsKeyStore) credentials;
        assertThat(creds.getKeyStore()).isNotNull();
        assertThat(creds.getKeyStore().getType()).isEqualTo("PKCS12");
    }

    @Test
    public void testGetCredentialsJsonInvalidStructure() {
        // Given...
        String targetName = "galasa.credentials.INVALID";
        mockCredentialManager.addCredential(targetName, "JSON", "{\"unknown\":\"field\"}");

        // When/Then...
        assertThatThrownBy(() -> store.getCredentials("INVALID"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Unknown JSON credential structure");
    }

    @Test
    public void testGetCredentialsJsonInvalidSyntax() {
        // Given...
        String targetName = "galasa.credentials.INVALID";
        mockCredentialManager.addCredential(targetName, "JSON", "not valid json");

        // When/Then...
        assertThatThrownBy(() -> store.getCredentials("INVALID"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Invalid JSON in credentials");
    }

    @Test
    public void testGetCredentialsJsonEmpty() {
        // Given...
        String targetName = "galasa.credentials.EMPTY";
        mockCredentialManager.addCredential(targetName, "JSON", "");

        // When/Then...
        assertThatThrownBy(() -> store.getCredentials("EMPTY"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("JSON credentials cannot be empty");
    }

    // ========== Error handling tests ==========

    @Test
    public void testGetCredentialsWithSimulatedErrorFailsCorrectly() {
        // Given...
        mockCredentialManager.setShouldFail(true);

        // When/Then...
        assertThatThrownBy(() -> store.getCredentials("TEST"))
            .isInstanceOf(OsCredentialsException.class);
    }

    // ========== Unsupported operations tests ==========

    @Test
    public void testGetAllCredentialsThrowsException() {
        assertThatThrownBy(() -> store.getAllCredentials())
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Getting all credentials is not enabled for OS credentials stores");
    }

    @Test
    public void testSetCredentialsThrowsException() {
        // Given...
        CredentialsUsernamePassword creds = new CredentialsUsernamePassword("user", "pass");

        // When/Then...
        assertThatThrownBy(() -> store.setCredentials("TEST", creds))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Setting credentials is not enabled for OS credentials stores");
    }

    @Test
    public void testDeleteCredentialsThrowsException() {
        assertThatThrownBy(() -> store.deleteCredentials("TEST"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Deleting credentials is not enabled for OS credentials stores");
    }

    @Test
    public void testShutdownDoesNotThrow() throws Exception {
        store.shutdown(); // Should complete without error
    }

    // ========== Credential type detection tests ==========

    @Test
    public void testCredentialTypeDetectionPrefixOrdering() throws Exception {
        // Given...
        // Test that username-token: is checked before username:
        String targetName = "galasa.credentials.TEST";
        mockCredentialManager.addCredential(targetName, "username-token:testuser", "token123");

        // When...
        ICredentials credentials = store.getCredentials("TEST");

        // Then...
        assertThat(credentials).isInstanceOf(CredentialsUsernameToken.class);
        CredentialsUsernameToken creds = (CredentialsUsernameToken) credentials;
        assertThat(creds.getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testCredentialTypeDetectionTokenCaseInsensitive() throws Exception {
        // Given...
        String targetName = "galasa.credentials.TEST";
        mockCredentialManager.addCredential(targetName, "TOKEN", "mytoken");

        // When...
        ICredentials credentials = store.getCredentials("TEST");

        // Then...
        assertThat(credentials).isInstanceOf(CredentialsToken.class);
    }

    @Test
    public void testCredentialTypeDetectionJsonCaseInsensitive() throws Exception {
        // Given...
        String keystoreBase64 = createTestKeyStore("JKS", "pass");
        String jsonPassword = createKeystoreJson(keystoreBase64, "pass", "JKS");

        String targetName = "galasa.credentials.TEST";
        mockCredentialManager.addCredential(targetName, "Json", jsonPassword);

        // When...
        ICredentials credentials = store.getCredentials("TEST");

        // Then...
        assertThat(credentials).isInstanceOf(CredentialsKeyStore.class);
    }
}
