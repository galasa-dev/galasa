/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = CorePropertiesSingleton.class, immediate = true)
public class CorePropertiesSingleton {

    private static CorePropertiesSingleton singletonInstance;
    private static void setInstance(CorePropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws CoreManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new CoreManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws CoreManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new CoreManagerException("Attempt to set manager CPS before instance created");
	}
}
