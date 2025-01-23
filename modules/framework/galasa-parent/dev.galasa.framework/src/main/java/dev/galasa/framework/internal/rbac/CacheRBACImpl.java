/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.HashSet;
import java.util.Set;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.CacheRBAC;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

public class CacheRBACImpl implements CacheRBAC {

    // Only keep users-to-actions entries in the cache for 24 hours
    private static final long CACHED_ACTIONS_TIME_TO_LIVE_SECS = 24 * 60 * 60;

    private static final String USER_PROPERTY_PREFIX = "user.";
    private static final String ACTIONS_PROPERTY_SUFFIX = ".actions";

    private IDynamicStatusStoreService dssService;
    private IAuthStoreService authStoreService;
    private RBACService rbacService;

    public CacheRBACImpl(
        IDynamicStatusStoreService dssService,
        IAuthStoreService authStoreService,
        RBACService rbacService
    ) {
        this.dssService = dssService;
        this.authStoreService = authStoreService;
        this.rbacService = rbacService;
    }

    @Override
    public synchronized void addUser(String loginId, Set<String> actionIds) throws RBACException {
        try {
            String commaSeparatedActionIds = String.join(",", actionIds);
            String actionsKey = getUserActionsPropertyKey(loginId);
            dssService.put(actionsKey, commaSeparatedActionIds, CACHED_ACTIONS_TIME_TO_LIVE_SECS);
        } catch (DynamicStatusStoreException e) {
            throw new RBACException("Failed to cache user actions", e);
        }
    }

    @Override
    public synchronized boolean isActionPermitted(String loginId, String actionId) throws RBACException {
        boolean isActionPermitted = false;
        try {
            String userActionsKey = getUserActionsPropertyKey(loginId);
            String commaSeparatedUserActions = dssService.get(userActionsKey);

            Set<String> userActions = new HashSet<>();
            if (commaSeparatedUserActions == null) {
                // Cache miss, so get the user's actions from the auth store
                userActions = getUserActionsFromAuthStore(loginId);

                // Add this user to the cache
                addUser(loginId, userActions);
            } else {
                userActions = Set.of(commaSeparatedUserActions.split(","));
            }

            // Check if the user is allowed to perform the given action
            isActionPermitted = userActions.contains(actionId);
        } catch (DynamicStatusStoreException e) {
            throw new RBACException("Error occurred when accessing the DSS", e);
        }
        return isActionPermitted;
    }

    @Override
    public synchronized void invalidateUser(String loginId) throws RBACException {
        try {
            String userActionsKey = getUserActionsPropertyKey(loginId);
            dssService.delete(userActionsKey);
        } catch (DynamicStatusStoreException e) {
            throw new RBACException("Failed to delete cached user actions", e);
        }
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

    private Set<String> getUserActionsFromAuthStore(String loginId) throws RBACException {
        IUser user = getUserFromAuthStore(loginId);
        String userRoleId = user.getRoleId();
        Role userRole = rbacService.getRoleById(userRoleId);

        return new HashSet<>(userRole.getActionIds());
    }

    private String getUserActionsPropertyKey(String loginId) {
        // The users-to-actions DSS property is in the form:
        // dss.rbac.user.<loginId>.actions = <comma-separated action IDs>
        return USER_PROPERTY_PREFIX + loginId + ACTIONS_PROPERTY_SUFFIX;
    }
}
