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
 * @galasa.name zos.image.[image].zosconnect.install.dir
 * 
 * @galasa.description Provides the value of the zOS Connect Install Directory for a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.image.[image].zosconnect.install.dir=/usr/lpp/IBM/zosconnect/v3r0/runtime/</code><br>
 *
 */
public class ZosConnectInstallDir extends CpsProperties {
    
    public static String get(IZosImage image) throws ZosManagerException {
        String imageId = image.getImageID();
        try {
            return getStringNulled(ZosPropertiesSingleton.cps(), "image", "zosconnect.install.dir", imageId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS Connect Install Directory for image " + imageId, e);
        }
    }

}
