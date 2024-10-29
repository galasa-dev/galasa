/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.kubernetes.KubernetesManagerException;

/**
 * Kubernetes Manager CPS singleton
 * 
 *  
 *
 */
@Component(service = KubernetesPropertiesSingleton.class, immediate = true)
public class KubernetesPropertiesSingleton {

    private static KubernetesPropertiesSingleton singletonInstance;
    private static void setInstance(KubernetesPropertiesSingleton instance){
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
    
    public static IConfigurationPropertyStoreService cps() throws KubernetesManagerException {
		if (singletonInstance != null) {
			return singletonInstance.cps;
		}
		
		throw new KubernetesManagerException("Attempt to access manager CPS before it has been initialised");
	}
	
	public static void setCps(IConfigurationPropertyStoreService cps) throws KubernetesManagerException {
		if (singletonInstance != null) {
			singletonInstance.cps = cps;
			return;
		}
		
		throw new KubernetesManagerException("Attempt to set manager CPS before instance created");
	}
}