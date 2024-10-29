/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

@Component(service = Phoenix2PropertiesSingleton.class, immediate = true)
public class Phoenix2PropertiesSingleton {

    private static Phoenix2PropertiesSingleton  INSTANCE;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws Phoenix2ManagerException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new Phoenix2ManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws Phoenix2ManagerException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new Phoenix2ManagerException("Attempt to set manager CPS before instance created");
    }
}
