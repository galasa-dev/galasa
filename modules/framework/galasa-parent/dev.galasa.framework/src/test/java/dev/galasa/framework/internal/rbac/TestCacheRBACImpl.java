/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.time.Instant;
import java.util.List;

import org.junit.*;

import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.mocks.MockUser;
import dev.galasa.framework.spi.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;

import static org.assertj.core.api.Assertions.*;

public class TestCacheRBACImpl {

    @Test
    public void testIsActionPermittedReturnsTrueForValidMappings() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);
        mockUser.setRoleId("2");

        mockAuthStoreService.addUser(mockUser);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(loginId);

        CacheRBAC cache = new CacheRBACImpl(mockAuthStoreService, mockRbacService);

        String apiAccessActionId = "GENERAL_API_ACCESS";
        String secretsGetActionId = "SECRETS_GET_UNREDACTED_VALUES";

        // When...
        boolean isApiAccessPermitted = cache.isActionPermitted(loginId, apiAccessActionId);
        boolean isSecretsAccessPermitted = cache.isActionPermitted(loginId, secretsGetActionId);

        // Then...
        assertThat(isApiAccessPermitted).isTrue();
        assertThat(isSecretsAccessPermitted).isTrue();
    }

    // TODO: Ignore for now as the auth store is used directly to pull users instead of a cache
    @Ignore
    @Test
    public void testIsActionPermittedUpdatesCacheWhenUserIsNotCached() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);

        mockAuthStoreService.addUser(mockUser);

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACService();
        CacheRBAC cache = new CacheRBACImpl(mockAuthStoreService, mockRbacService);

        String apiAccessActionId = "GENERAL_API_ACCESS";
        String secretsGetActionId = "SECRETS_GET_UNREDACTED_VALUES";
        List<String> actions = List.of(apiAccessActionId, secretsGetActionId);

        cache.addUser(loginId, actions);

        // When...
        boolean isApiAccessPermitted = cache.isActionPermitted(loginId, apiAccessActionId);
        boolean isSecretsAccessPermitted = cache.isActionPermitted(loginId, secretsGetActionId);

        // Then...
        assertThat(isApiAccessPermitted).isTrue();
        assertThat(isSecretsAccessPermitted).isTrue();
    }

    @Test
    public void testIsActionPermittedReturnsFalseForInvalidMappings() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);
        mockUser.setRoleId("2");

        mockAuthStoreService.addUser(mockUser);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(loginId);

        CacheRBAC cache = new CacheRBACImpl(mockAuthStoreService, mockRbacService);

        String apiAccessActionId = "GENERAL_API_ACCESS";
        String secretsGetActionId = "SECRETS_GET_UNREDACTED_VALUES";
        List<String> actions = List.of(apiAccessActionId, secretsGetActionId);

        cache.addUser(loginId, actions);

        // Then...
        assertThat(cache.isActionPermitted(loginId, "not_a_permitted_action")).isFalse();
    }

    @Test
    public void testIsActionPermittedThrowsErrorForUnknownUsers() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACService();
        CacheRBAC cache = new CacheRBACImpl(mockAuthStoreService, mockRbacService);
        String loginId = "unknown";
        String apiAccessActionId = "GENERAL_API_ACCESS";

        // When...
        RBACException thrown = catchThrowableOfType(() -> {
            cache.isActionPermitted(loginId, apiAccessActionId);
        }, RBACException.class);

        // Then...
        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("No user with the given login ID exists");
    }

    // TODO: Ignore for now as users are fetched from the auth store
    @Ignore
    @Test
    public void testInvalidateRemovesUserFromCache() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACService();
        CacheRBAC cache = new CacheRBACImpl(mockAuthStoreService, mockRbacService);
        String loginId = "bob";

        String apiAccessActionId = "GENERAL_API_ACCESS";
        String secretsGetActionId = "SECRETS_GET_UNREDACTED_VALUES";
        List<String> actions = List.of(apiAccessActionId, secretsGetActionId);

        cache.addUser(loginId, actions);
        assertThat(cache.isActionPermitted(loginId, apiAccessActionId)).isTrue();

        // When...
        cache.invalidateUser(loginId);

        // Then...
        RBACException thrown = catchThrowableOfType(() -> {
            cache.isActionPermitted(loginId, apiAccessActionId);
        }, RBACException.class);

        assertThat(thrown).isNotNull();
        assertThat(thrown.getMessage()).contains("No user with the given login ID exists");
    }
}
