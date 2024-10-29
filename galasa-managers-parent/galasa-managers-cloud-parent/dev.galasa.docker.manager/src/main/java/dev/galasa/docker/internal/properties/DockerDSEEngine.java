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
 * Docker Engine DSE CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.dse.engine.[engineTag]
 * 
 * @galasa.description A property that allows a image to be tagged, and then selected from a test class
 * 
 * @galasa.required No 
 * 
 * @galasa.default PRIMARY
 * 
 * @galasa.valid_values An ID for the engine, e.g. LOCAL
 * 
 * @galasa.examples 
 * <code>docker.dse.engine.PRIMARY=LOCAL<br>
 * </code>
 * 
 * */
public class DockerDSEEngine extends CpsProperties {

    public static String get(final DockerEngineImpl dockerEngineImpl) throws DockerManagerException {
		try {
            // Check for a DSE defined engine
			final String dockerDseEngine = getStringNulled(DockerPropertiesSingleton.cps(), "dse.engine", dockerEngineImpl.getEngineTag());
			return dockerDseEngine;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the docker dse engine", e);
        }
	}
}