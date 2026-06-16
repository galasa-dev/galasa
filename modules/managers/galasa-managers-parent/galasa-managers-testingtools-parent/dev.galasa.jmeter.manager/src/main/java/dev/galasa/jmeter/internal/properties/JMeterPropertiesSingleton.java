/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.jmeter.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.jmeter.JMeterManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = JMeterPropertiesSingleton.class, immediate = true)
public class JMeterPropertiesSingleton {

    private static JMeterPropertiesSingleton singletonInstance;
    
    private static void setInstance(JMeterPropertiesSingleton instance) {
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
    
    public static IConfigurationPropertyStoreService cps() throws JMeterManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }
        
        throw new JMeterManagerException("Attempt to access manager CPS before it has been initialised");
    }
    
    public static void setCps(IConfigurationPropertyStoreService cps) throws JMeterManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }
        
        throw new JMeterManagerException("Attempt to set manager CPS before instance created");
    }
}
