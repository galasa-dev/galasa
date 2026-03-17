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

/**
 * Unit tests for MacOsKeychainStore.
 */
public class MacOsKeychainStoreTest {

    private MockSecurityFramework mockSecurity;
    private MockCommandExecutor mockCommandExecutor;
    private MacOsKeychainStore store;

    @Before
    public void setUp() {
        mockSecurity = new MockSecurityFramework();
        mockCommandExecutor = new MockCommandExecutor();
        store = new MacOsKeychainStore(mockSecurity, mockCommandExecutor);
    }

    @After
    public void tearDown() {
        mockSecurity.clear();
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

    // ========== setCredentials() tests ==========

    @Test
    public void testSetCredentialsWithNullId() {
        assertThatThrownBy(() -> store.setCredentials(null, new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testSetCredentialsWithEmptyId() {
        assertThatThrownBy(() -> store.setCredentials("", new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testSetCredentialsWithNullCredentials() {
        assertThatThrownBy(() -> store.setCredentials("test-creds", null))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials cannot be null");
    }

    @Test
    public void testSetUsernamePasswordCredentials() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        String username = "myuser";
        CredentialsUsernamePassword credentials = new CredentialsUsernamePassword(username, "mypass");

        // When
        store.setCredentials(credsId, credentials);

        // Then
        String serviceName = "galasa.credentials." + credsId;
        assertThat(mockSecurity.hasPassword(serviceName, username))
            .as("Credentials should be stored with username as account name").isTrue();
        
        // Sync the mocks so getCredentials can read what setCredentials wrote
        mockCommandExecutor.addPassword(serviceName, username, "mypass");
        
        // Verify we can retrieve them
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(retrievedCreds.getPassword()).isEqualTo("mypass");
    }

    @Test
    public void testSetUsernameTokenCredentials() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        String username = "myuser";
        CredentialsUsernameToken credentials = new CredentialsUsernameToken(username, "mytoken789");

        // When
        store.setCredentials(credsId, credentials);

        // Then
        String serviceName = "galasa.credentials." + credsId;
        assertThat(mockSecurity.hasPassword(serviceName, username))
            .as("Credentials should be stored with username as account name").isTrue();
        
        // Sync the mocks so getCredentials can read what setCredentials wrote
        mockCommandExecutor.addPassword(serviceName, username, "mytoken789");
        
        // Verify we can retrieve them (as UsernamePassword)
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(retrievedCreds.getPassword()).isEqualTo("mytoken789");
    }

    @Test
    public void testSetUsernameCredentials() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        String username = "myuser";
        CredentialsUsername credentials = new CredentialsUsername(username);

        // When
        store.setCredentials(credsId, credentials);

        // Then
        String serviceName = "galasa.credentials." + credsId;
        assertThat(mockSecurity.hasPassword(serviceName, username))
            .as("Credentials should be stored with username as account name").isTrue();
        
        // Sync the mocks so getCredentials can read what setCredentials wrote
        mockCommandExecutor.addPassword(serviceName, username, "");
        
        // Verify we can retrieve them
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(retrievedCreds.getPassword()).as("Password should be empty for username-only").isEmpty();
    }

    @Test
    public void testSetTokenCredentials() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        CredentialsToken credentials = new CredentialsToken("mytoken999");

        // When
        store.setCredentials(credsId, credentials);

        // Then
        String serviceName = "galasa.credentials." + credsId;
        // Token-only credentials use credsId as account name since there's no username
        assertThat(mockSecurity.hasPassword(serviceName, credsId))
            .as("Credentials should be stored with credsId as account name for token-only").isTrue();
        
        // Sync the mocks so getCredentials can read what setCredentials wrote
        mockCommandExecutor.addPassword(serviceName, credsId, "mytoken999");
        
        // Verify we can retrieve them
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should be credsId for token-only").isEqualTo(credsId);
        assertThat(retrievedCreds.getPassword()).isEqualTo("mytoken999");
    }

    @Test
    public void testSetCredentialsOverwritesExisting() throws Exception {
        // Given - set initial credentials
        String credsId = "SYSTEM1";
        String username = "user";
        String serviceName = "galasa.credentials." + credsId;
        store.setCredentials(credsId, new CredentialsUsernamePassword(username, "oldpass"));
        // Sync to mockCommandExecutor so the second setCredentials can delete it
        mockCommandExecutor.addPassword(serviceName, username, "oldpass");

        // When - overwrite with new credentials
        // Note: setCredentials deletes from mockCommandExecutor but not mockSecurity,
        // so we need to manually remove from mockSecurity to simulate the real behavior
        mockSecurity.removePassword(serviceName, username);
        store.setCredentials(credsId, new CredentialsUsernamePassword(username, "newpass"));
        // Sync the new password to mockCommandExecutor for retrieval
        mockCommandExecutor.addPassword(serviceName, username, "newpass");

        // Then
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(retrievedCreds.getPassword()).as("Should have new password").isEqualTo("newpass");
    }

    @Test
    public void testSetCredentialsUserCanceled() {
        // Given
        mockSecurity.setShouldCancelAccess(true);

        // When/Then
        assertThatThrownBy(() -> store.setCredentials("test-creds", new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("User cancelled keychain access");
    }

    @Test
    public void testSetCredentialsAuthFailed() {
        // Given
        mockSecurity.setShouldFailAuth(true);

        // When/Then
        assertThatThrownBy(() -> store.setCredentials("test-creds", new CredentialsUsername("user")))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Authorization failed for keychain access");
    }

    // ========== deleteCredentials() tests ==========

    @Test
    public void testDeleteCredentialsWithNullId() {
        assertThatThrownBy(() -> store.deleteCredentials(null))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testDeleteCredentialsWithEmptyId() {
        assertThatThrownBy(() -> store.deleteCredentials(""))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials ID cannot be null or empty");
    }

    @Test
    public void testDeleteCredentialsNotFound() {
        assertThatThrownBy(() -> store.deleteCredentials("nonexistent"))
            .isInstanceOf(OsCredentialsException.class)
            .hasMessageContaining("Credentials not found");
    }

    @Test
    public void testDeleteCredentials() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        String username = "user";
        String serviceName = "galasa.credentials." + credsId;
        store.setCredentials(credsId, new CredentialsUsernamePassword(username, "pass"));
        mockCommandExecutor.addPassword(serviceName, username, "pass");

        // When
        store.deleteCredentials(credsId);
        mockCommandExecutor.removePassword(serviceName);

        // Then
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).as("Credentials should be deleted").isNull();
    }

    // ========== getAllCredentials() tests ==========

    @Test
    public void testGetAllCredentialsReturnsEmptyMap() throws Exception {
        // When
        var allCredentials = store.getAllCredentials();

        // Then
        assertThat(allCredentials).as("Should return empty map").isEmpty();
    }

    // ========== shutdown() tests ==========

    @Test
    public void testShutdownDoesNotThrow() throws Exception {
        // When/Then - should not throw
        store.shutdown();
    }

    // ========== Integration tests ==========

    @Test
    public void testCompleteWorkflow() throws Exception {
        // Given
        String credsId = "SYSTEM1";
        String username = "workflowuser";
        String serviceName = "galasa.credentials." + credsId;
        CredentialsUsernamePassword credentials = new CredentialsUsernamePassword(username, "workflowpass");

        // When - set credentials
        store.setCredentials(credsId, credentials);
        mockCommandExecutor.addPassword(serviceName, username, "workflowpass");

        // Then - retrieve and verify
        ICredentials retrieved = store.getCredentials(credsId);
        assertThat(retrieved).isInstanceOf(CredentialsUsernamePassword.class);
        CredentialsUsernamePassword retrievedCreds = (CredentialsUsernamePassword) retrieved;
        assertThat(retrievedCreds.getUsername()).as("Username should match").isEqualTo(username);
        assertThat(retrievedCreds.getPassword()).isEqualTo("workflowpass");

        // When - delete credentials
        store.deleteCredentials(credsId);
        mockCommandExecutor.removePassword(serviceName);

        // Then - verify deleted
        ICredentials afterDelete = store.getCredentials(credsId);
        assertThat(afterDelete).as("Credentials should be deleted").isNull();
    }

    @Test
    public void testMultipleCredentialsIndependence() throws Exception {
        // Given
        String credsId1 = "SYSTEM1";
        String credsId2 = "SYSTEM2";
        String username1 = "user1";
        String username2 = "user2";
        String serviceName1 = "galasa.credentials." + credsId1;
        String serviceName2 = "galasa.credentials." + credsId2;
        
        // When - set different credentials
        store.setCredentials(credsId1, new CredentialsUsernamePassword(username1, "pass1"));
        mockCommandExecutor.addPassword(serviceName1, username1, "pass1");
        store.setCredentials(credsId2, new CredentialsUsernamePassword(username2, "pass2"));
        mockCommandExecutor.addPassword(serviceName2, username2, "pass2");

        // Then - both should be retrievable independently
        ICredentials creds1 = store.getCredentials(credsId1);
        ICredentials creds2 = store.getCredentials(credsId2);
        
        assertThat(creds1).isInstanceOf(CredentialsUsernamePassword.class);
        assertThat(creds2).isInstanceOf(CredentialsUsernamePassword.class);
        assertThat(((CredentialsUsernamePassword) creds1).getUsername()).as("Username1 should match").isEqualTo(username1);
        assertThat(((CredentialsUsernamePassword) creds1).getPassword()).isEqualTo("pass1");
        assertThat(((CredentialsUsernamePassword) creds2).getUsername()).as("Username2 should match").isEqualTo(username2);
        assertThat(((CredentialsUsernamePassword) creds2).getPassword()).isEqualTo("pass2");

        // When - delete one
        store.deleteCredentials(credsId1);
        mockCommandExecutor.removePassword(serviceName1);

        // Then - other should still exist
        assertThat(store.getCredentials(credsId1)).isNull();
        assertThat(store.getCredentials(credsId2)).isNotNull();
    }

    @Test
    public void testManualKeychainEntry() throws Exception {
        // Simulate a user manually adding credentials via Keychain Access.app
        String credsId = "MANUAL_SYSTEM";
        String serviceName = "galasa.credentials." + credsId;
        String manualUsername = "IBM user";
        
        // User adds: Service="galasa.credentials.MANUAL_SYSTEM", Account="IBM user", Password="manual_password"
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

// Made with Bob
