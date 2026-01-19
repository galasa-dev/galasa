/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;

import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

import java.net.URI;
import java.util.*;

public class MockFrameworkInitialisation implements IFrameworkInitialisation {
    
    protected URI authStoreUri;
    protected URI cpsBootstrapUri;
    protected URI dssUri;

    private List<IAuthStore> registeredAuthStores = new ArrayList<IAuthStore>();
    private List<IConfigurationPropertyStore> registeredConfigPropertyStores = new ArrayList<IConfigurationPropertyStore>();
    private List<IEventsService> registeredEventsServices = new ArrayList<IEventsService>();
    private List<IDynamicStatusStore> registeredDynamicStatusStores = new ArrayList<IDynamicStatusStore>();

    public MockFrameworkInitialisation() {}

    public MockFrameworkInitialisation(URI cpsBootstrapUri) {
        this.cpsBootstrapUri = cpsBootstrapUri;
    }

    public MockFrameworkInitialisation(URI cpsBootstrapUri, URI dssUri) {
        this.cpsBootstrapUri = cpsBootstrapUri;
        this.dssUri = dssUri;
    }

    public void setAuthStoreUri(URI authStoreUri) {
        this.authStoreUri = authStoreUri;
    }

    @Override
    public @NotNull URI getAuthStoreUri() {
        return authStoreUri;
    }

    @Override
    public @NotNull URI getBootstrapConfigurationPropertyStore() {
        return cpsBootstrapUri;
    }

    @Override
    public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore) {
        registeredConfigPropertyStores.add(configurationPropertyStore);
    }

    public List<IConfigurationPropertyStore> getRegisteredConfigurationPropertyStores() {
        return registeredConfigPropertyStores;
    }

    @Override
    public void registerAuthStore(@NotNull IAuthStore authStore) throws AuthStoreException {
        registeredAuthStores.add(authStore);
    }

    public List<IAuthStore> getRegisteredAuthStores() {
        return registeredAuthStores;
    }

    @Override
    public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
            throws DynamicStatusStoreException {
        registeredDynamicStatusStores.add(dynamicStatusStore);
    }

    @Override
    public URI getDynamicStatusStoreUri() {
        return dssUri;
    }

    @Override
    public @NotNull IFramework getFramework() {
        return new MockFramework();
    }

    @Override
    public void registerEventsService(@NotNull IEventsService eventsService) throws EventsException {
        registeredEventsServices.add(eventsService);
    }

    public List<IEventsService> getRegisteredEventsServices() {
        return registeredEventsServices;
    }

    // UNIMPLEMENTED METHODS BELOW

    @Override
    public URI getCredentialsStoreUri() {
        throw new UnsupportedOperationException("Unimplemented method 'getCredentialsStoreUri'");
    }

    @Override
    public @NotNull List<URI> getResultArchiveStoreUris() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultArchiveStoreUris'");
    }

    @Override
    public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'registerResultArchiveStoreService'");
    }

    @Override
    public void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
            throws CertificateStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'registerCertificateStoreService'");
    }

    @Override
    public void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException {
        throw new UnsupportedOperationException("Unimplemented method 'registerConfidentialTextService'");
    }

    @Override
    public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {
        throw new UnsupportedOperationException("Unimplemented method 'registerCredentialsStore'");
    }

    @Override
    public void initialiseAuthStore(Log logger, Properties overrideProperties) throws FrameworkException {
        throw new UnsupportedOperationException("Unimplemented method 'initialiseAuthStore'");
    }
}
