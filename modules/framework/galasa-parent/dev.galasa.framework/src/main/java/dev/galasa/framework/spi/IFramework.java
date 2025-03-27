/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.net.URL;
import java.util.Properties;
import java.util.Random;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.rbac.RBACException;
import dev.galasa.framework.spi.rbac.RBACService;
import dev.galasa.framework.spi.streams.IStreamsService;
import dev.galasa.framework.spi.streams.StreamsException;

/**
 * <p>
 * IFramework provides access to the services the Framework specifically
 * controls, although will be provided by other OSGi bundles. Examples being the
 * Configuration Properties Store service, authentication services etc.
 * </p>
 * <p>
 * Access to the IFramework object will be via the initialisation methods of the
 * services and managers
 * </p>
 * <p>
 * There must only be 1 provider of the IFramework service and the Framework
 * bundle will always be started before any other Galasa bundle.
 * </p>
 * 
 *  
 *
 */
public interface IFramework {

    void setFrameworkProperties(Properties overrideProperties);

    boolean isInitialised();

    /**
     * <p>
     * Retrieve the Configuration Property Store service from the framework. This
     * will allow you to access the configuration properties.
     * </p>
     * 
     * <p>
     * The namespace is used to departmentalise the configuration properties to
     * prevent managers from directly accessing another manager's properties.
     * </p>
     * 
     * <p>
     * the namespace can be alphanumeric, but no '.', cannot be an empty string and
     * cannot be null
     * </p>
     * 
     * <p>
     * As an example, the zOS Batch Manager would have a namespace of 'zosbatch'.
     * The zOS Manager with be 'zos'.
     * </p>
     * 
     * @param namespace - The string used to identify the manager/service to the
     *                  configuration store
     * @return A {@link IConfigurationPropertyStore}, cannot be null
     * @throws ConfigurationPropertyStoreException - If an invalid namespace is
     *                                             given
     */
    @NotNull
    IConfigurationPropertyStoreService getConfigurationPropertyService(@NotNull String namespace)
            throws ConfigurationPropertyStoreException;

    /**
     * <p>
     * Retrieve the Dynamic Status Store service from the framework. This will allow
     * you to access the dynamic status store.
     * </p>
     * 
     * <p>
     * The namespace is used to departmentalise the status properties to prevent
     * managers from directly accessing another manager's properties.
     * </p>
     * 
     * <p>
     * the namespace can be alphanumeric, but no '.', cannot be an empty string and
     * cannot be null
     * </p>
     * 
     * <p>
     * As an example, the zOS Batch Manager would have a namespace of 'zosbatch'.
     * The zOS Manager with be 'zos'.
     * </p>
     * 
     * @param namespace - The string used to identify the manager/service to the
     *                  dynamic status store
     * @return The dynamic status store service for the specified namespace
     * @throws DynamicStatusStoreException
     */
    @NotNull
    IDynamicStatusStoreService getDynamicStatusStoreService(@NotNull String namespace)
            throws DynamicStatusStoreException;
    
    /**
     * <p>
     * Retrieve the Certificate Store Service which can be used to retrieve 
     * keystores of certificates or individual certificates from the store.
     * </p>
     * @return A {@link ICertificateStoreService}, cannot be null
     */
    @NotNull
    ICertificateStoreService getCertificateStoreService();

    /**
     * <p>
     * Retrieve the Result Archive Store from the framework.
     * </p>
     * 
     * @return A {@link IResultArchiveStore}, cannot be null
     */
    @NotNull
    IResultArchiveStore getResultArchiveStore();

    /**
     * <p>
     * Retrieve the Auth Store from the framework.
     * </p>
     *
     * @return An {@link IAuthStore}, cannot be null
     */
    @NotNull
    IAuthStore getAuthStore();

    /**
     * Retrieve the Auth Store Service which can be used to retrieve users and
     * tokens from the store.
     *
     * @return An {@link IAuthStoreService}, cannot be null
     */
    @NotNull
    IAuthStoreService getAuthStoreService();

    /**
     * <p>
     * Provide access to the Resource Pooling Service
     * </p>
     * 
     * @return {@link IResourcePoolingService} The Resource Pooling Service
     */
    @NotNull
    IResourcePoolingService getResourcePoolingService();

    /**
     * <p>
     * Provide access to the Confidential Text Service
     * </p>
     * 
     * @return The Confidential Text Service
     */
    @NotNull
    IConfidentialTextService getConfidentialTextService();

    @NotNull
    IEventsService getEventsService();

    @NotNull
    ICredentialsService getCredentialsService() throws CredentialsException;

    @NotNull
    RBACService getRBACService() throws RBACException;

    @NotNull
    IStreamsService getStreamsService() throws StreamsException;

    /**
     * Retrieve the test run name. Will be null for non test runs
     * 
     * @return - The test run name, null if not a test run
     */
    String getTestRunName();

    /**
     * Get a predefined Random object for sharing across all managers and servers
     * 
     * @return a random object
     */
    Random getRandom();

    IFrameworkRuns getFrameworkRuns() throws FrameworkException;

    IRun getTestRun();

    Properties getRecordProperties();

    URL getApiUrl(@NotNull Api api) throws FrameworkException;
    
    /**
     * If this is a shared environment run, return the run type
     * 
     * @return returns the shared environment run type, or null if it is not
     * @throws ConfigurationPropertyStoreException if there is a problem accessing the CPS
     */
    SharedEnvironmentRunType getSharedEnvironmentRunType() throws ConfigurationPropertyStoreException;
}
