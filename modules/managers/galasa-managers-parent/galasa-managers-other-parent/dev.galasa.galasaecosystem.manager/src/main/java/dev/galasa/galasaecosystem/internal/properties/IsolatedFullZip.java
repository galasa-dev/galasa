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
 * Isolated Full zip location
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.isolated.full.zip
 * 
 * @galasa.description The location of the isolated zip for the full distribution
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values Valid URL
 * 
 * @galasa.examples 
 * <code>galasaecosystem.isolated.full.zip=http://cicsk8sm.hursley.ibm.com:31210/galasa-isolated-full-maven-repo.zip</code>
 * 
 */
public class IsolatedFullZip extends CpsProperties {

    public static URL get() throws GalasaEcosystemManagerException {
        try {
            String url = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "isolated", "full.zip") ;
            if (url == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.isolated.full.zip is missing");
            }
            return new URL(url);
        } catch (ConfigurationPropertyStoreException | MalformedURLException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the isolated full zip url", e);
        }
    }
}