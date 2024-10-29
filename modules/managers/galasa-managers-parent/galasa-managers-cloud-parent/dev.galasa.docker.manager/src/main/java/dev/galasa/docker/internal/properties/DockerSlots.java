/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerEngineImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Engine Maximum Slots CPS Property
 * 
 * @galasa.name docker.engine.max.slots
 * 
 * @galasa.description The maximum number of containers(slots) that run on 
 * 
 * @galasa.required No
 * 
 * @galasa.default 3
 * 
 * @galasa.valid_values A valid Java integer value
 * 
 * @galasa.examples 
 * <code>docker.engine.max.value=47</code>
 * 
 * @galasa.extra
 * This property indicates what the maximum number of containers Galasa can start on the Docker Engine.  
 * In Galasa terms, a container is a "slot" a platform independent term to reserve resources.
 * <br>
 * If the value is less than one, it effectively stop new containers being started on the Docker Engine, a way for draining 
 * the Docker Engine for maintenance without stopping the entire Galasa automation system. 
 * 
 */
public class DockerSlots extends CpsProperties {
    public static String get(DockerEngineImpl dockerEngine) throws DockerManagerException {
		try {
			String maxSlots = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "max.slots", dockerEngine.getEngineId());

			if (maxSlots == null) {
				throw new DockerManagerException("Value for Docker Engine max slots not configured for the docker engine: "  + dockerEngine);
			}
			return maxSlots;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the max slots for the docker engine: "  + dockerEngine, e);
		}
	}

}