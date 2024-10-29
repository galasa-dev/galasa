/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server Credentials
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[SERVERID].credentials
 * 
 * @galasa.description The z/OS credentials to use when accessing the zOS/MF server 
 * 
 * @galasa.required No
 * 
 * @galasa.default None, however the zOS/MF Manager will use the default z/OS image credentials
 * 
 * @galasa.valid_values  Valid credential ID
 * 
 * @galasa.examples 
 * <code>zosmf.server.MFSYSA.credentials=ZOS</code><br>
 *
 */
public class ServerCreds extends CpsProperties {

    public static String get(@NotNull String serverId) throws ZosmfManagerException {
        try {
            return getStringNulled(ZosmfPropertiesSingleton.cps(), serverId, "server", "credentials", serverId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem accessing the CPS when retrieving zOS/MF credentials for server " + serverId, e);
        }
    }

}
