/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.selenium.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.selenium.SeleniumManagerException;

@Component(service = SeleniumPropertiesSingleton.class, immediate = true)
public class SeleniumPropertiesSingleton {

    private static SeleniumPropertiesSingleton instance;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        instance = this; //NOSONAR
    }

    @Deactivate
    public void deacivate() {
        instance = null; //NOSONAR
    }

    public static IConfigurationPropertyStoreService cps() throws SeleniumManagerException {
        if (instance != null) {
            return instance.cps;
        }

        throw new SeleniumManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws SeleniumManagerException {
        if (instance != null) {
            instance.cps = cps;
            return;
        }

        throw new SeleniumManagerException("Attempt to set manager CPS before instance created");
    }
}
