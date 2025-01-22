/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import org.junit.*;

import dev.galasa.framework.mocks.MockAuthStoreService;
import dev.galasa.framework.mocks.MockTimeService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

import static org.assertj.core.api.Assertions.*;

public class TestRBACServiceImpl {
    
    @Test
    public void testRolesMapByIdContainsAdminRole() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Role> roleMap = service.getRolesMapById();

        Role roleGot = roleMap.get("2");
        assertThat(roleGot).isNotNull();
        assertThat(roleGot.getName()).isEqualTo("admin");
        assertThat(roleGot.getDescription()).contains("Administrator access");

        assertThat(roleGot.getActionIds())
            .hasSize(4)
            .contains("USER_ROLE_UPDATE_ANY")
            .contains("SECRETS_GET_UNREDACTED_VALUES")
            .contains("GENERAL_API_ACCESS")
            .contains("CPS_PROPERTIES_SET");
    }

    @Test
    public void testRolesMapByIdContainsTesterRole() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Role> roleMap = service.getRolesMapById();

        Role roleGot = roleMap.get("1");
        assertThat(roleGot).isNotNull();
        assertThat(roleGot.getName()).isEqualTo("tester");

        assertThat(roleGot.getActionIds())
            .hasSize(2)
            .contains("USER_ROLE_UPDATE_ANY")
            .contains("GENERAL_API_ACCESS");
    }

    @Test
    public void testRolesMapByIdContainsDeactivateddRole() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Role> roleMap = service.getRolesMapById();

        Role roleGot = roleMap.get("0");
        assertThat(roleGot).isNotNull();
        assertThat(roleGot.getName()).isEqualTo("deactivated");

        assertThat(roleGot.getActionIds())
        .hasSize(0);
    }

    @Test 
    public void testActionsMapByIdContainsActionUserRoleUpdateAny() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("USER_ROLE_UPDATE_ANY");
        assertThat(action.getId()).isEqualTo("USER_ROLE_UPDATE_ANY");
    }


    @Test 
    public void testActionsMapByIdContainsActionSecretsGet() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("SECRETS_GET_UNREDACTED_VALUES");
        assertThat(action.getId()).isEqualTo("SECRETS_GET_UNREDACTED_VALUES");
    }

    @Test 
    public void testActionsMapByIdContainsActionGeneralApiAccess() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("GENERAL_API_ACCESS");
        assertThat(action.getId()).isEqualTo("GENERAL_API_ACCESS");
    }

    @Test 
    public void testActionsMapByIdContainsActionCpsPropertiesSet() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("CPS_PROPERTIES_SET");
        assertThat(action.getId()).isEqualTo("CPS_PROPERTIES_SET");
    }

    @Test
    public void testActionsMapByNameContainsSecretsGet() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMapById = service.getActionsMapById();

        Action action = actionMapById.get("SECRETS_GET_UNREDACTED_VALUES");

        assertThat(action.getId()).isEqualTo("SECRETS_GET_UNREDACTED_VALUES");
    }

    @Test
    public void testServiceCanLookupAdminRoleById() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Role roleGotBack = service.getRoleById("2");
        assertThat(roleGotBack.getName()).isEqualTo("admin");
    }

    @Test
    public void testServiceCanLookupGetSecretsActionById() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Action actionGotBack = service.getActionById("SECRETS_GET_UNREDACTED_VALUES");
        assertThat(actionGotBack.getId()).isEqualTo("SECRETS_GET_UNREDACTED_VALUES");
    }

    @Test
    public void testServiceCanLookupGetSecretsActionByName() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Action actionGotBack = service.getActionById("SECRETS_GET_UNREDACTED_VALUES");
        assertThat(actionGotBack.getId()).isEqualTo("SECRETS_GET_UNREDACTED_VALUES");
    }

    @Test
    public void testGetSecretsActionHasDescription() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Action actionGotBack = service.getActionById("SECRETS_GET_UNREDACTED_VALUES");
        assertThat(actionGotBack.getDescription()).contains("Able to get unredacted secret values");
    }

    @Test 
    public void testSetCpsPropertiesActionHasDescription() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("CPS_PROPERTIES_SET");
        assertThat(action.getDescription()).isEqualTo("Able to set CPS properties");
    }

    @Test
    public void testActionsAreSorted() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Iterator<Action> walker = service.getActionsSortedByName().iterator();
        assertThat(walker.hasNext()).isTrue();
        // Only check the first one. Should be enough...
        assertThat(walker.next().getId()).isEqualTo("CPS_PROPERTIES_SET");
    }

    @Test
    public void testRolesAreSorted() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        Iterator<Role> walker = service.getRolesSortedByName().iterator();
        assertThat(walker.hasNext()).isTrue();
        // Only check the first one (alphabetically). Should be enough...
        assertThat(walker.next().getName()).isEqualTo("admin");
    }

    @Test
    public void testDefaultRoleIsAdmin() throws Exception {
        MockTimeService mockTimeService = new MockTimeService(Instant.now());
        MockAuthStoreService mockAuthStoreService = new MockAuthStoreService(mockTimeService);
        RBACService service = new RBACServiceImpl(mockAuthStoreService);
        String defaultRoleId = service.getDefaultRoleId();
        Role defaultRole = service.getRoleById(defaultRoleId);
        assertThat(defaultRole).isNotNull();
        assertThat(defaultRole.getName()).isEqualTo("admin");
    }
}
