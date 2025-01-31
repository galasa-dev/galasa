/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi.internal;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.InternalUser;
import dev.galasa.framework.auth.spi.mocks.MockDexGrpcClient;
import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockInternalAuthToken;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;

public class AuthServiceTest {

    @Test
    public void testRevokeTokenDeletesTokensOk() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();

        String userIdDeletingTokens = "usernameDeletingTokens";
        IInternalUser owner = new InternalUser(userIdDeletingTokens, "dexId");

        List<IInternalAuthToken> tokens = new ArrayList<>();
        tokens.add(new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId));
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);

        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://my-issuer");
        mockDexGrpcClient.addDexClient(clientId, "my-secret", "http://a-callback-url");
        mockDexGrpcClient.addMockRefreshToken(owner.getLoginId(), clientId);

        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(userIdDeletingTokens);

        AuthService authService = new AuthService(authStoreService, mockDexGrpcClient, rbacService);
        assertThat(tokens).hasSize(1);

        // When...
        authService.revokeToken(tokenId, userIdDeletingTokens);

        // Then...
        assertThat(tokens).isEmpty();
    }

    @Test
    public void testRevokeTokenWithFailingAuthStoreThrowsError() throws Exception {
        // Given...
        String tokenId = "id123";
        String description = "test token";
        String clientId = "my-client";
        Instant creationTime = Instant.now();
        IInternalUser owner = new InternalUser("username", "dexId");

        List<IInternalAuthToken> tokens = new ArrayList<>();
        tokens.add(new MockInternalAuthToken(tokenId, description, creationTime, owner, clientId));
        MockAuthStoreService authStoreService = new MockAuthStoreService(tokens);

        // Simulate a failure in the auth store
        authStoreService.setThrowException(true);

        MockDexGrpcClient mockDexGrpcClient = new MockDexGrpcClient("http://my-issuer");
        mockDexGrpcClient.addDexClient(clientId, "my-secret", "http://a-callback-url");
        mockDexGrpcClient.addMockRefreshToken(owner.getLoginId(), clientId);

        String userIdDeletingTokens = "usernameDeletingTokens";
        MockRBACService rbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(userIdDeletingTokens);

        AuthService authService = new AuthService(authStoreService, mockDexGrpcClient, rbacService);
        assertThat(tokens).hasSize(1);

        // When...
        InternalServletException thrown = catchThrowableOfType(
            () -> authService.revokeToken(tokenId,userIdDeletingTokens),
            InternalServletException.class
        );

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("GAL5064E", "Failed to revoke the token with the given ID");
    }
}
