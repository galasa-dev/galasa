/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zos3270.Zos3270ManagerException;

@Component(service = Zos3270PropertiesSingleton.class, immediate = true)
public class Zos3270PropertiesSingleton {

    private static Zos3270PropertiesSingleton  instance;

    private IConfigurationPropertyStoreService cps;

    @Activate
    public void activate() {
        setInstance(this);
    }

    @Deactivate
    public void deacivate() {
        setInstance(null);
    }

    public static IConfigurationPropertyStoreService cps() throws Zos3270ManagerException {
        if (instance != null) {
            return instance.cps;
        }

        throw new Zos3270ManagerException("Attempt to access manager CPS before it has been initialised");
    }

    public static void setCps(IConfigurationPropertyStoreService cps) throws Zos3270ManagerException {
        if (instance != null) {
            instance.cps = cps;
            return;
        }

        throw new Zos3270ManagerException("Attempt to set manager CPS before instance created");
    }

    private static synchronized void setInstance(Zos3270PropertiesSingleton newInstance) {
        instance = newInstance;
    }
}
