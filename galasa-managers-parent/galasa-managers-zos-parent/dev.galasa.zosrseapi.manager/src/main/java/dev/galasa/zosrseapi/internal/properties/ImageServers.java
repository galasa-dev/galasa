/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * RSE API Image Servers 
 * 
 * @galasa.cps.property
 * 
 * @galasa.name rseapi.image.IMAGEID.servers
 * 
 * @galasa.description The RSE API servers for use with z/OS Image, the RSE API do not need to be running the actual z/OS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values  Comma separated RSE API server IDs
 * 
 * @galasa.examples 
 * <code>rseapi.image.MV2C.servers=RSESYSA,RSESYSB</code><br>
 *
 */
public class ImageServers extends CpsProperties {

    public static @NotNull List<String> get(IZosImage zosImage) throws RseapiManagerException {
        try {
            return getStringList(RseapiPropertiesSingleton.cps(), "image." + zosImage.getImageID(), "servers");
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Problem asking the CPS for the RSE API servers property for zOS image "  + zosImage.getImageID(), e);
        }
    }

}
