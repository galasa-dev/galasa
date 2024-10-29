/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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
}
