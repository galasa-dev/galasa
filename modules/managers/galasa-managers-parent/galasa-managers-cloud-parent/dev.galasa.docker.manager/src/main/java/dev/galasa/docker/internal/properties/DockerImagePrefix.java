/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerRegistryImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker Image Prefix CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.registry.ID.image.prefix
 * 
 * @galasa.description Provides a prefix to be applied to all image names, particularly useful if you have a dockerhub proxy.
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid String
 * 
 * @galasa.examples 
 * <code>docker.registry.LOCAL.image.prefix=dockerhub/</code>
 */
public class DockerImagePrefix extends CpsProperties {

    public static String get(DockerRegistryImpl dockerRegistry) throws DockerManagerException {
        String id = dockerRegistry.getId();
        String dockerImagePrefix = "";
        try {
        	dockerImagePrefix = getStringNulled(DockerPropertiesSingleton.cps(), "registry", "image.prefix", id);
            // Default value
            if (dockerImagePrefix == null) {
            	return "";
            }
            return dockerImagePrefix + "/";
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Problem asking the CPS for the docker image prefix", e);
        }
    }
}
