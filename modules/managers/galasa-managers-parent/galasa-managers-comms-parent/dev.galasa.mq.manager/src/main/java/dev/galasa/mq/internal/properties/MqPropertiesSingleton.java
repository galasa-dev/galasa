/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.mq.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.mq.MqManagerException;

@Component(service = MqPropertiesSingleton.class, immediate = true)
public class MqPropertiesSingleton {

    private static MqPropertiesSingleton instance;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        instance = this; //NOSONAR
    }

    @Deactivate
    public void deactivate() {
        instance = null; //NOSONAR
    }

    public static IConfigurationPropertyStoreService cps() throws MqManagerException {
        if (instance != null) {
            return instance.cps;
        }

        throw new MqManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws MqManagerException {
        if (instance != null) {
            instance.cps = cps;
            return;
        }

        throw new MqManagerException("Attempt to set manager CPS before instance created");
    }
}
