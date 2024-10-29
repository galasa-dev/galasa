/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;

public interface IRseapiRestApiProcessor {    
    
    /**
     * Send RSE API request
     * @param requestType as defined by {@link RseapiRequestType}
     * @param path the RSE API API path
     * @param headers the required HTTP headers or null
     * @param body the request body or null
     * @param validStatusCodes list HTTP status codes expected from this request. default of HTTP 200 when null 
     * @param convert is a data conversion required. If true, data will be converted between EBCDIC to ISO8859-1. If false, no data conversion will take place.
     * @return the response {@link IRseapiResponse}
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse sendRequest(RseapiRequestType requestType, String path, 
        Map<String, String> headers, Object body, List<Integer> validStatusCodes, 
        boolean convert) throws RseapiException;
}
