/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal.properties;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.sdv.SdvManagerException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * CPS Properties singleton class.
 */
@Component(service = SdvPropertiesSingleton.class, immediate = true)
public class SdvPropertiesSingleton {

    private static SdvPropertiesSingleton instance;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        instance = this; // NOSONAR
    }

    @Deactivate
    public void deactivate() {
        instance = null; // NOSONAR
    }

    /**
     * Returns instance of CPS Properties class.
     */
    public static IConfigurationPropertyStoreService cps() throws SdvManagerException {
        if (instance != null) {
            return instance.cps;
        }

        throw new SdvManagerException(
                "Attempt to access manager CPS before it has been initialised");
    }

    /**
     * Sets instance of CPS Properties class.
     */
    public static void setCps(IConfigurationPropertyStoreService cps) throws SdvManagerException {
        if (instance != null) {
            instance.cps = cps;
            return;
        }

        throw new SdvManagerException("Attempt to set manager CPS before instance created");
    }
}
