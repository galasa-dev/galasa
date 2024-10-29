/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;

@Component(service = GalasaEcosystemPropertiesSingleton.class, immediate = true)
public class GalasaEcosystemPropertiesSingleton {

    private static GalasaEcosystemPropertiesSingleton singletonInstance;
    private static void setInstance(GalasaEcosystemPropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws GalasaEcosystemManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new GalasaEcosystemManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws GalasaEcosystemManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new GalasaEcosystemManagerException("Attempt to set manager CPS before instance created");
	}
}