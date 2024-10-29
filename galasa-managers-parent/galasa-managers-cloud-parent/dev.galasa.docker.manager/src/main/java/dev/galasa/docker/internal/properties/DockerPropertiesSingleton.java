/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

@Component(service = DockerPropertiesSingleton.class, immediate = true)
public class DockerPropertiesSingleton {

    private static DockerPropertiesSingleton singletonInstance;
    private static void setInstance(DockerPropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws DockerManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new DockerManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws DockerManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new DockerManagerException("Attempt to set manager CPS before instance created");
	}
}