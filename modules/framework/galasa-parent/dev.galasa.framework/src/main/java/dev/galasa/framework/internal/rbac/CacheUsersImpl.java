/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.rbac.Role;

import org.apache.commons.logging.*;

public class CacheUsersImpl implements CacheUsers {

    // Only keep users property entries in the cache for 24 hours
    private static final long CACHED_USER_TIME_TO_LIVE_SECS = 24 * 60 * 60;

    private static final String USER_PROPERTY_PREFIX = "user.";
    private static final String ACTIONS_PROPERTY_SUFFIX = ".actions";
    private static final String PRIORITY_PROPERTY_SUFFIX = ".priority";

    private IDynamicStatusStoreService dssService;
    private IAuthStoreService authStoreService;
    private RBACService rbacService;
    private final Log logger = LogFactory.getLog(getClass());

    public CacheUsersImpl(
        IDynamicStatusStoreService dssService,
        IAuthStoreService authStoreService,
        RBACService rbacService
    ) {
        this.dssService = dssService;
        this.authStoreService = authStoreService;
        this.rbacService = rbacService;
    }

    @Override
    public synchronized void addUser(IUser user) throws RBACException {
        try {
            String loginId = user.getLoginId();
            Set<String> actionIds = getUserActionsFromAuthStore(user);

            // The users-to-actions DSS property is in the form:
            // dss.rbac.user.<loginId>.actions = <comma-separated action IDs>
            String actionsKey = getSuffixedUserPropertyKey(loginId, ACTIONS_PROPERTY_SUFFIX);
            String commaSeparatedActionIds = String.join(",", actionIds);

            // Add the user's priority value to the cache
            String priorityKey = getSuffixedUserPropertyKey(loginId, PRIORITY_PROPERTY_SUFFIX);
            String userPriority = Long.toString(user.getPriority());

            Map<String, String> propertiesToSet = new HashMap<>();
            propertiesToSet.put(actionsKey, commaSeparatedActionIds);
            propertiesToSet.put(priorityKey, userPriority);

            dssService.put(propertiesToSet, CACHED_USER_TIME_TO_LIVE_SECS);
        } catch (DynamicStatusStoreException e) {
            throw new RBACException("Failed to cache user properties", e);
        }
    }

    @Override
    public synchronized boolean isActionPermitted(String loginId, String actionId) throws RBACException {

        boolean isActionPermitted = false;
        try {
            String userActionsKey = getSuffixedUserPropertyKey(loginId, ACTIONS_PROPERTY_SUFFIX);
            String commaSeparatedUserActions = dssService.get(userActionsKey);

            Set<String> userActions = new HashSet<>();
            if (commaSeparatedUserActions == null) {
                // Cache miss, so get the user's actions from the auth store
                IUser user = getUserFromAuthStore(loginId);
                if (user==null) {
                    // The user record doesn't exist.
                    // So we know the user isn't permitted right now.
                    logger.info("User does not have a user record. Permission denied.");
                    isActionPermitted = false;
                } else {

                    userActions = getUserActionsFromAuthStore(user);

                    // Add this user to the cache
                    addUser(user);
                }
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
            // Delete all cached properties related to this user using a property prefix in the form:
            // user.<login ID>.
            String userPropertiesPrefix = USER_PROPERTY_PREFIX + loginId + ".";
            dssService.deletePrefix(userPropertiesPrefix);
        } catch (DynamicStatusStoreException e) {
            throw new RBACException("Failed to delete cached user properties", e);
        }
    }

    private synchronized IUser getUserFromAuthStore(String loginId) throws RBACException {
        IUser user = null;
        try {
            user = authStoreService.getUserByLoginId(loginId);
        } catch (AuthStoreException e) {
            throw new RBACException("Internal Server Error: Authorisation store returned an unexpected failure when looking up a user record.", e);
        }
        return user;
    }

    private Set<String> getUserActionsFromAuthStore(IUser user) throws RBACException {
        String userRoleId = user.getRoleId();
        Role userRole = rbacService.getRoleById(userRoleId);

        return new HashSet<>(userRole.getActionIds());
    }

    private String getSuffixedUserPropertyKey(String loginId, String suffix) {
        return USER_PROPERTY_PREFIX + loginId + suffix;
    }

    @Override
    public long getUserPriority(String loginId) throws RBACException {
        long priority = 0;
        try {
            String userPriorityKey = getSuffixedUserPropertyKey(loginId, PRIORITY_PROPERTY_SUFFIX);
            String userPriorityStr = dssService.get(userPriorityKey);

            if (userPriorityStr == null || userPriorityStr.isBlank()) {
                // Cache miss, so get the user's priority points from the auth store
                IUser user = getUserFromAuthStore(loginId);
                if (user != null) {
                    // Add this user to the cache
                    priority = user.getPriority();
                    addUser(user);
                }

            } else {
                priority = Long.parseLong(userPriorityStr);
            }
        } catch (DynamicStatusStoreException e) {
            logger.warn("Failed to access DSS to get user priority, using priority " + priority);
        } catch (NumberFormatException e ) {
            logger.warn("Invalid priority set for user, using priority " + priority);
        }
        return priority;
    }
}
