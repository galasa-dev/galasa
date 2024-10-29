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
 * RSE API Server retry request
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.server.[SERVERID].request.retry
 * 
 * @galasa.description The number of times to retry when RSE API request fails
 * 
 * @galasa.required No
 * 
 * @galasa.default 3
 * 
 * @galasa.valid_values numerical value &gt; 0 
 * 
 * @galasa.examples 
 * <code>rseapi.server.request.retry=5</code><br>
 * <code>rseapi.server.RSESYSA.request.retry=5</code>
 *
 */
public class RequestRetry extends CpsProperties {

    private static final String DEFAULT_REQUEST_RETRY = "3";

    public static int get(@NotNull String serverId) throws RseapiManagerException {
    	String retryString = getStringWithDefault(RseapiPropertiesSingleton.cps(), DEFAULT_REQUEST_RETRY, "command", "request.retry", serverId);
        try {
            return Integer.parseInt(retryString);
        } catch(NumberFormatException e) {
            throw new RseapiManagerException("Invalid value given for rseapi.*.request.retry '" + retryString + "'", e);
        }
    }
}
