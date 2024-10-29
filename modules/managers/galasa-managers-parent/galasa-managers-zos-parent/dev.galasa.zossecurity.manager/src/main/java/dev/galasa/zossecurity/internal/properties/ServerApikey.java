/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Server Apikey
 */
public class ServerApikey extends CpsProperties {
    
    public static String get() throws ZosSecurityManagerException {
        try {
            String value = getStringNulled(ZosSecurityPropertiesSingleton.cps(), "server", "apikey");
            if (value == null) {
                throw new ZosSecurityManagerException("Missing property for the zOS Security Server Apikey");
            }
            return value;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Server Apikey", e);
        }
    }

}
