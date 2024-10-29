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
 * zOS Batch default MSGCLASS
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.default.[imageid].message.class
 * 
 * @galasa.description The default message class to set on the job card for submitted jobs
 * 
 * @galasa.required No
 * 
 * @galasa.default A
 * 
 * @galasa.valid_values a valid JES message class literal
 * 
 * @galasa.examples 
 * <code>zosbatch.default.MVSA.message.class=S</code><br>
 * <code>zosbatch.default.message.class=A</code>
 * 
 */
public class MsgClass extends CpsProperties {
    
    private static final String DEFAULT_CLASS = "A";
    
    public static String get(IZosImage image) throws ZosBatchManagerException {
        try {
            String msgClass = getStringNulled(ZosBatchPropertiesSingleton.cps(), "default", "message.class", image.getImageID());

            if (msgClass == null) {
                return DEFAULT_CLASS;
            }  
            
            msgClass = msgClass.toUpperCase();
            if (msgClass.length() != 1 || !(msgClass.matches("^[A-Z0-9]*$"))) {
                throw new ZosBatchManagerException("Message class value must be 1 character in the range [A-Z0-9]");
            }
            
            return msgClass;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF default message class for zOS image "  + image.getImageID(), e);
        }
    }

}
