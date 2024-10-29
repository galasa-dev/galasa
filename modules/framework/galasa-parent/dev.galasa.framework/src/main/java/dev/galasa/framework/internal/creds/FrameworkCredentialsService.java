/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.creds;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.creds.ICredentialsStore;
import dev.galasa.ICredentials;
import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsernamePassword;

/**
 * <p>
 * This class is used to drive the registered Credentials Store, and retireve
 * values from the Credentials Store.
 * </p>
 * 
 *  
 *  
 */
public class FrameworkCredentialsService implements ICredentialsService {
    private final ICredentialsStore  credsStore;
    private IConfidentialTextService confTextService;
    private boolean                  registerConfidentialText;

    /**
     * <p>
     * This constructor retrieves the location of stored credentials and registers
     * credentials with the confidentials text store
     * </p>
     * 
     * @param framework  - The framework object
     * @param credsStore - the registered store the the Credentials
     * @throws CredentialsException
     */
    public FrameworkCredentialsService(IFramework framework, ICredentialsStore credsStore) throws CredentialsException {
        this.credsStore = credsStore;
        this.confTextService = framework.getConfidentialTextService();

        try {
            IConfigurationPropertyStoreService cpsService = framework.getConfigurationPropertyService("framework");
            this.registerConfidentialText = Boolean
                    .parseBoolean(cpsService.getProperty("credentials", "auto.register.cts"));
        } catch (Exception e) {
            throw new CredentialsException("Unable to initialise the Credentials Service", e);
        }
    }

    /**
     * <p>
     * A simple method thta checks the provided URI to the CPS is a local file or
     * not.
     * </p>
     * 
     * @param credsId - id used to access the credentials
     * @return - object containing appropriate credentials
     * @throws CredentialsException
     */
    @Override
    public ICredentials getCredentials(@NotNull String credsId) throws CredentialsException {

        ICredentials creds;
        try {
            creds = this.credsStore.getCredentials(credsId);
        } catch (CredentialsException e) {
            throw new CredentialsException("Unable to retrieve credentials for id " + credsId, e);
        }
        if (creds == null) {
            return null;
        }

        if (!this.registerConfidentialText) {
            return creds;
        }

        if (creds instanceof ICredentialsToken) {
            ICredentialsToken token = (ICredentialsToken) creds;
            confTextService.registerText(new String(token.getToken()), "Token for credentials id " + credsId);
            return creds;
        }

        if (creds instanceof ICredentialsUsernamePassword) {
            ICredentialsUsernamePassword up = (ICredentialsUsernamePassword) creds;
            confTextService.registerText(up.getPassword(), "Token for credentials id " + credsId);
            return creds;
        }

        return creds;
    }

    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        credsStore.setCredentials(credentialsId, credentials);
    }

    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        credsStore.deleteCredentials(credentialsId);
    }
}
