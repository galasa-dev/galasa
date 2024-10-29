/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosbatch.ZosBatchManagerException;

/**
 * zOS Batch restrict processing to the server on the specified image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosbatch.batchjob.[imageid].restrict.to.image
 * 
 * @galasa.description Use only the server (e.g. zOSMF, RSE API, etc) running on the image associated with the zOS Batch job
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>zosbatch.batchjob.MVSA.restrict.to.image=true</code><br>
 * <code>zosbatch.batchjob.default.restrict.to.image=false</code>
 *
 */
public class BatchRestrictToImage extends CpsProperties {

    public static boolean get(String imageId) throws ZosBatchManagerException {
        try {
            String restritToImageString = getStringNulled(ZosBatchPropertiesSingleton.cps(), "batchjob", "restrict.to.image", imageId);
            return Boolean.parseBoolean(restritToImageString);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosBatchManagerException("Problem asking the CPS for the batch job restrict to image property for zOS image "  + imageId, e);
        }
    }

}
