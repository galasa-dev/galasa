/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.Iterator;
import java.util.Map;

import org.junit.*;

import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

import static org.assertj.core.api.Assertions.*;

public class TestRBACServiceImpl {
    
    @Test
    public void testRolesMapByIdContainsAdminRole() throws Exception {
        RBACService service = new RBACServiceImpl();
        Map<String,Role> roleMap = service.getRolesMapById();

        Role roleGot = roleMap.get("2");
        assertThat(roleGot).isNotNull();
        assertThat(roleGot.getName()).isEqualTo("admin");
        assertThat(roleGot.getDescription()).contains("Administrator access");

        assertThat(roleGot.getActionIds())
            .hasSize(3)
            .contains("USER_ROLE_UPDATE_ANY")
            .contains("SECRETS_GET")
            .contains("GENERAL_API_ACCESS");
    }

    @Test
    public void testRolesMapByIdContainsTesterRole() throws Exception {
        RBACService service = new RBACServiceImpl();
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
        RBACService service = new RBACServiceImpl();
        Map<String,Role> roleMap = service.getRolesMapById();

        Role roleGot = roleMap.get("0");
        assertThat(roleGot).isNotNull();
        assertThat(roleGot.getName()).isEqualTo("deactivated");

        assertThat(roleGot.getActionIds())
        .hasSize(0);
    }

    @Test 
    public void testActionsMapByIdContainsActionUserRoleUpdateAny() throws Exception {
        RBACService service = new RBACServiceImpl();
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("USER_ROLE_UPDATE_ANY");
        assertThat(action.getId()).isEqualTo("USER_ROLE_UPDATE_ANY");
    }


    @Test 
    public void testActionsMapByIdContainsActionSecretsGet() throws Exception {
        RBACService service = new RBACServiceImpl();
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("SECRETS_GET");
        assertThat(action.getId()).isEqualTo("SECRETS_GET");
    }

    @Test 
    public void testActionsMapByIdContainsActionGeneralApiAccess() throws Exception {
        RBACService service = new RBACServiceImpl();
        Map<String,Action> actionMap = service.getActionsMapById();

        Action action = actionMap.get("GENERAL_API_ACCESS");
        assertThat(action.getId()).isEqualTo("GENERAL_API_ACCESS");
    }

    @Test
    public void testActionsMapByNameContainsSecretsGet() throws Exception {
        RBACService service = new RBACServiceImpl();
        Map<String,Action> actionMapById = service.getActionsMapById();

        Action action = actionMapById.get("SECRETS_GET");

        assertThat(action.getId()).isEqualTo("SECRETS_GET");
    }

    @Test
    public void testServiceCanLookupAdminRoleById() throws Exception {
        RBACService service = new RBACServiceImpl();
        Role roleGotBack = service.getRoleById("2");
        assertThat(roleGotBack.getName()).isEqualTo("admin");
    }

    @Test
    public void testServiceCanLookupGetSecretsActionById() throws Exception {
        RBACService service = new RBACServiceImpl();
        Action actionGotBack = service.getActionById("SECRETS_GET");
        assertThat(actionGotBack.getId()).isEqualTo("SECRETS_GET");
    }

    @Test
    public void testServiceCanLookupGetSecretsActionByName() throws Exception {
        RBACService service = new RBACServiceImpl();
        Action actionGotBack = service.getActionById("SECRETS_GET");
        assertThat(actionGotBack.getId()).isEqualTo("SECRETS_GET");
    }

    @Test
    public void testGetSecretsActionHasDescription() throws Exception {
        RBACService service = new RBACServiceImpl();
        Action actionGotBack = service.getActionById("SECRETS_GET");
        assertThat(actionGotBack.getDescription()).contains("Able to get secret values");
    }

    @Test
    public void testActionsAreSorted() throws Exception {
        RBACService service = new RBACServiceImpl();
        Iterator<Action> walker = service.getActionsSortedByName().iterator();
        assertThat(walker.hasNext()).isTrue();
        // Only check the first one. Should be enough...
        assertThat(walker.next().getId()).isEqualTo("GENERAL_API_ACCESS");
    }

    @Test
    public void testRolesAreSorted() throws Exception {
        RBACService service = new RBACServiceImpl();
        Iterator<Role> walker = service.getRolesSortedByName().iterator();
        assertThat(walker.hasNext()).isTrue();
        // Only check the first one (alphabetically). Should be enough...
        assertThat(walker.next().getName()).isEqualTo("admin");
    }

    @Test
    public void testDefaultRoleIsAdmin() throws Exception {
        RBACService service = new RBACServiceImpl();
        String defaultRoleId = service.getDefaultRoleId();
        Role defaultRole = service.getRoleById(defaultRoleId);
        assertThat(defaultRole).isNotNull();
        assertThat(defaultRole.getName()).isEqualTo("admin");
    }
}
