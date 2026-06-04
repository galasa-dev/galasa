/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import java.util.HashMap;
import java.util.Map;

import dev.galasa.creds.os.internal.OsCredentialsException;

/**
 * Mock implementation of the Windows Credential Manager for testing.
 * Simulates Windows Credential Manager behavior without requiring actual Windows API calls.
 */
public class MockCredentialManager implements ICredentialManager {

    private final Map<String, CredentialItem> storage = new HashMap<>();
    private boolean shouldFail = false;

    @Override
    public CredentialItem readCredential(String targetName) throws OsCredentialsException {
        if (shouldFail) {
            throw new OsCredentialsException("simulating an error");
        }

        return storage.get(targetName);
    }

    // Test helper methods

    /**
     * Adds a credential to the mock storage.
     *
     * @param targetName the target name of the credential
     * @param username the username
     * @param password the password
     */
    public void addCredential(String targetName, String username, String password) {
        storage.put(targetName, new CredentialItem(username, password));
    }

    /**
     * Removes a credential from the mock storage.
     *
     * @param targetName the target name of the credential
     */
    public void removeCredential(String targetName) {
        storage.remove(targetName);
    }

    /**
     * Checks if a credential exists in the mock storage.
     *
     * @param targetName the target name of the credential
     * @return true if the credential exists
     */
    public boolean hasCredential(String targetName) {
        return storage.containsKey(targetName);
    }

    /**
     * Simulates a "no logon session" error on the next read operation.
     *
     * @param shouldFail true to simulate the error
     */
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }

    /**
     * Clears all stored credentials and resets the error flag.
     */
    public void clear() {
        storage.clear();
        shouldFail = false;
    }
}
