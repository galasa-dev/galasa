/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Server Credentials
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[SERVERID].credentials
 * 
 * @galasa.description The z/OS credentials to use when accessing the RSE API server 
 * 
 * @galasa.required No
 * 
 * @galasa.default None, however the RSE API Manager will use the default z/OS image credentials
 * 
 * @galasa.valid_values  Valid credential ID
 * 
 * @galasa.examples 
 * <code>rseapi.server.RSESYSA.credentials=ZOS</code><br>
 *
 */
public class ServerCreds extends CpsProperties {

    public static String get(@NotNull String serverId) throws RseapiManagerException {
        try {
            return getStringNulled(RseapiPropertiesSingleton.cps(), serverId, "server", "credentials", serverId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem accessing the CPS when retrieving RSE API credentials for server " + serverId, e);
        }
    }

}
