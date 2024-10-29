/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.CertificateStoreException;
import dev.galasa.framework.spi.ConfidentialTextException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.EventsException;
import dev.galasa.framework.spi.ICertificateStoreService;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStore;
import dev.galasa.framework.spi.IDynamicStatusStore;
import dev.galasa.framework.spi.IEventsService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreService;
import dev.galasa.framework.spi.ResultArchiveStoreException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

public class MockFrameworkInitialisation implements IFrameworkInitialisation {

    private URI cpsUri;
    private URI dssUri;
    private URI credsUri;
    private IFramework framework;

    public MockFrameworkInitialisation(URI cpsUri) {
        this(cpsUri, null, null);
    }

    public MockFrameworkInitialisation(URI cpsUri, URI dssUri, URI credsUri) {
        this(cpsUri, dssUri, credsUri, new MockFramework());
    }

    public MockFrameworkInitialisation(URI cpsUri, URI dssUri, URI credsUri, MockFramework framework) {
        this.cpsUri = cpsUri;
        this.dssUri = dssUri;
        this.credsUri = credsUri;
        this.framework = framework;
    }

    @Override
    public URI getBootstrapConfigurationPropertyStore() {
        return cpsUri;
    }

    @Override
    public URI getDynamicStatusStoreUri() {
        return dssUri;
    }

    @Override
    public URI getCredentialsStoreUri() {
        return credsUri;
    }

    @Override
    public IFramework getFramework() {
        return framework;
    }

    @Override
    public void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
            throws ConfigurationPropertyStoreException {
        // Do nothing...
    }

    @Override
    public void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore)
            throws DynamicStatusStoreException {
        // Do nothing...
    }

    @Override
    public void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException {
        // Do nothing...
    }

    @Override
    public void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
            throws CertificateStoreException {
        // Do nothing...
    }

    @Override
    public void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException {
        // Do nothing...
    }

    @Override
    public void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException {
        // Do nothing...
    }

    @Override
    public void registerEventsService(@NotNull IEventsService eventsService) throws EventsException {
        // Do nothing...
    }

    @Override
    public List<URI> getResultArchiveStoreUris() {
        throw new UnsupportedOperationException("Unimplemented method 'getResultArchiveStoreUris'");
    }
}
