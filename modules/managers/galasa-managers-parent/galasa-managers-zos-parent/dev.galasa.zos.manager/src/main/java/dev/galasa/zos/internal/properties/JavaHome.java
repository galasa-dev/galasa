/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

/**
 * The value of Java home for the zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[image].javahome
 * 
 * @galasa.description Provides the Java home value for a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.image.[image].javahome=/usr/lpp/java/java/J8.0_64/</code><br>
 *
 */
public class JavaHome extends CpsProperties {
    
    public static String get(IZosImage image) throws ZosManagerException {
        String imageId = image.getImageID();
        try {
            return getStringNulled(ZosPropertiesSingleton.cps(), "image", "javahome", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS run Java home for image "  + imageId, e);
        }
    }

}
