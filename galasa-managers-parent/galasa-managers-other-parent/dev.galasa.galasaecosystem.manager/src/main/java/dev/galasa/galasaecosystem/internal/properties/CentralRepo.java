/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal.properties;

import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;


/**
 * Central Repository URL
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.central.repository
 * 
 * @galasa.description The location of the Maven Central Repository
 * 
 * @galasa.required Yes
 * 
 * @galasa.default https://repo.maven.apache.org/maven2
 * 
 * @galasa.valid_values Value URL
 * 
 * @galasa.examples 
 * <code>galasaecosystem.central.repository=https://repo.maven.apache.org/maven2</code>
 * 
 */
public class CentralRepo extends CpsProperties {

    public static URL get() throws GalasaEcosystemManagerException {
        try {
            String url = getStringWithDefault(GalasaEcosystemPropertiesSingleton.cps(), "https://repo.maven.apache.org/maven2", "central", "repository") ;
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the central artifact repository", e);
        }
    }
}