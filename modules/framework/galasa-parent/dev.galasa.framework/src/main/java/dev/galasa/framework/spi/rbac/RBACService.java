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
