/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * Maximum slots for zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[tag].max.slots
 * 
 * @galasa.description The maximum slots available on a zOS Image for the specified tag
 * 
 * @galasa.required No
 * 
 * @galasa.default 2
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.image.[tag].max.slots=2</code><br>
 *
 */
public class ImageMaxSlots extends CpsProperties {
    
    private static final int DEFAULT_MAX_SLOTS = 2;
    
    public static int get(String imageId) throws ZosManagerException {
        try {
            String slots = getStringNulled(ZosPropertiesSingleton.cps(), "image", "max.slots", imageId);
            if (slots == null)  {
                return DEFAULT_MAX_SLOTS;
            }
            return Integer.parseInt(slots);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS image "  + imageId+ " max slots", e);
        }
    }

}
