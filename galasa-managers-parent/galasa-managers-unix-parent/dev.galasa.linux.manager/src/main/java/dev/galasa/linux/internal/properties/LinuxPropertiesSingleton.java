/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.linux.LinuxManagerException;

@Component(service = LinuxPropertiesSingleton.class, immediate = true)
public class LinuxPropertiesSingleton {

    private static LinuxPropertiesSingleton    INSTANCE;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws LinuxManagerException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new LinuxManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws LinuxManagerException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new LinuxManagerException("Attempt to set manager CPS before instance created");
    }
}
