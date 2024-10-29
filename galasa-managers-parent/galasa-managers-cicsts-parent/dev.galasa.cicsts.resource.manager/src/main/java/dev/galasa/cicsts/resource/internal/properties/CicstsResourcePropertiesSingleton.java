/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service=CicstsResourcePropertiesSingleton.class, immediate=true)
public class CicstsResourcePropertiesSingleton {
    
    private static CicstsResourcePropertiesSingleton singletonInstance;

    private static void setInstance(CicstsResourcePropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws CicsResourceManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new CicsResourceManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws CicsResourceManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new CicsResourceManagerException("Attempt to set manager CPS before instance created");
    }
}
