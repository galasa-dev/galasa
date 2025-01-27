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
