/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import java.net.URL;
import java.util.Properties;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import javax.validation.constraints.NotNull;

import dev.galasa.extensions.common.mocks.cps.MockConfigurationPropertyStoreService;
import dev.galasa.framework.spi.Api;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkRuns;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStreamsService;

public class MockFramework implements IFramework {

    @Override
    public void setFrameworkProperties(Properties overrideProperties) {
        throw new UnsupportedOperationException("Unimplemented method 'setFrameworkProperties'");
    }

    @Override
    public boolean isInitialised() {
        throw new UnsupportedOperationException("Unimplemented method 'isInitialised'");
    }

    @Override
    public @NotNull IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException {
        Map<String,String> props = new HashMap<String,String>();
        return new MockConfigurationPropertyStoreService(props);
    }

    @Override
    public @NotNull IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getDynamicStatusStoreService'");
    }

    @Override
    public @NotNull ICertificateStoreService getCertificateStoreService() {
        throw new UnsupportedOperationException("Unimplemented method 'getCertificateStoreService'");
    }

    @Override
    public @NotNull IResultArchiveStore getResultArchiveStore() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultArchiveStore'");
    }

    @Override
    public @NotNull IResourcePoolingService getResourcePoolingService() {
        throw new UnsupportedOperationException("Unimplemented method 'getResourcePoolingService'");
    }

    @Override
    public @NotNull IConfidentialTextService getConfidentialTextService() {
        throw new UnsupportedOperationException("Unimplemented method 'getConfidentialTextService'");
    }

    @Override
    public @NotNull ICredentialsService getCredentialsService() throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'getCredentialsService'");
    }

    @Override
    public String getTestRunName() {
        Random random = new Random();
        int randomNumber = 100 + random.nextInt(900);
        return "C" + randomNumber;
    }

    @Override
    public Random getRandom() {
        throw new UnsupportedOperationException("Unimplemented method 'getRandom'");
    }

    @Override
    public IFrameworkRuns getFrameworkRuns() throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getFrameworkRuns'");
    }

    @Override
    public IRun getTestRun() {
        throw new UnsupportedOperationException("Unimplemented method 'getTestRun'");
    }

    @Override
    public Properties getRecordProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getRecordProperties'");
    }

    @Override
    public URL getApiUrl(@NotNull Api api) throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'getApiUrl'");
    }

    @Override
    public SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getSharedEnvironmentRunType'");
    }

    @Override
    public @NotNull IAuthStoreService getAuthStoreService() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthStoreService'");
    }

    @Override
    public @NotNull IAuthStore getAuthStore() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthStore'");
    }

    @Override
    public @NotNull IEventsService getEventsService() {
        throw new UnsupportedOperationException("Unimplemented method 'getEventsService'");
    }

    @Override
    public @NotNull RBACService getRBACService() throws RBACException {
        throw new UnsupportedOperationException("Unimplemented method 'getRBACService'");
    }

    @Override 
    public @NotNull IStreamsService getStreamsService() {
        throw new UnsupportedOperationException("Unimplemented method 'getStreamsService'");
    }

}