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
 * Docker Engine Port CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.engine.port
 * 
 * @galasa.description Provides TCP Port of the Docker Engine
 * 
 * @galasa.required No
 * 
 * @galasa.default 2375
 * 
 * @galasa.valid_values Any valid TCP Port number
 * 
 * @galasa.examples 
 * <code>docker.engine.port=2375</code>
 * 
 * @galasa.extra
 * The Docker Manager communicates with the Docker Engine via TCP. The Docker Engine needs to be 
 * configured to open the TCP port, which is usually 2375. If the port is not the default one, then this property needs to be provided in the CPS.
 * 
 */

public class DockerEnginePort extends CpsProperties {

    public static String get(DockerEngineImpl dockerEngineImpl) throws DockerManagerException {
		try {
			String dockerEngineUri = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "port", dockerEngineImpl.getEngineId());

			if (dockerEngineUri == null) {
				throw new DockerManagerException("Could not find a docker engine port in CPS.");
			}
			return dockerEngineUri;
		} catch (ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker engine port", e);
	    }
    }
}