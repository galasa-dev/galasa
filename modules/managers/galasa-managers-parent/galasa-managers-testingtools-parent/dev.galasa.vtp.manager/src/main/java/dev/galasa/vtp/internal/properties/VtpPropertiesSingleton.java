/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.vtp.manager.VtpManagerException;

@Component(service = VtpPropertiesSingleton.class, immediate = true)
public class VtpPropertiesSingleton {

    private static VtpPropertiesSingleton instance;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        instance = this; //NOSONAR
    }

    @Deactivate
    public void deactivate() {
        instance = null; //NOSONAR
    }

    public static IConfigurationPropertyStoreService cps() throws VtpManagerException {
        if (instance != null) {
            return instance.cps;
        }

        throw new VtpManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws VtpManagerException {
        if (instance != null) {
            instance.cps = cps;
            return;
        }

        throw new VtpManagerException("Attempt to set manager CPS before instance created");
    }
}
