/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceda.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.CedaManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CedaPropertiesSingleton.class, immediate=true)
public class CedaPropertiesSingleton {
    private static CedaPropertiesSingleton singletonInstance;
    private static void setInstance(CedaPropertiesSingleton instance) {
        singletonInstance = instance;
    }
    
    private IConfigurationPropertyStoreService cps;
    
    @Activate
    public void activate() {
        setInstance(this);
    }
    
    @Deactivate
    public void deacivate() {
        setInstance(null);
    }
    
    public static IConfigurationPropertyStoreService cps() throws CedaManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new CedaManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws CedaManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new CedaManagerException("Attempt to set manager CPS before instance created");
    }
    
}