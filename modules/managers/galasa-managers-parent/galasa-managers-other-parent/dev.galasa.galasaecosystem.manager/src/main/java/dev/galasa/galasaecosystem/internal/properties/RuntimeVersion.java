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
 * Runtime Artifact Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.runtime.version
 * 
 * @galasa.description The versions of the runtime artifacts to be used with the Ecosystem
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid maven version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.runtime.version=0.4.0</code>
 * 
 */
public class RuntimeVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "runtime", "version") ;
            if (version == null) {
                throw new GalasaEcosystemManagerException("Property galasaecosystem.runtime.version is missing");
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the runtime artifact version", e);
        }
    }
}