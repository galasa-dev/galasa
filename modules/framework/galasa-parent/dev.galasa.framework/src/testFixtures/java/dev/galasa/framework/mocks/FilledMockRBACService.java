/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;
import java.util.ArrayList;
import dev.galasa.framework.spi.rbac.Action;
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

        MockRole role1 = new MockRole("role1","2","role1 description",actionIDsList);
        
        List<Role> roles = new ArrayList<Role>();
        roles.add(role1);

        MockRBACService service = new MockRBACService(roles,actions,role1);

        return service;
    }
}
