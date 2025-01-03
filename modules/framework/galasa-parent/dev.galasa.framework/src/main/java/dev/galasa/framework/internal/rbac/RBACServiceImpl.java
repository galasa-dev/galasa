/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.rbac.Action;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

public class RBACServiceImpl implements RBACService {

    private static final Action actionUserRoleUpdateAny = new ActionImpl("USER_ROLE_UPDATE_ANY","User role update any", "Able to update the role of any user");
    private static final Action actionSecretsGet = new ActionImpl("SECRETS_GET","Get secrets", "Able to get secret values" );
    private static final Action actionGeneralApiAccess = new ActionImpl("GENERAL_API_ACCESS","General API access", "Able to access the REST API" );

    private static final List<Action> allActionsUnsorted = List.of(
        actionUserRoleUpdateAny, 
        actionSecretsGet, 
        actionGeneralApiAccess);

    private static List<Action> actionsSortedByName ;

    private static Map<String,Action> actionsMapById ;

    private static Role roleAdmin ;
    private static Role roleDefault;

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

        roleDefault = new RoleImpl("tester", "1", "Test developer and runner", 
            List.of( actionUserRoleUpdateAny.getId() , actionGeneralApiAccess.getId() )   
        );

        roleDeactivated = new RoleImpl("deactivated", "0", "User has no access", new ArrayList<String>());

        List<Role> rolesUnsorted = List.of(roleAdmin, roleDefault, roleDeactivated);


        rolesSortedByName = new ArrayList<Role>(rolesUnsorted);
        Comparator<Role> roleNameComparator = (role1, role2)-> role1.getName().compareTo(role2.getName());
        Collections.sort(rolesSortedByName, roleNameComparator );

        for( Role role : rolesUnsorted ) {
            rolesMapById.put( role.getId(), role);
        }
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
    
}
