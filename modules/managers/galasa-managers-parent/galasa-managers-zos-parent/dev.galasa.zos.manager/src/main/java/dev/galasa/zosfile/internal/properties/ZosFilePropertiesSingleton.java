/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.zosfile.ZosFileManagerException;

@Component(service=ZosFilePropertiesSingleton.class, immediate=true)
public class ZosFilePropertiesSingleton {
    
    private static ZosFilePropertiesSingleton singletonInstance;
    private static void setInstance(ZosFilePropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws ZosFileManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new ZosFileManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws ZosFileManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new ZosFileManagerException("Attempt to set manager CPS before instance created");
    }
}
