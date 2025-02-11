/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.*;

import dev.galasa.framework.mocks.FilledMockRBACService;
import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockIDynamicStatusStoreService;
import dev.galasa.framework.mocks.MockRBACService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.mocks.MockUser;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.RBACException;

import static org.assertj.core.api.Assertions.*;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

public class TestCacheRBACImpl {

    @Test
    public void testIsActionPermittedReturnsTrueForValidMappings() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockIDynamicStatusStoreService mockDssService = new MockIDynamicStatusStoreService();

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);
        mockUser.setRoleId("2");

        mockAuthStoreService.addUser(mockUser);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(loginId);

        CacheRBAC cache = new CacheRBACImpl(mockDssService, mockAuthStoreService, mockRbacService);

        String apiAccessActionId = "GENERAL_API_ACCESS";
        String secretsGetActionId = "SECRETS_GET_UNREDACTED_VALUES";

        // When...
        boolean isApiAccessPermitted = cache.isActionPermitted(loginId, apiAccessActionId);
        boolean isSecretsAccessPermitted = cache.isActionPermitted(loginId, secretsGetActionId);

        // Then...
        assertThat(isApiAccessPermitted).isTrue();
        assertThat(isSecretsAccessPermitted).isTrue();
    }

    @Test
    public void testIsActionPermittedUpdatesCacheWhenUserIsNotCached() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockIDynamicStatusStoreService mockDssService = new MockIDynamicStatusStoreService();

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);
        mockUser.setRoleId("2");

        mockAuthStoreService.addUser(mockUser);

        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction(), CPS_PROPERTIES_SET.getAction());

        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(loginId, permittedActions);
        CacheRBAC cache = new CacheRBACImpl(mockDssService, mockAuthStoreService, mockRbacService);

        Map<String, String> dssData = mockDssService.data;
        assertThat(dssData).isEmpty();

        // When...
        boolean isApiAccessPermitted = cache.isActionPermitted(loginId, GENERAL_API_ACCESS.getAction().getId());

        // Then...
        assertThat(isApiAccessPermitted).isTrue();
        assertThat(dssData).hasSize(1);

        // The DSS should have a property of the form:
        // dss.rbac.loginId.actions = GENERAL_API_ACCESS,CPS_PROPERTIES_SET
        String[] actualActions = dssData.get("user." + loginId + ".actions").split(",");
        assertThat(actualActions).containsExactlyInAnyOrder("GENERAL_API_ACCESS", "CPS_PROPERTIES_SET");
    }

    @Test
    public void testIsActionPermittedReturnsFalseForInvalidMappings() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockIDynamicStatusStoreService mockDssService = new MockIDynamicStatusStoreService();

        String loginId = "bob";
        MockUser mockUser = new MockUser();
        mockUser.setLoginId(loginId);
        mockUser.setRoleId("2");

        mockAuthStoreService.addUser(mockUser);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACServiceWithTestUser(loginId);

        CacheRBAC cache = new CacheRBACImpl(mockDssService, mockAuthStoreService, mockRbacService);

        List<Action> permittedActions = List.of(GENERAL_API_ACCESS.getAction(), SECRETS_GET_UNREDACTED_VALUES.getAction());
        Set<String> permittedActionsIds = permittedActions.stream().map(Action::getId).collect(Collectors.toSet());

        cache.addUser(loginId, permittedActionsIds);

        // Then...
        assertThat(cache.isActionPermitted(loginId, "not_a_permitted_action")).isFalse();
    }

    // If the user has a valid JWT, but their user record has been deleted, then 
    // the cache will be empty, and the user will have no user record.
    // If they are checking permissions, then a boolean should be returned rather
    // than an exception.
    @Test
    public void testIsActionPermittedSaysNotPermittedForUnknownUsers() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACService();
        MockIDynamicStatusStoreService mockDssService = new MockIDynamicStatusStoreService();

        CacheRBAC cache = new CacheRBACImpl(mockDssService, mockAuthStoreService, mockRbacService);
        String loginId = "unknown";
        String apiAccessActionId = "GENERAL_API_ACCESS";

        // When...
        boolean isPermitted = cache.isActionPermitted(loginId, apiAccessActionId);

        // Then...
        assertThat(isPermitted).isFalse();
    }

    @Test
    public void testInvalidateRemovesUserFromCache() throws Exception {
        // Given...
        MockTimeService timeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(timeService);
        MockRBACService mockRbacService = FilledMockRBACService.createTestRBACService();
        MockIDynamicStatusStoreService mockDssService = new MockIDynamicStatusStoreService();

        CacheRBAC cache = new CacheRBACImpl(mockDssService, mockAuthStoreService, mockRbacService);
        String loginId = "bob";

        List<Action> permittedActions = BuiltInAction.getActions();
        Set<String> permittedActionsIds = permittedActions.stream().map(Action::getId).collect(Collectors.toSet());

        cache.addUser(loginId, permittedActionsIds);

        Map<String, String> dssData = mockDssService.data;
        assertThat(dssData).hasSize(1);
        assertThat(dssData).containsKey("user." + loginId + ".actions");

        // When...
        cache.invalidateUser(loginId);

        // Then...
        assertThat(dssData).isEmpty();
    }
}
