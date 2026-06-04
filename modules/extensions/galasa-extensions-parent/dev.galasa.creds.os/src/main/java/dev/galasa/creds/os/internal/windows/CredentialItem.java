/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

/**
 * Data class representing a credential item retrieved from Windows Credential Manager.
 * Contains the username and password/secret data.
 */
public class CredentialItem {
    private final String username;
    private final String password;

    /**
     * Creates a new credential item.
     * 
     * @param username the username from the credential
     * @param password the password/secret from the credential
     */
    public CredentialItem(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username from the credential.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password/secret from the credential.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
