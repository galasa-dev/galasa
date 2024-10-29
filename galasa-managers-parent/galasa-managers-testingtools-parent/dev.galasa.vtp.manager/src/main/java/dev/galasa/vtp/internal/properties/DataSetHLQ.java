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
 * HLQ for the created dump dataset for VTP recordings
 * 
 * @galasa.cps.property
 * 
 * @galasa.name vtp.playback.hlq
 * 
 * @galasa.description The HLQ that should be used to create VTP recording files
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values VTP.RECORDINGS
 * 
 * @galasa.examples 
 * <code>vtp.playback.hlq=VTP.RECORDINGS</code><br>
 *
 */
public class DataSetHLQ extends CpsProperties {
    
    public static String get() throws VtpManagerException {
        try {
            return getStringNulled(VtpPropertiesSingleton.cps(), "playback", "hlq");
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking the CPS for the playback HLQ for VTP recording'", e);
        }
    }

}
