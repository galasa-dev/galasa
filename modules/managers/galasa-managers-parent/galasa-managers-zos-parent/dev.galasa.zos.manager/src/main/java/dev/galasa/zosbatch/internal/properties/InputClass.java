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
 * zOS Batch default input class
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.default.[imageid].input.class
 * 
 * @galasa.description The default input class to set on the job card for submitted jobs
 * 
 * @galasa.required No
 * 
 * @galasa.default A
 * 
 * @galasa.valid_values a valid JES input class literal
 * 
 * @galasa.examples 
 * <code>zosbatch.default.MVSA.input.class=S</code><br>
 * <code>zosbatch.default.input.class=A</code>
 * 
 */
public class InputClass extends CpsProperties {
    
    private static final String DEFAULT_CLASS = "A";
    
    public static String get(IZosImage image) throws ZosBatchManagerException {
        try {
            String inputClass = getStringNulled(ZosBatchPropertiesSingleton.cps(), "default", "input.class", image.getImageID());

            if (inputClass == null) {
                return DEFAULT_CLASS;
            } 
            
            inputClass = inputClass.toUpperCase();
            if (inputClass.length() == 0 || inputClass.length() > 8 || !(inputClass.matches("^[A-Z0-9]*$"))) {
                throw new ZosBatchManagerException("Input class value must be between 1 and 8 characters in the range [A-Z0-9]");
            }
            
            return inputClass;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the zOSMF default input class for zOS image "  + image.getImageID(), e);
        }
    }

}
