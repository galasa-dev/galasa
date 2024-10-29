/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = Db2PropertiesSingleton.class, immediate = true)
public class Db2PropertiesSingleton {
	private static Db2PropertiesSingleton singletonInstance;
    private static void setInstance(Db2PropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws Db2ManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new Db2ManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws Db2ManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new Db2ManagerException("Attempt to set manager CPS before instance created");
	}
}
