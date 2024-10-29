/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.extensions.common.couchdb.CouchdbException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.ResultArchiveStoreException;

@Component(service = { IResultArchiveStoreRegistration.class })
public class CouchdbRasRegistration implements IResultArchiveStoreRegistration {

    private IFramework      framework;
    private URI             rasUri;
    private CouchdbRasStore store;

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ResultArchiveStoreException {
        this.framework = frameworkInitialisation.getFramework();

        // *** See if this RAS is to be activated, will eventually allow multiples of
        // itself
        final List<URI> rasUris = frameworkInitialisation.getResultArchiveStoreUris();
        for (final URI uri : rasUris) {
            if ("couchdb".equals(uri.getScheme())) {
                if (this.rasUri != null && !store.isShutdown()) {
                    throw new ResultArchiveStoreException(
                            "The CouchDB RAS currently does not support multiple instances of itself");
                }
                this.rasUri = uri;
            }
        }

        if (this.rasUri == null) {
            return;
        }

        // *** Test we can contact the CouchDB server
        try {
            store = new CouchdbRasStore(framework, this.rasUri);
        } catch (CouchdbException e) {
            throw new ResultArchiveStoreException(e);
        }

        // *** All good, register it
        frameworkInitialisation.registerResultArchiveStoreService(store);
    }

}
