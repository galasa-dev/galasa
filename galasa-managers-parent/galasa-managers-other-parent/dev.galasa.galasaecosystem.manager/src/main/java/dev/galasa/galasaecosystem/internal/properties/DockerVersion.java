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
 * Docker Image Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.docker.version
 * 
 * @galasa.description The versions of the Docker images to be used with the Ecosystem
 * 
 * @galasa.required Yes
 * 
 * @galasa.default The setting of galasaecosystem.maven.version
 * 
 * @galasa.valid_values A valid Docker version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.docker.version=0.4.0</code>
 * 
 */
public class DockerVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "docker", "version") ;
            if (version == null) {
                version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "maven", "version") ;
            }
            if (version == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.docker.version is missing");
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the docker artifact version", e);
        }
    }
}