/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Server port
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[serverid].port
 * 
 * @galasa.description The port number of the zOS/MF server
 * 
 * @galasa.required No
 * 
 * @galasa.default 443
 * 
 * @galasa.valid_values A valid IP port number 
 * 
 * @galasa.examples 
 * <code>zosmf.server.port=443</code><br>
 * <code>zosmf.server.MFSYSA.port=443</code>
 *
 */
public class ServerPort extends CpsProperties {

    public static int get(@NotNull String serverId) throws ZosmfManagerException {
        String serverPort = getStringWithDefault(ZosmfPropertiesSingleton.cps(), "443", "server", "port", serverId);

        try {
            int serverPortInt = Integer.parseInt(serverPort);
            if (serverPortInt < 0 || serverPortInt > 65535) {
                throw new ZosmfManagerException("Invalid value '" + serverPort + "' for zOSMF server port property for zOS server "  + serverId + ". Range  0-65535");
            }
            return serverPortInt;
        } catch(NumberFormatException e) {
            throw new ZosmfManagerException("Invalid value '" + serverPort + "' for zOSMF server port property for zOS server "  + serverId + ". Range  0-65535",e);
        }
    }

}
