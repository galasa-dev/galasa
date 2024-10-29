/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;

/**
 * The Core Manager provides Tests with access to some of the most common
 * features within the Galasa Framework
 *
 * To gain access to the Core Manager, include the following in the test class:-
 * 
 * <pre>
   &#64;CoreManager
   public ICoreManager coreManager;
   </pre>
 *
 * @see CoreManager
 *
 */
public interface ICoreManager {

    /**
     * Returns the Run Name of the Test Run, unique during the length of this Test
     * Run
     *
     * @return Unique Test Run name
     */
    @NotNull String getRunName();
    
    /**
     * Retrieve Credentials
     * 
     * @param credentialsId
     * @return A credentials object or null if id not found
     * @throws CoreManagerException If there is a problem accessing the credentials store
     */
    ICredentials getCredentials(@NotNull String credentialsId) throws CoreManagerException;
    
    /**
     * Retrieve Username and Password Credentials only
     * 
     * @param  credentialsId
     * @return A credentials object or null if id not found
     * @throws CoreManagerException If there is a problem accessing the credentials store 
     *         or if the credential is not of type ICredentialsUsernamePassword
     */
    ICredentialsUsernamePassword getUsernamePassword(@NotNull String credentialsId) throws CoreManagerException;
    
    void registerConfidentialText(String confidentialString, String comment);
}
