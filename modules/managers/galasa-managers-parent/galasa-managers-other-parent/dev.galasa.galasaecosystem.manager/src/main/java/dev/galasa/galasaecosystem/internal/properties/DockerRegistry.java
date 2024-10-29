/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * Docker Registry
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.docker.registry
 * 
 * @galasa.description The registry that contains the Docker images 
 * 
 * @galasa.required No
 * 
 * @galasa.default docker.io
 * 
 * @galasa.valid_values a valid hostname
 * 
 * @galasa.examples 
 * <code>galasaecosystem.docker.registry=docker.io</code>
 * 
 */
public class DockerRegistry extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "docker", "registry") ;
            if (version == null) {
                return "docker.io";
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the docker registry", e);
        }
    }
}