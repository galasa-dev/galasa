/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.dss;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreRegistration;
import dev.galasa.framework.spi.IFrameworkInitialisation;

/**
 * 
 * 
 *  
 */
@Component(service = { IDynamicStatusStoreRegistration.class })
public class FpfDynamicStatusStoreRegistration implements IDynamicStatusStoreRegistration {

    /**
     * <p>
     * This method registers this as the only DSS Store.
     * </p>
     * 
     * @param frameworkInitialisation
     * @throws DynamicStatusStoreException
     */
    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation)
            throws DynamicStatusStoreException {
        URI dss = frameworkInitialisation.getDynamicStatusStoreUri();
        if (isFileUri(dss)) {
            frameworkInitialisation.registerDynamicStatusStore(new FpfDynamicStatusStore(dss));
        }
    }

    /**
     * <p>
     * A simple method thta checks the provided URI to the CPS is a local file or
     * not.
     * </p>
     * 
     * @param uri - URI to the CPS
     * @return - boolean if File or not.
     */
    public static boolean isFileUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
}
