/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.common;

public class RasDetailsQueryParams {
    
    public static final String DETAILS_METHOD_QUERY_PARAM_VALUE = "methods";

    public RasDetailsQueryParams() {
        
    }

    public boolean isMethodDetailsExcluded(String detailParam) {

        boolean isMethodDetailsExcluded = true;
        if(detailParam != null && detailParam.equals(DETAILS_METHOD_QUERY_PARAM_VALUE)) {
            isMethodDetailsExcluded = false;
        }

        return isMethodDetailsExcluded;

    }

}
