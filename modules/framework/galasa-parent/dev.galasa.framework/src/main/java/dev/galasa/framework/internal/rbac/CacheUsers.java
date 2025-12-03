/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.rbac;

import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.rbac.RBACException;

public interface CacheUsers {

    void addUser(IUser user) throws RBACException;

    boolean isActionPermitted(String loginId, String actionId) throws RBACException;

    long getUserPriority(String loginId) throws RBACException;

    void invalidateUser(String loginId) throws RBACException;
}
