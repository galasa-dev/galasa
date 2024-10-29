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
 * Should the VTP manager be enabled
 * 
 * @galasa.cps.property
 * 
 * @galasa.name vtp.recording.enable
 * 
 * @galasa.description Should the VTP manager be activated for this run
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true, false
 * 
 * @galasa.examples 
 * <code>vtp.recording.enable=true</code><br>
 *
 */
public class VtpEnable extends CpsProperties {
    
    public static boolean get() throws VtpManagerException {
        try {
            String vtpEnable = getStringNulled(VtpPropertiesSingleton.cps(), "recording", "enable");
            if (vtpEnable == null)  {
                return false;
            }
            return Boolean.parseBoolean(vtpEnable);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking CPS for the VTP recording enable status", e); 
        }
    }
}
