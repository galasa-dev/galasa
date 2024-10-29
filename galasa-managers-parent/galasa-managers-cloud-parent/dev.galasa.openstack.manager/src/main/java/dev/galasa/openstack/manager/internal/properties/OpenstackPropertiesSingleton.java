/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.openstack.manager.OpenstackManagerException;

@Component(service = OpenstackPropertiesSingleton.class, immediate = true)
public class OpenstackPropertiesSingleton {

    private static OpenstackPropertiesSingleton INSTANCE;

    private IConfigurationPropertyStoreService  cps;

    @Activate
    public void activate() {
        INSTANCE = this;
    }

    @Deactivate
    public void deacivate() {
        INSTANCE = null;
    }

    public static IConfigurationPropertyStoreService cps() throws OpenstackManagerException {
        if (INSTANCE != null) {
            return INSTANCE.cps;
        }

        throw new OpenstackManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws OpenstackManagerException {
        if (INSTANCE != null) {
            INSTANCE.cps = cps;
            return;
        }

        throw new OpenstackManagerException("Attempt to set manager CPS before instance created");
    }
}
