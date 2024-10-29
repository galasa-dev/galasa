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
 * SimBank tests Artifact Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.simbanktests.version
 * 
 * @galasa.description The versions of the  simbanktests artifacts to be used with the Ecosystem
 * 
 * @galasa.required Yes
 * 
 * @galasa.default Runtime version
 * 
 * @galasa.valid_values A valid maven version literial
 * 
 * @galasa.examples 
 * <code>galasaecosystem.simbanktests.version=0.4.0</code>
 * 
 */
public class SimBankTestsVersion extends CpsProperties {

    public static String get() throws GalasaEcosystemManagerException {
        try {
            String version = getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "simbanktests", "version") ;
            if (version == null) {
                return RuntimeVersion.get();
            }
            return version;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the simbanktests artifact version", e);
        }
    }
}