/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.testharness;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class InMemoryCreds implements ICredentialsService {
    
    public HashMap<String, ICredentials> credentials = new HashMap<>();

    @Override
    public ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException {
        return credentials.get(credentialsId);
    }

    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'setCredentials'");
    }

    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteCredentials'");
    }
}
