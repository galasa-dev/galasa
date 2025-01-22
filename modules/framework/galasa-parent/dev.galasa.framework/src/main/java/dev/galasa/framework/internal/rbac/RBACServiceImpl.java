/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.BuiltInAction;
import dev.galasa.framework.spi.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

import static dev.galasa.framework.spi.rbac.BuiltInAction.*;

public class RBACServiceImpl implements RBACService {

    private static CacheRBAC userActionsCache;

    private static final List<Action> allActionsUnsorted = BuiltInAction.getActions();

    private static List<Action> actionsSortedByName ;

    private static Map<String,Action> actionsMapById ;

    private static Role roleAdmin ;
    private static Role roleTester;

    private static Role roleDeactivated;

    private static List<Role> rolesSortedByName ;

    private static Map<String,Role> rolesMapById = new HashMap<String,Role>();

    {
        actionsSortedByName = new ArrayList<Action>(allActionsUnsorted);
        Comparator<Action> nameComparator = (action1, action2)-> action1.getName().compareTo(action2.getName());
        Collections.sort(actionsSortedByName, nameComparator );

        List<String> allActionIds = new ArrayList<String>();
        for( Action action: allActionsUnsorted) {
            allActionIds.add(action.getId());
        }

        actionsMapById = new HashMap<String,Action>();
        for(Action action: allActionsUnsorted) {
            actionsMapById.put(action.getId(),action);
        }

        roleAdmin= new RoleImpl("admin","2","Administrator access",allActionIds);

        roleTester = new RoleImpl("tester", "1", "Test developer and runner", 
            List.of( USER_ROLE_UPDATE_ANY.getAction().getId() , GENERAL_API_ACCESS.getAction().getId() )   
        );

        roleDeactivated = new RoleImpl("deactivated", "0", "User has no access", new ArrayList<String>());

        List<Role> rolesUnsorted = List.of(roleAdmin, roleTester, roleDeactivated);


        rolesSortedByName = new ArrayList<Role>(rolesUnsorted);
        Comparator<Role> roleNameComparator = (role1, role2)-> role1.getName().compareTo(role2.getName());
        Collections.sort(rolesSortedByName, roleNameComparator );

        for( Role role : rolesUnsorted ) {
            rolesMapById.put( role.getId(), role);
        }
    }

    public RBACServiceImpl(IDynamicStatusStoreService dssService, IAuthStoreService authStoreService) {
        userActionsCache = new CacheRBACImpl(dssService, authStoreService, this);
    }

    @Override
    public Map<String,Role> getRolesMapById() {
        return rolesMapById;
    }

    @Override
    public List<Role> getRolesSortedByName() {
        return rolesSortedByName;
    }

    @Override
    public Map<String,Action> getActionsMapById() {
        return actionsMapById;
    }

    @Override
    public Role getRoleById(String id) {
        return getRolesMapById().get(id);
    }

    @Override
    public Action getActionById(String id) {
        return getActionsMapById().get(id);
    }

    @Override
    public List<Action> getActionsSortedByName() throws RBACException {
        return actionsSortedByName;
    }

    @Override
    public String getDefaultRoleId() throws RBACException {
        // We currently don't want to lock anyone out of doing anything, so defaulting to use the admin role for everyone 
        // without a role already.
        return roleAdmin.getId();
    }

    @Override
    public Role getRoleByName(String roleNameWanted) throws RBACException {
        Role roleFound = null;

        for (Role possibleMatch: rolesSortedByName) {
            if (possibleMatch.getName().equals(roleNameWanted)) {
                roleFound = possibleMatch;
                break;
            }
        }
        return roleFound; 
    }

    @Override
    public boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        return userActionsCache.isActionPermitted(loginId, actionId);
    }

    @Override
    public void invalidateUser(String loginId) throws RBACException {
        userActionsCache.invalidateUser(loginId);
    }
}
