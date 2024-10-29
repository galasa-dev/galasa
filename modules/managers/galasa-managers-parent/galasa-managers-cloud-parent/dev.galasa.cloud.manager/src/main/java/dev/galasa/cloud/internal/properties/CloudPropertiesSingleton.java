/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = CloudPropertiesSingleton.class, immediate = true)
public class CloudPropertiesSingleton {

    private static CloudPropertiesSingleton singletonInstance;
    private static void setInstance(CloudPropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws CloudManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new CloudManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws CloudManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new CloudManagerException("Attempt to set manager CPS before instance created");
	}
}
