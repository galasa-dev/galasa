/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosProvisionedImageImpl;

/**
 * IP Host ID of the zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.image.[tag].iphostid
 * 
 * @galasa.description The IP Host ID of the zOS Image for the supplied tag.<br> 
 * If CPS property zos.image.[tag].iphostid exists, then that is returned, otherwise the zOS Image ID is returned
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.image.[tag].iphostid=sysa.ibm.com</code><br>
 *
 */
public class HostId extends CpsProperties {
    
    public static String get(@NotNull ZosProvisionedImageImpl image) throws ZosManagerException {
        String imageId = image.getImageID();
        try {
            String hostid = getStringNulled(ZosPropertiesSingleton.cps(), "image." + imageId, "iphostid");
            if (hostid == null) {
                return imageId.toLowerCase();
            }
            return hostid.toLowerCase();
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS image "  + imageId + " ip host id", e);
        }
    }

}
