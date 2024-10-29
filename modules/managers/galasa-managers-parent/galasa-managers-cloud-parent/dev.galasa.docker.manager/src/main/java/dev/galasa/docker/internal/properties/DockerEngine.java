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
 * Docker Engine CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.engine.[engineId].hostname
 * 
 * @galasa.description Provides location of the Docker Engine
 * 
 * @galasa.required Yes - the hostname of the Docker Engine must be provided
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid DNS name or IPv4/6 address
 * 
 * @galasa.examples 
 * <code>docker.engine.[engineId].hostname=docker.example.company.org<br>
 * docker.engine.[engineId].hostname=192.168.2.3
 * </code>
 * 
 * @galasa.extra
 * Currently, the Docker Manager supports only a single Docker Engine although it is planned to allow multiple Engines to be configured.<br>
 * To allow local runs to access the local Docker Engine, you must add this property to the CPS and enable the TCP port of your local Docker Engine.<br>
 * If the Docker Engine is not using the default TCP port, you must provide the *docker.engine.port* configuration property in the CPS.
 * */
public class DockerEngine extends CpsProperties {

    public static String get(final DockerEngineImpl dockerEngineImpl) throws DockerManagerException {
		try {
			final String dockerEngine = getStringNulled(DockerPropertiesSingleton.cps(), "engine", "hostname", dockerEngineImpl.getEngineId());

			if (dockerEngine == null) {
				throw new DockerManagerException("Could not find a docker engine in CPS.");
			}
			return dockerEngine;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker engine URI", e);
        }
	}
}