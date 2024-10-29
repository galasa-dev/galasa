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
 * Kubernetes Ecosystem Tag Shared Environment
 * 
 * @galasa.cps.property
 * 
 * @galasa.name galasaecosystem.ecosystem.tag.XXXXXX.shared.environment
 * 
 * @galasa.description Tells the Galasa Ecosystem Manager which Shared Environment is assigned to an Ecosystem Tag 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid Shared Environment
 * 
 * @galasa.examples 
 * <code>galasaecosystem.ecosystem.tag.SHARED.shared.environment=M1</code>
 * 
 */
public class KubernetesEcosystemTagSharedEnvironment extends CpsProperties {

    public static String get(String tag) throws GalasaEcosystemManagerException {
        try {
            return getStringNulled(GalasaEcosystemPropertiesSingleton.cps(), "ecosystem.tag." + tag, "shared.environment") ;
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Problem retrieving the shared environment for ecosystem tag " + tag, e);
        }
    }
}