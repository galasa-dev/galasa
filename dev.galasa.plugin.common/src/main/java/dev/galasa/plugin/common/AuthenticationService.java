/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

public interface AuthenticationService {
    public String getJWT() throws AuthenticationException ;
}
