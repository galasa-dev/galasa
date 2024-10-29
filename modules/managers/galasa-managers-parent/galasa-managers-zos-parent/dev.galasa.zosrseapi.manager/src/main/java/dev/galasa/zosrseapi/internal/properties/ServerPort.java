/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Server port
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[serverid].port
 * 
 * @galasa.description The port number of the RSE API server 
 * 
 * @galasa.required no
 * 
 * @galasa.default 6800
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>rseapi.server.port=6800</code><br>
 * <code>rseapi.server.RSESYSA.port=6800</code>
 *
 */
public class ServerPort extends CpsProperties {

    public static int get(@NotNull String serverId) throws RseapiManagerException {
        String serverPort = getStringWithDefault(RseapiPropertiesSingleton.cps(), "6800", "server", "port", serverId);

        try {
            int serverPortInt = Integer.parseInt(serverPort);
            if (serverPortInt < 0 || serverPortInt > 65535) {
                throw new RseapiManagerException("Invalid value '" + serverPort + "' for RSE API server port property for server "  + serverId + ". Range  0-65535");
            }
            return serverPortInt;
        } catch(NumberFormatException e) {
            throw new RseapiManagerException("Invalid value '" + serverPort + "' for RSE API server port property for server "  + serverId + ". Range  0-65535",e);
        }
    }

}
