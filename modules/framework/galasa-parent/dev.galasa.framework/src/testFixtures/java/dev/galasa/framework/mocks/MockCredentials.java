/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.time.Instant;
import java.util.Properties;

import dev.galasa.ICredentials;

public class MockCredentials implements ICredentials {

    @Override
    public Properties toProperties(String credentialsId) {
        throw new UnsupportedOperationException("Unimplemented method 'toProperties'");
    }

    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Unimplemented method 'setDescription'");
    }

    @Override
    public void setLastUpdatedByUser(String username) {
        throw new UnsupportedOperationException("Unimplemented method 'setLastUpdatedByUser'");
    }

    @Override
    public void setLastUpdatedTime(Instant time) {
        throw new UnsupportedOperationException("Unimplemented method 'setLastUpdatedTime'");
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Unimplemented method 'getDescription'");
    }

    @Override
    public String getLastUpdatedByUser() {
        throw new UnsupportedOperationException("Unimplemented method 'getLastUpdatedByUser'");
    }

    @Override
    public Instant getLastUpdatedTime() {
        throw new UnsupportedOperationException("Unimplemented method 'getLastUpdatedTime'");
    }

    @Override
    public Properties getMetadataProperties(String credentialsId) {
        throw new UnsupportedOperationException("Unimplemented method 'getMetadataProperties'");
    }
}
