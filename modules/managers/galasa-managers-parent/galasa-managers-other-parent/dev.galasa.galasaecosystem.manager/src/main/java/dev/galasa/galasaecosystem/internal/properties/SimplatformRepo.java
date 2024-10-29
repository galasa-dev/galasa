/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * SimPlatform Repository URL
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.simplatform.repository
 * 
 * @galasa.description The location of the Maven Repository all simplatform artifacts will be downloaded from
 * 
 * @galasa.required Yes
 * 
 * @galasa.default runtime repo
 * 
 * @galasa.valid_values Value URL
 * 
 * @galasa.examples 
 * <code>galasaecosystem.simplatform.repository=http://development.galasa.dev/main/maven-repo/obr</code>
 * 
 */
public class SimplatformRepo extends CpsProperties {

    public static URL get() throws GalasaEcosystemManagerException {
        try {
            String url = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "simplatform", "repository") ;
            if (url == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.simplatform.repository is missing");
            }
            return new URL(url);
        } catch (ConfigurationPropertyStoreException | MalformedURLException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the simplatform artifact repository", e);
        }
    }
}