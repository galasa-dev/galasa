/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.List;

import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

public class CacheRBACImpl implements CacheRBAC {

    private IAuthStoreService authStoreService;
    private RBACService rbacService;
    
    public CacheRBACImpl(IAuthStoreService authStoreService, RBACService rbacService) {
        this.authStoreService = authStoreService;
        this.rbacService = rbacService;
    }

    @Override
    public synchronized void addUser(String loginId, List<String> actionIds) throws RBACException {
        // Do nothing for now...
    }

    @Override
    public synchronized boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        IUser user = getUserFromAuthStore(loginId);
        String userRoleId = user.getRoleId();
        if (userRoleId == null) {
            userRoleId = rbacService.getDefaultRoleId();
            user.setRoleId(userRoleId);
        }
        Role userRole = rbacService.getRoleById(userRoleId);

        // Check if the user is allowed to perform the given action
        return userRole.getActionIds().contains(actionId);
    }

    @Override
    public synchronized void invalidateUser(String loginId) throws RBACException {
        // Do nothing for now...
    }

    private synchronized IUser getUserFromAuthStore(String loginId) throws RBACException {
        IUser user = null;
        try {
            user = authStoreService.getUserByLoginId(loginId);
            if (user == null) {
                throw new RBACException("No user with the given login ID exists");
            }
        } catch (AuthStoreException e) {
            throw new RBACException("Unable to find user with the given login ID", e);
        }
        return user;
    }
}
