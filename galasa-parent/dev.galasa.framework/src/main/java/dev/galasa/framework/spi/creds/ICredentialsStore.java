/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.creds;

import dev.galasa.ICredentials;

public interface ICredentialsStore {

    ICredentials getCredentials(String credsId) throws CredentialsException;

    void setCredentials(String credsId, ICredentials credentials) throws CredentialsException;

    void deleteCredentials(String credsId) throws CredentialsException;

    void shutdown() throws CredentialsException;
}
