/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.ZosBatchManagerException;

/**
 * zOS Batch default message level
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.default.[imageid].message.level
 * 
 * @galasa.description The default message level to set on the job card for submitted jobs
 * 
 * @galasa.required No
 * 
 * @galasa.default (1,1)
 * 
 * @galasa.valid_values a valid JES message level
 * 
 * @galasa.examples 
 * <code>zosbatch.default.MVSA.message.level=(1,1)</code><br>
 * <code>zosbatch.default.message.level=(2,0)</code>
 * 
 */
public class MsgLevel extends CpsProperties {
    
    private static final String DEFAULT_LEVEL = "(1,1)";
    
    public static String get(IZosImage image) throws ZosBatchManagerException {
        try {
            String messageLevel = getStringNulled(ZosBatchPropertiesSingleton.cps(), "default", "message.level", image.getImageID());

            if (messageLevel == null) {
                return DEFAULT_LEVEL;
            }  
            
            if (!(messageLevel.matches("[0-3]|(^[\\(]([0-3]?)[,][0-2]?[\\)])"))) {
                throw new ZosBatchManagerException("Message level value invalid. Valid examples: \"(2,1)\", \"0\", \"(,0)\"");
            }
            
            return messageLevel;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF default message level for zOS image "  + image.getImageID(), e);
        }
    }

}
