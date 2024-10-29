/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.*;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

public class MockCredentialsStore implements ICredentialsStore {


    Map<String,ICredentials> creds ;

    public MockCredentialsStore(Map<String,ICredentials> creds) {
        this.creds = creds ;
    }

    @Override
    public ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException {
        return this.creds.get(credentialsId);
    }

    @Override
    public void shutdown() throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'setCredentials'");
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteCredentials'");
    }
}
