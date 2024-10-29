/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.ras.directory;

import java.net.URI;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.IResultArchiveStoreRegistration;
import dev.galasa.framework.spi.ResultArchiveStoreException;

/**
 * A RAS Registration
 *
 *  
 *
 */
@Component(service = { IResultArchiveStoreRegistration.class })
public class DirectoryResultArchiveStoreRegistration implements IResultArchiveStoreRegistration {

    private DirectoryResultArchiveStoreService service;

    private URI                                rasUri;

    /*
     * (non-Javadoc)
     *
     * @see
     * dev.galasa.framework.spi.IResultArchiveStoreService#initialise(dev.galasa.
     * framework .spi.IFrameworkInitialisation)
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws ResultArchiveStoreException {
        IFramework framework = frameworkInitialisation.getFramework();

        // *** See if this RAS is to be activated, will eventually allow multiples of
        // itself
        final List<URI> rasUris = frameworkInitialisation.getResultArchiveStoreUris();
        for (final URI uri : rasUris) {
            if ("file".equals(uri.getScheme())) {
                if (this.rasUri != null && !this.service.isShutdown()) {
                    throw new ResultArchiveStoreException(
                            "The Directory RAS currently does not support multiple instances of itself");
                }
                this.rasUri = uri;
            }
        }

        if (this.rasUri == null) {
            return;
        }

        service = new DirectoryResultArchiveStoreService(framework, this.rasUri);
        frameworkInitialisation.registerResultArchiveStoreService(service);

        return;
    }

}