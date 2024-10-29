/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.vtp.manager.VtpManagerException;

/**
 * Should the VTP Manager use the VTP API to control VTP
 * 
 * @galasa.cps.property
 * 
 * @galasa.name vtp.api.enable
 * 
 * @galasa.description Should the VTP manager use the VTP API
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true, false
 * 
 * @galasa.examples 
 * <code>vtp.api.enable=false</code><br>
 *
 */
public class VtpAPI extends CpsProperties {
    
    public static boolean get() throws VtpManagerException {
        try {
            String vtpAPI = getStringNulled(VtpPropertiesSingleton.cps(), "api", "enable");
            if (vtpAPI == null)  {
                return false;
            }
            return Boolean.parseBoolean(vtpAPI);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking CPS for the VTP recording enable status", e); 
        }
    }
}
