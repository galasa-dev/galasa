/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api;

public interface IRateLimiter {
    boolean tryToAcquireToken();
}
