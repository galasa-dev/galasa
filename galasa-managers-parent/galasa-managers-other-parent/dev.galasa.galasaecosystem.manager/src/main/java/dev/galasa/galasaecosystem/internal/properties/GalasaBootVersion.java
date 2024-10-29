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
 * Galas Boot Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.galasaboot.version
 * 
 * @galasa.description The version version of the galasa boot maven artifact to be downloaded
 * 
 * @galasa.required Yes
 * 
 * @galasa.default runtime version
 * 
 * @galasa.valid_values A valid maven version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.galasaboot.version=0.4.0</code>
 * 
 */
public class GalasaBootVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "galasaboot", "version") ;
            if (version == null) {
                return RuntimeVersion.get();
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the galasa boot version", e);
        }
    }
}