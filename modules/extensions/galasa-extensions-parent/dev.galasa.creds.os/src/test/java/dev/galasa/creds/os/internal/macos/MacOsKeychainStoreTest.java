/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;

public class MacOsKeychainStoreTest {

    private MockCommandExecutor mockCommandExecutor;
    private MacOsKeychainStore store;

    @Before
    public void setUp() {
        mockCommandExecutor = new MockCommandExecutor();
        store = new MacOsKeychainStore(mockCommandExecutor);
    }

    @After
    public void tearDown() {
        mockCommandExecutor.clear();
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
    public void testSetCredentialsWithNullId() {
        assertThatThrownBy(() -> store.setCredentials(null, new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Method not implemented for Mac OS Keychain");
    }

    // ========== deleteCredentials() tests ==========

    @Test
    public void testDeleteCredentialsWithNullId() {
        assertThatThrownBy(() -> store.deleteCredentials(null))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Method not implemented for Mac OS Keychain");
    }

    // ========== getAllCredentials() tests ==========

    @Test
    public void testGetAllCredentialsReturnsEmptyMap() throws Exception {
        assertThatThrownBy(() -> store.getAllCredentials())
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Method not implemented");
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
}
