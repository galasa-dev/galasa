/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.coreos.dex.api.DexOuterClass.Client;

import dev.galasa.framework.api.authentication.mocks.MockDexGrpcClient;
import dev.galasa.framework.api.common.InternalServletException;

public class DexGrpcClientTest {

    @Test
    public void testCreateClientWithValidClientResponseCreatesDexClient() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", "client-id", "secret", callbackUrl);

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(client.getDexClients()).hasSize(1);
        assertThat(responseClient).isEqualTo(client.getDexClients().get(0));
    }

    @Test
    public void testCreateClientWithEmptyClientResponseReturnsNull() throws Exception {
        // Given...
        String callbackUrl = "http://my-app/callback";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");

        // When...
        Client responseClient = client.createClient(callbackUrl);

        // Then...
        assertThat(responseClient).isNull();
    }

    @Test
    public void testGetClientWithEmptyClientResponseThrowsInternalServletException() throws Exception {
        // Given...
        String clientId = "myclient";

        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");

        // When...
        Throwable thrown = catchThrowable(() -> {
            client.getClient(clientId);
        });

        // Then...
        assertThat(thrown).isInstanceOf(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5052E", "Unable to retrieve client for authentication");
    }

    @Test
    public void testGetClientWithValidClientResponseReturnsClient() throws Exception {
        // Given...
        String clientId = "myclient";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", clientId, "secret", "http://callback");

        // When...
        Client responseClient = client.getClient(clientId);

        // Then...
        assertThat(responseClient).isEqualTo(client.getDexClients().get(0));
    }

    @Test
    public void testGetClientWithInvalidClientIdThrowsInternalServletException() throws Exception {
        // Given...
        // Force the mock client to throw a StatusRuntimeException
        String clientId = "error";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", clientId, "secret", "http://callback");
        client.setThrowError(true);

        // When...
        Throwable thrown = catchThrowable(() -> {
            client.getClient(clientId);
        });

        // Then...
        assertThat(thrown).isInstanceOf(InternalServletException.class);
        assertThat(thrown.getMessage()).contains("GAL5051E","Invalid GALASA_TOKEN value provided");
    }

    @Test
    public void testDeleteClientRemovesClientOK() throws Exception {
        // Given...
        String clientId = "a-client";
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", clientId, "secret", "http://callback");
        client.addDexClient("anotherclient", "anothersecret", "http://another-callback-url");

        assertThat(client.getDexClients()).hasSize(2);

        // When...
        client.deleteClient(clientId);

        // Then...
        assertThat(client.getDexClients()).hasSize(1);
    }

    @Test
    public void testDeleteClientWithFailingClientDeletionThrowsException() throws Exception {
        // Given...
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer", "a-client", "secret", "http://callback");
        client.setThrowError(true);

        assertThat(client.getDexClients()).hasSize(1);

        // When...
        Throwable thrown = catchThrowableOfType(() -> {
            client.deleteClient("a-non-existant-client");
        }, InternalServletException.class);

        // Then...
        assertThat(thrown.getMessage()).contains("GAL5063E", "Internal server error", "Failed to delete Dex client with the given ID");
        assertThat(client.getDexClients()).hasSize(1);
    }

    @Test
    public void testRevokeRefreshWithFailingRevokeOperationThrowsException() throws Exception {
        // Given...
        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");
        client.setThrowError(true);

        // When...
        Throwable thrown = catchThrowableOfType(() -> {
            client.revokeRefreshToken("my-user", "notfound");
        }, InternalServletException.class);

        // Then...
        assertThat(thrown.getMessage()).contains("GAL5064E", "Failed to revoke the token with the given ID");
    }

    @Test
    public void testRevokeRefreshRemovesTokenOK() throws Exception {
        // Given...
        String userId = "my-user";
        String clientId = "my-client";

        MockDexGrpcClient client = new MockDexGrpcClient("http://my.issuer");
        client.addDexClient(clientId, "my-secret", "http://my-callback-url");
        client.addMockRefreshToken(userId, clientId);

        assertThat(client.getRefreshTokens()).hasSize(1);

        // When...
        client.revokeRefreshToken(userId, clientId);

        // Then...
        assertThat(client.getRefreshTokens()).hasSize(0);
    }
}
