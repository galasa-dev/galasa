/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * An interface to the Windows Credential Manager API operations.
 */
public interface ICredentialManager {

    /**
     * Reads a credential from Windows Credential Manager.
     *
     * @param targetName the target name of the credential to read
     * @return the credential item, or null if not found
     * @throws OsCredentialsException if there's an error reading the credential
     */
    CredentialItem readCredential(String targetName) throws OsCredentialsException;
}
