/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dev.galasa.ICredentials;
import dev.galasa.cps.etcd.internal.Etcd3CredentialsStore;
import dev.galasa.etcd.internal.mocks.MockEncryptionService;
import dev.galasa.etcd.internal.mocks.MockEtcdClient;
import dev.galasa.framework.spi.creds.CredentialsToken;
import dev.galasa.framework.spi.creds.CredentialsUsername;
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.CredentialsUsernameToken;

import static org.assertj.core.api.Assertions.*;

public class Etcd3CredentialsStoreTest {

    @Test
    public void testGetAllCredentialsReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        Map<String, String> mockCreds = new HashMap<>();
        String credsId1 = "CRED1";
        String username1 = "my-user";
        String token1 = "my-token";
        
        mockCreds.put("secure.credentials." + credsId1 + ".username", username1);
        mockCreds.put("secure.credentials." + credsId1 + ".token", token1);

        String credsId2 = "CRED2";
        String username2 = "another-username";
        String password2 = "a-password";
        mockCreds.put("secure.credentials." + credsId2 + ".username", username2);
        mockCreds.put("secure.credentials." + credsId2 + ".password", password2);

        String credsId3 = "CRED3";
        String token3 = "another-token";
        mockCreds.put("secure.credentials." + credsId3 + ".token", token3);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        Map<String, ICredentials> creds = store.getAllCredentials();

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds).hasSize(3);

        CredentialsUsernameToken actualCreds1 = (CredentialsUsernameToken) creds.get(credsId1);
        CredentialsUsernamePassword actualCreds2 = (CredentialsUsernamePassword) creds.get(credsId2);
        CredentialsToken actualCreds3 = (CredentialsToken) creds.get(credsId3);

        assertThat(actualCreds1.getUsername()).isEqualTo(username1);
        assertThat(actualCreds1.getToken()).isEqualTo(token1.getBytes());

        assertThat(actualCreds2.getUsername()).isEqualTo(username2);
        assertThat(actualCreds2.getPassword()).isEqualTo(password2);

        assertThat(actualCreds3.getToken()).isEqualTo(token3.getBytes());
    }

    @Test
    public void testGetAllCredentialsWithMissingPropertySuffixReturnsValidProperties() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        Map<String, String> mockCreds = new HashMap<>();
        String credsId1 = "CRED1";
        String username1 = "my-user";
        String token1 = "my-token";
        
        mockCreds.put("secure.credentials." + credsId1 + ".username", username1);
        mockCreds.put("secure.credentials." + credsId1 + ".token", token1);

        String credsId2 = "CRED2";
        String username2 = "another-username";
        String password2 = "a-password";
        mockCreds.put("secure.credentials." + credsId2 + ".password", password2);
        
        // This property is missing a credentials ID and suffix, so the property should be ignored
        mockCreds.put("secure.credentials", username2);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        Map<String, ICredentials> creds = store.getAllCredentials();

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds).hasSize(1);

        CredentialsUsernameToken actualCreds1 = (CredentialsUsernameToken) creds.get(credsId1);

        assertThat(actualCreds1.getUsername()).isEqualTo(username1);
        assertThat(actualCreds1.getToken()).isEqualTo(token1.getBytes());
    }

    @Test
    public void testGetAllCredentialsWithBadlyFormedPropertyReturnsValidProperties() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        Map<String, String> mockCreds = new HashMap<>();
        String credsId1 = "CRED1";
        String username1 = "my-user";
        String token1 = "my-token";
        
        mockCreds.put("secure.credentials." + credsId1 + ".username", username1);
        mockCreds.put("secure.credentials." + credsId1 + ".token", token1);

        String credsId2 = "CRED2";
        String username2 = "another-username";
        String password2 = "a-password";
        mockCreds.put("secure.credentials." + credsId2 + ".password", password2);
        
        // This property is missing a ".username" suffix, so the credential should be ignored
        mockCreds.put("secure.credentials." + credsId2, username2);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        Map<String, ICredentials> creds = store.getAllCredentials();

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds).hasSize(1);

        CredentialsUsernameToken actualCreds1 = (CredentialsUsernameToken) creds.get(credsId1);

        assertThat(actualCreds1.getUsername()).isEqualTo(username1);
        assertThat(actualCreds1.getToken()).isEqualTo(token1.getBytes());
    }

    @Test
    public void testGetAllCredentialsWithOtherPrefixesReturnsOnlyCredentials() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        Map<String, String> mockCreds = new HashMap<>();
        String credsId1 = "CRED1";
        String username1 = "my-user";
        String token1 = "my-token";
        
        mockCreds.put("secure.credentials." + credsId1 + ".username", username1);
        mockCreds.put("secure.credentials." + credsId1 + ".token", token1);

        String credsId2 = "NOT_A_CRED";
        String username2 = "a-random-value";
        mockCreds.put("secure.not-credentials." + credsId2, username2);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        Map<String, ICredentials> creds = store.getAllCredentials();

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds).hasSize(1);

        CredentialsUsernameToken actualCreds1 = (CredentialsUsernameToken) creds.get(credsId1);

        assertThat(actualCreds1.getUsername()).isEqualTo(username1);
        assertThat(actualCreds1.getToken()).isEqualTo(token1.getBytes());
    }

    @Test
    public void testGetUsernameCredentialsReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "my-user";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        CredentialsUsername creds = (CredentialsUsername) store.getCredentials(credsId);

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds.getUsername()).isEqualTo(username);
    }

    @Test
    public void testGetUsernamePasswordCredentialsReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "my-user";
        String password = "not-a-password";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + credsId + ".password", password);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) store.getCredentials(credsId);

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds.getUsername()).isEqualTo(username);
        assertThat(creds.getPassword()).isEqualTo(password);
    }

    @Test
    public void testGetUsernamePasswordCredentialsWithMetadataReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "my-user";
        String password = "not-a-password";
        String description = "a description of my credentials";
        String lastUpdatedUser = "myUsername";
        Instant lastUpdatedTime = Instant.EPOCH;
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + credsId + ".password", password);
        mockCreds.put("secure.credentials." + credsId + ".description", description);
        mockCreds.put("secure.credentials." + credsId + ".lastUpdated.time", lastUpdatedTime.toString());
        mockCreds.put("secure.credentials." + credsId + ".lastUpdated.user", lastUpdatedUser);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) store.getCredentials(credsId);

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds.getUsername()).isEqualTo(username);
        assertThat(creds.getPassword()).isEqualTo(password);
        assertThat(creds.getDescription()).isEqualTo(description);
        assertThat(creds.getLastUpdatedByUser()).isEqualTo(lastUpdatedUser);
        assertThat(creds.getLastUpdatedTime()).isEqualTo(lastUpdatedTime);
    }

    @Test
    public void testGetUsernameTokenCredentialsReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "my-user";
        String token = "a-token";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + credsId + ".token", token);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        CredentialsUsernameToken creds = (CredentialsUsernameToken) store.getCredentials(credsId);

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds.getUsername()).isEqualTo(username);
        assertThat(creds.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testGetTokenCredentialsReturnsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String token = "a-token";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".token", token);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        CredentialsToken creds = (CredentialsToken) store.getCredentials(credsId);

        // Then...
        assertThat(creds).isNotNull();
        assertThat(creds.getToken()).isEqualTo(token.getBytes());
    }

    @Test
    public void testSetUsernameCredentialsSetsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        
        Map<String, String> mockCreds = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        CredentialsUsername mockUsernameCreds = new CredentialsUsername(username);

        // When...
        store.setCredentials(credsId, mockUsernameCreds);

        // Then...
        assertThat(mockCreds).hasSize(1);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".username")).isEqualTo(username);
        
        // The credentials should have been encrypted when being set
        assertThat(mockEncryptionService.getEncryptCount()).isEqualTo(1);
        assertThat(mockEncryptionService.getDecryptCount()).isEqualTo(0);
    }

    @Test
    public void testSetUsernamePasswordCredentialsSetsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        String password = "not-a-password";
        
        Map<String, String> mockCreds = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        CredentialsUsernamePassword mockUsernamePasswordCreds = new CredentialsUsernamePassword(username, password);

        // When...
        store.setCredentials(credsId, mockUsernamePasswordCreds);

        // Then...
        assertThat(mockCreds).hasSize(2);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".username")).isEqualTo(username);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".password")).isEqualTo(password);
        
        // The credentials should have been encrypted when being set
        assertThat(mockEncryptionService.getEncryptCount()).isEqualTo(2);
        assertThat(mockEncryptionService.getDecryptCount()).isEqualTo(0);
    }

    @Test
    public void testSetUsernameTokenCredentialsSetsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        String token = "a-token";
        
        Map<String, String> mockCreds = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        CredentialsUsernameToken mockUsernameTokenCreds = new CredentialsUsernameToken(username, token);

        // When...
        store.setCredentials(credsId, mockUsernameTokenCreds);

        // Then...
        assertThat(mockCreds).hasSize(2);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".username")).isEqualTo(username);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".token")).isEqualTo(token);
        
        // The credentials should have been encrypted when being set
        assertThat(mockEncryptionService.getEncryptCount()).isEqualTo(2);
        assertThat(mockEncryptionService.getDecryptCount()).isEqualTo(0);
    }

    @Test
    public void testSetTokenCredentialsSetsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String token = "a-token";
        
        Map<String, String> mockCreds = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        CredentialsToken mockTokenCreds = new CredentialsToken(token);

        // When...
        store.setCredentials(credsId, mockTokenCreds);

        // Then...
        assertThat(mockCreds).hasSize(1);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".token")).isEqualTo(token);
        
        // The credentials should have been encrypted when being set
        assertThat(mockEncryptionService.getEncryptCount()).isEqualTo(1);
        assertThat(mockEncryptionService.getDecryptCount()).isEqualTo(0);
    }

    @Test
    public void testDeleteCredentialsRemovesCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        assertThat(mockCreds).hasSize(1);
        store.deleteCredentials(credsId);

        // Then...
        assertThat(mockCreds).hasSize(0);
    }

    @Test
    public void testDeleteCredentialsRemovesCredentialsByPrefix() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        String password = "not-a-password";
        
        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + credsId + ".password", password);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        assertThat(mockCreds).hasSize(2);
        store.deleteCredentials(credsId);

        // Then...
        assertThat(mockCreds).hasSize(0);
    }

    @Test
    public void testDeleteCredentialsDoesNotDeleteSimilarlyNamedSecret() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "SYS1_USER";
        String similarCredsId = "SYS1_USER_ABC";
        String username = "user-a";
        String similarUsername = "user-b";

        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + similarCredsId + ".username", similarUsername);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        store.deleteCredentials(credsId);

        // Then...
        assertThat(mockCreds).doesNotContainKey("secure.credentials." + credsId + ".username");
        assertThat(mockCreds).containsKey("secure.credentials." + similarCredsId + ".username");
        assertThat(mockCreds.get("secure.credentials." + similarCredsId + ".username")).isEqualTo(similarUsername);
    }

    @Test
    public void testSetCredentialsDoesNotDeleteSimilarlyNamedSecret() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "SYS1_ADMIN";
        String similarCredsId = "SYS1_ADMIN_XYZ";
        String username = "admin-a";
        String newUsername = "admin-aaa";
        String similarUsername = "admin-b";

        Map<String, String> mockCreds = new HashMap<>();
        mockCreds.put("secure.credentials." + credsId + ".username", username);
        mockCreds.put("secure.credentials." + similarCredsId + ".username", similarUsername);

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        store.setCredentials(credsId, new CredentialsUsername(newUsername));

        // Then...
        assertThat(mockCreds.get("secure.credentials." + credsId + ".username")).isEqualTo(newUsername);
        assertThat(mockCreds).containsKey("secure.credentials." + similarCredsId + ".username");
        assertThat(mockCreds.get("secure.credentials." + similarCredsId + ".username")).isEqualTo(similarUsername);
    }

    @Test
    public void testShutdownClosesEtcdClientsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        Map<String, String> mockCreds = new HashMap<>();

        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        // When...
        store.shutdown();

        // Then...
        assertThat(mockClient.isClientShutDown()).isTrue();
    }

    @Test
    public void testSetCredentialsWithMetadataSetsCredentialsOk() throws Exception {
        // Given...
        MockEncryptionService mockEncryptionService = new MockEncryptionService();
        String credsId = "CRED1";
        String username = "a-username";
        String lastUpdatedUser = "myuser";
        Instant lastUpdatedTime = Instant.EPOCH;
        String description = "this is a description of my username secret";
        
        
        Map<String, String> mockCreds = new HashMap<>();
        MockEtcdClient mockClient = new MockEtcdClient(mockCreds);
        Etcd3CredentialsStore store = new Etcd3CredentialsStore(null, mockEncryptionService, mockClient);

        CredentialsUsername mockUsernameCreds = new CredentialsUsername(username);
        mockUsernameCreds.setDescription(description);
        mockUsernameCreds.setLastUpdatedByUser(lastUpdatedUser);
        mockUsernameCreds.setLastUpdatedTime(lastUpdatedTime);

        // When...
        store.setCredentials(credsId, mockUsernameCreds);

        // Then...
        assertThat(mockCreds).hasSize(4);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".username")).isEqualTo(username);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".description")).isEqualTo(description);
        assertThat(mockCreds.get("secure.credentials." + credsId + ".lastUpdated.time")).isEqualTo(lastUpdatedTime.toString());
        assertThat(mockCreds.get("secure.credentials." + credsId + ".lastUpdated.user")).isEqualTo(lastUpdatedUser);
        
        // The credentials should have been encrypted when being set, but the metadata should not be encrypted
        assertThat(mockEncryptionService.getEncryptCount()).isEqualTo(1);
        assertThat(mockEncryptionService.getDecryptCount()).isEqualTo(0);
    }
}
