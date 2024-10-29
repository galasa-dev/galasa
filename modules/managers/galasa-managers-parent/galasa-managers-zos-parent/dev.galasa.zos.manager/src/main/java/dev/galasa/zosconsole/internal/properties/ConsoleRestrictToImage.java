/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosconsole.ZosConsoleManagerException;

/**
 * Restrict zOS console processing to the zOSMF server on the specified image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosconsole.console.[imageid].restrict.to.image
 * 
 * @galasa.description Use only the zOSMF server running on the image associated with the zOS Console
 * 
 * @galasa.required No
 * 
 * @galasa.default False
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosconsole.console.restrict.to.image=true</code><br>
 * <code>zosconsole.console.SYSA.restrict.to.image=true</code>
 * 
 */
public class ConsoleRestrictToImage extends CpsProperties {

    public static boolean get(String imageId) throws ZosConsoleManagerException {
        try {
            String restritToImageString = getStringNulled(ZosConsolePropertiesSingleton.cps(), "zosconsole", "restrict.to.image", imageId);
            return Boolean.parseBoolean(restritToImageString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosConsoleManagerException("Problem asking the CPS for the console restrict to image property for zOS image "  + imageId, e);
        }
    }

}
