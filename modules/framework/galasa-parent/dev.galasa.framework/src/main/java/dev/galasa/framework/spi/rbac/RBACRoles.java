/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.rbac;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.galasa.framework.internal.rbac.RoleImpl;
import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

public enum RBACRoles {

    DEACTIVATED(new RoleImpl("deactivated", "0", "User has no access", new ArrayList<>(), true)),
    TESTER(new RoleImpl("tester", "1", "Test developer and runner",
            List.of(TEST_RUN_LAUNCH.getAction().getId(), USER_EDIT_OTHER.getAction().getId(), GENERAL_API_ACCESS.getAction().getId()), true)),
    ADMIN(new RoleImpl("admin", "2", "Administrator access", getAllActionIds(), true)),
    OWNER(new RoleImpl("owner", "3", "Galasa service owner", getAllActionIds(), false));

    private final Role role;

    RBACRoles(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return this.role;
    }

    private static List<String> getAllActionIds() {
        List<Action> allActions = BuiltInAction.getActions();
        List<Action> sortedActions = new ArrayList<>(allActions);

        // Sort by name, for example
        sortedActions.sort(Comparator.comparing(Action::getName));

        List<String> actionIds = new ArrayList<>();
        for (Action action : sortedActions) {
            actionIds.add(action.getId());
        }

        return actionIds;
    }
}
