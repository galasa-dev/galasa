/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * zOSMF Image Servers 
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosmf.image.IMAGEID.servers
 * 
 * @galasa.description The zOSMF servers for use with z/OS Image, the zOS/MF do not need to be running the actual z/OS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values  Comma separated zOS/MF server IDs
 * 
 * @galasa.examples 
 * <code>zosmf.image.MV2C.servers=MFSYSA,MFSYSB</code><br>
 *
 */
public class ImageServers extends CpsProperties {

    public static @NotNull List<String> get(IZosImage zosImage) throws ZosmfManagerException {
        try {
            List<String> serverImages = getStringList(ZosmfPropertiesSingleton.cps(), "image." + zosImage.getImageID(), "servers");

            return serverImages;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Problem asking the CPS for the zOSMF servers property for zOS image "  + zosImage.getImageID(), e);
        }
    }

}
