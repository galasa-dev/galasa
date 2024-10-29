/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Server port is https
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[imageid].https
 * 
 * @galasa.description Use https (SSL) for RSE API server
 * 
 * @galasa.required No
 * 
 * @galasa.default True
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.https=true</code><br>
 * <code>rseapi.server.RSESYSA.https=true</code>
 *
 */
public class Https extends CpsProperties {
    
    private static final boolean USE_HTTPS = true;
    
    public static boolean get(String imageId) throws RseapiManagerException {
        try {
            String https = getStringNulled(RseapiPropertiesSingleton.cps(), "server", "https", imageId);

            if (https == null) {
                return USE_HTTPS;
            }
            return Boolean.valueOf(https);
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API server use https property for zOS image "  + imageId, e);
        }
    }

}
