/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=ImstmPropertiesSingleton.class, immediate=true)
public class ImstmPropertiesSingleton {
    
    private static ImstmPropertiesSingleton singletonInstance;

    private static void setInstance(ImstmPropertiesSingleton instance) {
        singletonInstance = instance;
    }
    
    private IConfigurationPropertyStoreService cps;
    
    @Activate
    public void activate() {
        setInstance(this);
    }
    
    @Deactivate
    public void deactivate() {
        setInstance(null);
    }
    
    public static IConfigurationPropertyStoreService getCps() throws ImstmManagerException {
        if (singletonInstance == null) {
            throw new ImstmManagerException("Attempt to access manager CPS before it has been initialised");
        }
        
        return singletonInstance.cps;
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws ImstmManagerException {
        try {
            singletonInstance.cps = cps;
        } catch (NullPointerException e) {
            throw new ImstmManagerException("Attempt to set manager CPS before instance created");
        }

        return;
    }
}
