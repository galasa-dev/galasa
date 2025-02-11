/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.rbac;

import java.util.List;
import java.util.Map;

public interface RBACService {

    /**
     * When a user is created, the user needs to have a role assigned.
     * This will depend upon some configuration done by the system installer.
     * It is drawn from a configMap in kubernetes, exposed as an environment variable here.
     */
    public static final String ENV_VARIABLE_GALASA_DEFAULT_USER_ROLE_NAME = "GALASA_DEFAULT_USER_ROLE";

    /**
     * This is the name of the environment variable which holds a list of user login ids, 
     * separated by commas. Each login id is synthetically treated as being an 'owner' of the
     * Galasa service.
     * 
     * The actual 'owner' role isn't set into their user record, but is just made to appear as
     * the owner role.
     * 
     * Users with the `owner` role cannot have their user record deleted, or their role changed
     * by the REST API or the command-line tool. They are a special form of administrator who has
     * more rights than a general user with `admin` rights. To change the list of users holding the 
     * `owner` role, edit the `values.yaml` file in the helm chart and run the helm upgrade command to
     * push the new configuration to Kubernetes.
     * 
     * Nobody is able to set someone else's user role to be 'owner'
     * 
     * The Helm chart values controls what this list contains.
     * 
     * The reason for doing this? So that there is always a way that a Galasa service systems
     * administrator with access to the helm chart/kubernetes can update the owner of the system.
     * This mechanism can be used to avoid admin lockout situations, where there are no admins
     * left in Galasa or the company organisation who can do administration roles.
     */
    public static final String ENV_VARIABLE_GALASA_OWNER_LOGIN_IDS = "GALASA_OWNER_LOGIN_IDS";

    /**
     * Finds out if the specified user login id is an owner of the Galasa service.
     * @param loginIdToCheck The name used by the user being checked.
     * @return true if the login id is one of the owners of the Galasa service.
     */
    boolean isOwner(String loginIdToCheck);

    /**
     * Gets all the roles available.
     * @return A map. The index is the role ID. The value is the role object.
     */
    Map<String,Role> getRolesMapById() throws RBACException;

    List<Role> getRolesSortedByName() throws RBACException;

    String getDefaultRoleId() throws RBACException;

    /**
     * Gets all the actions available.
     * @return A map. The index is the action id. The value is the action object.
     */
    Map<String,Action> getActionsMapById() throws RBACException;

    List<Action> getActionsSortedByName() throws RBACException;

    Role getRoleById( String id ) throws RBACException;

    Action getActionById( String id ) throws RBACException;

    Role getRoleByName(String roleNameWanted) throws RBACException;

    boolean isActionPermitted(String loginId, String actionId) throws RBACException;

    void invalidateUser(String loginId) throws RBACException;
}
