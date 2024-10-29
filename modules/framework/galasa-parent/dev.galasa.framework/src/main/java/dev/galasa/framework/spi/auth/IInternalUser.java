/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

/**
 * An interface for user beans used internally to implement. This allows user details to
 * be retrieved and used without exposure via API server responses.
 */
public interface IInternalUser {

    String getDexUserId();

    String getLoginId();
}
