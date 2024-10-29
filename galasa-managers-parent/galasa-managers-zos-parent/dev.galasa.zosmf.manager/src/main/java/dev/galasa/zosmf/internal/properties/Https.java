/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server port is https
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[imageid].https
 * 
 * @galasa.description Use https (SSL) for zOSMF server
 * 
 * @galasa.required No
 * 
 * @galasa.default True
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosmf.server.https=true</code><br>
 * <code>zosmf.server.SYSA.https=true</code>
 *
 */
public class Https extends CpsProperties {
    
    private static final boolean USE_HTTPS = true;
    
    public static boolean get(String imageId) throws ZosmfManagerException {
        try {
            String https = getStringNulled(ZosmfPropertiesSingleton.cps(), "server", "https", imageId);

            if (https == null) {
                return USE_HTTPS;
            }
            return Boolean.valueOf(https);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF server use https property for zOS image "  + imageId, e);
        }
    }

}
