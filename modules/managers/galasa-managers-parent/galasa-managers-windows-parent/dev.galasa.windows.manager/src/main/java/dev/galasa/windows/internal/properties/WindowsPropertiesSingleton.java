/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.windows.WindowsManagerException;

@Component(service = WindowsPropertiesSingleton.class, immediate = true)
public class WindowsPropertiesSingleton {

    private static WindowsPropertiesSingleton    INSTANCE;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws WindowsManagerException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new WindowsManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws WindowsManagerException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new WindowsManagerException("Attempt to set manager CPS before instance created");
    }
}
