/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.galasa.framework.internal.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;

public class MockCacheRBAC implements CacheRBAC {

    private Map<String, List<String>> usersToActionsMap;

    public MockCacheRBAC() {
        usersToActionsMap = new HashMap<>();
    }

    public MockCacheRBAC(Map<String, List<String>> usersToActionsMap) {
        this.usersToActionsMap = usersToActionsMap;
    }

    @Override
    public void addUser(String loginId, Set<String> actionIds) throws RBACException {
        usersToActionsMap.put(loginId, new ArrayList<>(actionIds));
    }

    @Override
    public boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        List<String> userActions = usersToActionsMap.get(loginId);
        return userActions.contains(actionId);
    }

    @Override
    public void invalidateUser(String loginId) throws RBACException {
        usersToActionsMap.remove(loginId);
    }
}
