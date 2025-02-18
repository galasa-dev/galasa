/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import dev.galasa.framework.FileSystem;
import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.GalasaFactory;
import dev.galasa.framework.IFileSystem;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IApiServerInitialisation;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.auth.AuthStoreException;
import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;

/**
 * This is a subclass of the FrameworkInitialisation class responsible for
 * initialising additional stores that are only used by Galasa's API server.
 * These additional stores should not be initialised when starting the core
 * framework in local or hybrid test runs.
 */
public class ApiServerInitialisation extends FrameworkInitialisation implements IApiServerInitialisation {

    private final URI uriAuthStore;
    private Log logger;

    public ApiServerInitialisation(
        Properties bootstrapProperties,
        Properties overrideProperties
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        this(bootstrapProperties, overrideProperties, null, getBundleContext(), new FileSystem(), new SystemEnvironment());
    }

    public ApiServerInitialisation(
        Properties bootstrapProperties,
        Properties overrideProperties,
        Log initLogger,
        BundleContext bundleContext,
        IFileSystem fileSystem, 
        Environment env
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        super(bootstrapProperties, overrideProperties, initLogger, bundleContext, fileSystem, env, GalasaFactory.getInstance().newDefaultInitStrategy());

        if (initLogger == null) {
            logger = LogFactory.getLog(this.getClass());
        } else {
            logger = initLogger;
        }

        this.uriAuthStore = locateAuthStore(logger, overrideProperties);
        initialiseAuthStore(logger, bundleContext);
    }

    private static BundleContext getBundleContext() {
        return FrameworkUtil.getBundle(ApiServerInitialisation.class).getBundleContext();
    }

    @Override
    public URI getAuthStoreUri() {
        return this.uriAuthStore;
    }

    @Override
    public void registerAuthStore(@NotNull IAuthStore authStore) throws AuthStoreException {
        this.framework.setAuthStore(authStore);
    }

    /**
     * Find where the auth store is located, or return null if one has not been set.
     */
    URI locateAuthStore(Log logger, Properties overrideProperties) throws URISyntaxException {
        URI storeUri = null;
        String propUri = overrideProperties.getProperty("framework.auth.store");

        if (propUri != null && !propUri.isEmpty()) {
            logger.debug("Bootstrap property framework.auth.store used to determine Auth Store location");
            storeUri = new URI(propUri);
            logger.debug("Auth Store is " + storeUri.toString());
        }
        return storeUri;
    }

    void initialiseAuthStore(Log logger, BundleContext bundleContext) throws FrameworkException, InvalidSyntaxException {

        logger.trace("Searching for Auth Store providers");
        final ServiceReference<?>[] authStoreServiceReference = bundleContext
                .getAllServiceReferences(IAuthStoreRegistration.class.getName(), null);
        if ((authStoreServiceReference == null) || (authStoreServiceReference.length == 0)) {
            throw new FrameworkException("No Auth Store Services have been found");
        }
        for (final ServiceReference<?> authStoreReference : authStoreServiceReference) {
            final IAuthStoreRegistration authStoreRegistration = (IAuthStoreRegistration) bundleContext
                    .getService(authStoreReference);
            logger.trace("Found Auth Store Provider " + authStoreRegistration.getClass().getName());

            // The registration code calls back to registerAuthStore to set the auth
            // store object in this.framework, so it can be retrieved by the
            // this.framework.getAuthStore() call...
            authStoreRegistration.initialise(this);
        }
        if (this.framework.getAuthStore() == null) {
            throw new FrameworkException("Failed to initialise an Auth Store, unable to continue");
        }
        logger.debug("Selected Auth Store Service is " + this.framework.getAuthStore().getClass().getName());
    }
}