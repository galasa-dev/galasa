/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import java.util.Set;

import dev.galasa.framework.spi.rbac.RBACException;

public interface CacheRBAC {

    void addUser(String loginId, Set<String> actionIds) throws RBACException;

    boolean isActionPermitted(String loginId, String actionId) throws RBACException;

    void invalidateUser(String loginId) throws RBACException;
}
