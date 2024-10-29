/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.resources;

import dev.galasa.ICredentials;
import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.api.common.ServletError;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

import static dev.galasa.framework.api.common.ServletErrorMessage.*;

import javax.servlet.http.HttpServletResponse;

public class Secret {

    private String secretId;
    private ICredentialsService credentialsService;
    private ICredentials value;

    public Secret(ICredentialsService credentialsService, String secretName) {
        this.secretId = secretName;
        this.credentialsService = credentialsService;
    }

    public boolean existsInCredentialsStore() {
        return value != null;
    }

    public void loadValueFromCredentialsStore() throws InternalServletException {
        try {
            value = credentialsService.getCredentials(secretId);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5079_FAILED_TO_GET_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void setSecretToCredentialsStore(ICredentials newValue) throws InternalServletException {
        try {
            credentialsService.setCredentials(secretId, newValue);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5077_FAILED_TO_SET_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteSecretFromCredentialsStore() throws InternalServletException {
        try {
            credentialsService.deleteCredentials(secretId);
        } catch (CredentialsException e) {
            ServletError error = new ServletError(GAL5078_FAILED_TO_DELETE_SECRET);
            throw new InternalServletException(error, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
