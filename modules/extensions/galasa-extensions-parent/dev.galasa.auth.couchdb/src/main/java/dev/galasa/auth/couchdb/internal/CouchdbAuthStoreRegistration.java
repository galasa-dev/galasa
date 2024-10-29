/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.auth.couchdb.internal;

import static dev.galasa.extensions.common.Errors.*;

import java.net.*;

import org.osgi.service.component.annotations.Component;

import dev.galasa.extensions.common.api.HttpClientFactory;
import dev.galasa.extensions.common.api.LogFactory;
import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.extensions.common.couchdb.CouchdbValidator;
import dev.galasa.extensions.common.impl.HttpClientFactoryImpl;
import dev.galasa.extensions.common.impl.HttpRequestFactoryImpl;
import dev.galasa.extensions.common.api.HttpRequestFactory;
import dev.galasa.extensions.common.impl.LogFactoryImpl;
import dev.galasa.framework.spi.IApiServerInitialisation;
import dev.galasa.framework.spi.SystemEnvironment;
import dev.galasa.framework.spi.auth.IAuthStoreRegistration;
import dev.galasa.framework.spi.utils.SystemTimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;

@Component(service = { IAuthStoreRegistration.class })
public class CouchdbAuthStoreRegistration implements IAuthStoreRegistration {

    private HttpClientFactory httpClientFactory;
    private HttpRequestFactory httpRequestFactory;
    private LogFactory logFactory;
    private CouchdbValidator couchdbValidator;

    public CouchdbAuthStoreRegistration() {
        this(
            new HttpClientFactoryImpl(),
            new HttpRequestFactoryImpl(CouchdbAuthStore.COUCHDB_AUTH_TYPE, new SystemEnvironment().getenv(CouchdbAuthStore.COUCHDB_AUTH_ENV_VAR)),
            new LogFactoryImpl(),
            new CouchdbAuthStoreValidator()
        );
    }

    public CouchdbAuthStoreRegistration(HttpClientFactory httpClientFactory, HttpRequestFactory httpRequestFactory, LogFactory logFactory, CouchdbValidator couchdbValidator) {
        this.httpClientFactory = httpClientFactory;
        this.httpRequestFactory = httpRequestFactory;
        this.logFactory = logFactory;
        this.couchdbValidator = couchdbValidator;
    }

    /**
     * This method checks that the auth store is a remote URL reference, and if true
     * registers a new couchdb auth store as the only auth store.
     *
     * @param apiServerInitialisation Parameters this extension can use to to
     *                                initialise itself.
     * @throws AuthStoreException if there was a problem accessing the auth store.
     */
    @Override
    public void initialise(IApiServerInitialisation apiServerInitialisation) throws AuthStoreException {

        URI authStoreUri = apiServerInitialisation.getAuthStoreUri();

        if (isUriReferringToThisExtension(authStoreUri)) {
            try {
                apiServerInitialisation.registerAuthStore(
                    new CouchdbAuthStore(
                        authStoreUri,
                        httpClientFactory,
                        httpRequestFactory,
                        logFactory,
                        couchdbValidator,
                        new SystemTimeService()
                    )
                );
            } catch (CouchdbException e) {
                String errorMessage = ERROR_FAILED_TO_INITIALISE_AUTH_STORE.getMessage(e.getMessage());
                throw new AuthStoreException(errorMessage, e);
            }
        }
    }

    /**
     * Checks whether the provided URI to the auth store is a couchdb URI or not.
     *
     * @param uri - URI to the auth store of the form
     *            "couchdb:http://my.couchdb.server:5984"
     * @return true if the URI is a couchdb URI, false otherwise.
     */
    public boolean isUriReferringToThisExtension(URI uri) {
        return CouchdbAuthStore.URL_SCHEME.equals(uri.getScheme());
    }
}
