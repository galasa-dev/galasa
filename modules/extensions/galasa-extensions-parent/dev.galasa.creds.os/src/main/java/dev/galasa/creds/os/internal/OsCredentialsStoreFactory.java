/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

/**
 * Factory for creating OS-specific credentials stores.
 * This allows for dependency injection in tests.
 */
public class OsCredentialsStoreFactory {

    /**
     * Creates an OS credentials store for the specified operating system.
     * 
     * @param os the operating system
     * @return the credentials store
     * @throws OsCredentialsException if the store cannot be created
     */
    public OsCredentialsStore createStore(OperatingSystem os) throws OsCredentialsException {
        return new OsCredentialsStore(os);
    }
}
