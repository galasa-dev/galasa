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
 * zOSMF Server retry request
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.server.[SERVERID].request.retry
 * 
 * @galasa.description The number of times to retry when zOSMF request fails
 * 
 * @galasa.required No
 * 
 * @galasa.default 3
 * 
 * @galasa.valid_values numerical value > 0 
 * 
 * @galasa.examples 
 * <code>zosmf.server.request.retry=5</code><br>
 * <code>zosmf.server.MFSYSA.request.retry=5</code>
 *
 */
public class RequestRetry extends CpsProperties {

    private static final String DEFAULT_REQUEST_RETRY = "3";

    public static int get(@NotNull String serverId) throws ZosmfManagerException {
        String retryString = getStringWithDefault(ZosmfPropertiesSingleton.cps(), DEFAULT_REQUEST_RETRY, "command", "request.retry", serverId);
        try {
            return Integer.parseInt(retryString);
        } catch(NumberFormatException e) {
            throw new ZosmfManagerException("Invalid value given for zosmf.*.request.retry '" + retryString + "'", e);
        }
    }

}
