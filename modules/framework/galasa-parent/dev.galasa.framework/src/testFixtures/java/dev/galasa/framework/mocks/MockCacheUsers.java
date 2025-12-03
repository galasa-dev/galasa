/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.internal.rbac.CacheUsers;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;

public class MockCacheUsers implements CacheUsers {

    private List<IUser> cachedUsers;
    private MockRBACService rbacService;

    public MockCacheUsers(MockRBACService rbacService) {
        this(rbacService, new ArrayList<>());
    }

    public MockCacheUsers(MockRBACService rbacService, List<IUser> users) {
        this.rbacService = rbacService;
        this.cachedUsers = users;
    }

    @Override
    public boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        boolean isPermitted = false;
        IUser user = getUserByLoginId(loginId);
        if (user != null) {
            List<String> actionIds = rbacService.getRoleById(user.getRoleId()).getActionIds();
            isPermitted = actionIds.contains(actionId);
        }
        return isPermitted;
    }

    @Override
    public void invalidateUser(String loginId) throws RBACException {
        cachedUsers.removeIf(user -> loginId.equals(user.getLoginId()));
    }

    @Override
    public void addUser(IUser user) throws RBACException {
        this.cachedUsers.add(user);
    }

    @Override
    public long getUserPriority(String loginId) throws RBACException {
        long priority = 0;
        IUser user = getUserByLoginId(loginId);

        if (user != null) {
            priority = user.getPriority();
        }

        return priority;
    }

    private IUser getUserByLoginId(String loginId) throws RBACException {
        IUser matchingUser = null;
        if (loginId != null) {
            for (IUser user : cachedUsers) {
                if (loginId.equals(user.getLoginId())) {
                    matchingUser = user;
                    break;
                }
            }
        }
        return matchingUser;
    }
}
