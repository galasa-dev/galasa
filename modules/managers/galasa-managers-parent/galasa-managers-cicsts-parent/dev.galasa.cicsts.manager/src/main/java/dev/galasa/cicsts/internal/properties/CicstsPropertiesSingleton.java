/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CicstsPropertiesSingleton.class, immediate=true)
public class CicstsPropertiesSingleton {
    
    private static CicstsPropertiesSingleton singletonInstance;

    private static void setInstance(CicstsPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws CicstsManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new CicstsManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws CicstsManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new CicstsManagerException("Attempt to set manager CPS before instance created");
    }
}
