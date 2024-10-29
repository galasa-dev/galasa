/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * <p>
 * IFrameworkInitialisation provides access to the framework routines that
 * should only be called during test run and server initialisation.
 * </p>
 * 
 *  
 *
 */
public interface IFrameworkInitialisation {

    /**
     * Retrieves the Configuration Property Store that was set in the bootstrap
     * 
     * @return {@link java.net.URI}
     */
    @NotNull
    URI getBootstrapConfigurationPropertyStore();

    URI getDynamicStatusStoreUri();

    URI getCredentialsStoreUri();

    /**
     * Retrieves a list of Result Archive URIs that need to be initialised
     * 
     * @return A list of URIs describing the RASs to be activated
     */
    @NotNull
    List<URI> getResultArchiveStoreUris();

    /**
     * <p>
     * Register the active Configuration Property StoreService. This can only be
     * called once per test run or service instance and will be one of the very
     * first things done during initialisation. If a second CPS attempts register
     * itself, {@link ConfigurationPropertyStoreException} will be thrown.
     * </p>
     * 
     * @param configurationPropertyStore - the configuration property store
     *                                          service chosen to be active
     * @throws ConfigurationPropertyStoreException - Only if a 2nd attempt to
     *                                             register a CPS was performed
     */
    void registerConfigurationPropertyStore(@NotNull IConfigurationPropertyStore configurationPropertyStore)
            throws ConfigurationPropertyStoreException;

    void registerDynamicStatusStore(@NotNull IDynamicStatusStore dynamicStatusStore) throws DynamicStatusStoreException;

    /**
     * <p>
     * Register a Result Archive Store Service. Multiple Result Archive stores can
     * be registered per test run or service instance and will be one of the first
     * things done during initialisation.
     * 
     * @param resultArchiveStoreService - the result archive store service to be
     *                                  registered
     * @throws ResultArchiveStoreException If there is a problem registering the
     *                                     service
     */
    void registerResultArchiveStoreService(@NotNull IResultArchiveStoreService resultArchiveStoreService)
            throws ResultArchiveStoreException;
    
    /**
     * <p>
     * Register a Certificate Store Service. 
     * </p>
     * 
     * @param certificateStoreService - the certificate store service to be
     *                                  registered
     * @throws CertificateStoreException If there is a problem registering the
     *                                     service
     */
    void registerCertificateStoreService(@NotNull ICertificateStoreService certificateStoreService)
            throws CertificateStoreException;

    void registerConfidentialTextService(@NotNull IConfidentialTextService confidentialTextService)
            throws ConfidentialTextException;

    void registerCredentialsStore(@NotNull ICredentialsStore credentialsStore) throws CredentialsException;

    void registerEventsService(@NotNull IEventsService eventsService) throws EventsException;

    /**
     * <p>
     * Retrieve the IFramework object. Not all the methods will be valid during the
     * initialisation period. Review the Framework Lifecycle to determine when parts
     * of the Framework is initialised
     * </p>
     * 
     * @return {@link dev.galasa.framework.spi.IFramework}
     */
    @NotNull
    IFramework getFramework();

}
