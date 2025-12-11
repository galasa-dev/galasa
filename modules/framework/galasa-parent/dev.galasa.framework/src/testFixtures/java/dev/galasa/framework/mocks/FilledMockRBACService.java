/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;
import java.util.stream.Collectors;

import java.util.ArrayList;

import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.Role;

public class FilledMockRBACService {
    
    public static MockRBACService createTestRBACService() {
        
        // CAN_DO_SOMETHING action
        MockAction action1 = new MockAction("CAN_DO_SOMETHING", "Can do something name" , "Can do something description");

        // CAN_DO_SOMETHING_ELSE action
        MockAction action2 = new MockAction("CAN_DO_SOMETHING_ELSE","can do something else name", "can do something else description");


        String[] actionIDs = new String[2];
        actionIDs[0] = action1.getId();
        actionIDs[1] = action2.getId();

        List<String> actionIDsList = new ArrayList<String>();
        actionIDsList.add(action1.getId());
        actionIDsList.add(action2.getId());

        List<Action> actions = new ArrayList<Action>();
        actions.add(action1);
        actions.add(action2);

        MockRole role1 = new MockRole("role1","2","role1 description",actionIDsList,true);
        
        List<Role> roles = new ArrayList<Role>();
        roles.add(role1);

        MockRBACService service = new MockRBACService(roles,actions,role1);

        return service;
    }

    public static MockRBACService createTestRBACServiceWithTestUser(String loginId) {
        return createTestRBACServiceWithTestUser(loginId, BuiltInAction.getActions());
    }

    public static MockRBACService createTestRBACServiceWithTestUser(String loginId, List<Action> actions) {
        MockUser user = new MockUser();
        user.setLoginId(loginId);
        user.setPriority(1);

        return createTestRBACServiceWithTestUser(user, actions);
    }

    public static MockRBACService createTestRBACServiceWithTestUser(MockUser user, List<Action> actions) {
        
        List<String> actionIDsList = actions.stream().map(action -> action.getId()).collect(Collectors.toList());

        MockRole role1 = new MockRole("role1","2","role1 description",actionIDsList,true);
        MockRole ownerRole = new MockRole("owner","0","owner description",actionIDsList,true);
        
        List<Role> roles = new ArrayList<Role>();
        roles.add(role1);
        roles.add(ownerRole);

        user.setRoleId(role1.getId());

        MockRBACService service = new MockRBACService(roles,actions,role1);

        List<IUser> users = new ArrayList<>();
        users.add(user);

        service.setUsersCache(new MockCacheUsers(service, users));

        return service;
    }
}
