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
 * Docker Engines CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.default.engines
 * 
 * @galasa.description Comma seperated list of availble docker engines
 * 
 * @galasa.required Yes - at least one engine needs to be defined
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values An ID for the engine, e.g. LOCAL
 * 
 * @galasa.examples 
 * <code>docker.default.engines=LOCAL<br>
 * </code>
 * 
 * @galasa.extra
 * Currently, the Docker Manager supports only a single Docker Engine group called "default" although it is planned to allow multiple Engine groups to be configured.<br>
 * */
public class DockerEngines extends CpsProperties {

    public static String get(final DockerEngineImpl dockerEngineImpl) throws DockerManagerException {
		try {
            // Only support "default" engines currently. Will likely be replaced with a dockerEngineIml.getEngineCluster()
			final String dockerEngines = getStringNulled(DockerPropertiesSingleton.cps(), "default", "engines");

			if (dockerEngines == null) {
				throw new DockerManagerException("Could not find docker engines in CPS.");
			}
			return dockerEngines;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker engine URI", e);
        }
	}
}