/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.time.Instant;

public interface IFrontEndClient {
    String getClientName();
    Instant getLastLogin();
    void setLastLogin(Instant lastLoginTime);
}
