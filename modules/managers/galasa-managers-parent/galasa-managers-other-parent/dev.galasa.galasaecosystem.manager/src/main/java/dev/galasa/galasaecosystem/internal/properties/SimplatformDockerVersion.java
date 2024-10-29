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
 * SimPlatform Docker Image Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.simplatform.docker.version
 * 
 * @galasa.description The versions of the Simplatform Docker images to be used with the Ecosystem
 * 
 * @galasa.required Yes
 * 
 * @galasa.default The setting of galasaecosystem.docker.version
 * 
 * @galasa.valid_values A valid Docker version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.simplatform.docker.version=0.4.0</code>
 * 
 */
public class SimplatformDockerVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "simplatform.docker", "version") ;
            if (version == null) {
                version = DockerVersion.get();
            }
            if (version == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.docker.version is missing");
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the simplatform docker artifact version", e);
        }
    }
}