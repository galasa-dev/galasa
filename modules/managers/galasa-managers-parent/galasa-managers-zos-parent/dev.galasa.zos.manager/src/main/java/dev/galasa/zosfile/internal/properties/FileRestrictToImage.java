/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * zOS File restrict processing to the server on the specified image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosfile.file.[imageid].restrict.to.image
 * 
 * @galasa.description Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the zOS data set or file
 * 
 * @galasa.required No
 * 
 * @galasa.default False
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosfile.file.restrict.to.image=true</code><br>
 * <cods>zosfile.file.SYSA.restrict.to.image=true</code>
 *
 */
public class FileRestrictToImage extends CpsProperties {

    private static final boolean RESTRICT_TO_IMAGE = false;

    public static boolean get(String imageId) throws ZosFileManagerException {
        try {
            String restrictString = getStringNulled(ZosFilePropertiesSingleton.cps(), "file", "restrict.to.image", imageId);

            if (restrictString == null) {
                return RESTRICT_TO_IMAGE;
            } else {
                return Boolean.parseBoolean(restrictString);
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosFileManagerException("Problem asking the CPS for the file restrict to image property for zOS image "  + imageId, e);
        }
    }

}
