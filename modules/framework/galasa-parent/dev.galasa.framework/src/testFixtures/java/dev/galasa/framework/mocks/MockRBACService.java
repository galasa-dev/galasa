/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

public class MockRBACService implements RBACService {

    private final Map<String,Role> roleMapById;
    private final Map<String,Role> roleMapByName;
    private final Map<String,Action> actionMapById;
    private final Map<String,Action> actionMapByName;
    private List<Action> actionsSortedByName;
    private List<Role> rolesSortedByName;
    private Role defaultRole;
    private CacheRBAC usersToActionsCache;

    public MockRBACService( List<Role> roles, List<Action> actions, Role defaultRole ) {
        this.usersToActionsCache = new MockCacheRBAC();
        this.defaultRole = defaultRole;

        roleMapById = new HashMap<String,Role>();
        roleMapByName = new HashMap<String,Role>();
        for(Role role: roles) {
            roleMapById.put(role.getId(),role);
            roleMapByName.put(role.getName(),role);
        }
        
        actionMapById = new HashMap<String,Action>();
        actionMapByName = new HashMap<String,Action>();
        for(Action action: actions) {
            actionMapById.put(action.getId(),action);
            actionMapByName.put(action.getName(),action);
        }

        actionsSortedByName = new ArrayList<Action>(actions);
        Comparator<Action> actionNameComparator = (action1, action2)-> action1.getName().compareTo(action2.getName());
        Collections.sort(actionsSortedByName, actionNameComparator );

        rolesSortedByName = new ArrayList<Role>(roles);
        Comparator<Role> roleNameComparator = (action1, action2)-> action1.getName().compareTo(action2.getName());
        Collections.sort(rolesSortedByName, roleNameComparator );
    }

    @Override
    public Map<String, Role> getRolesMapById() throws RBACException {
        return this.roleMapById;
    }

    @Override
    public Map<String, Action> getActionsMapById() throws RBACException {
        return this.actionMapById;
    }

    @Override
    public Role getRoleById(String id) throws RBACException {
       return getRolesMapById().get(id);
    }

    @Override
    public Action getActionById(String id) throws RBACException {

        return getActionsMapById().get(id);  
    }

    @Override
    public List<Role> getRolesSortedByName() throws RBACException {
        return rolesSortedByName;
    }

    @Override
    public List<Action> getActionsSortedByName() throws RBACException {
        return actionsSortedByName;
    }

    @Override
    public String getDefaultRoleId() throws RBACException {
       return this.defaultRole.getId();
    }

    @Override
    public Role getRoleByName(String roleNameWanted) throws RBACException {
        return this.roleMapByName.get(roleNameWanted);
    }

    public void setUsersActionsCache(MockCacheRBAC cache) {
        this.usersToActionsCache = cache;
    }

    @Override
    public boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        return usersToActionsCache.isActionPermitted(loginId, actionId);
    }

    @Override
    public void invalidateUser(String loginId) throws RBACException {
        usersToActionsCache.invalidateUser(loginId);
    }
}
