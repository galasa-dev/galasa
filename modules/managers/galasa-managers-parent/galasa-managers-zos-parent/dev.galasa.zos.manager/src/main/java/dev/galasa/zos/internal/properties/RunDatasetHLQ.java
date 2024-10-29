/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;

/**
 * The run data set HLQ for the zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.run.[image].dataset.hlq
 * 
 * @galasa.description The data set HLQ(s) for temporary data sets created on zOS Image.<br> 
 * If CPS property zos.run.[image].dataset.hlq exists, then that is returned
 * 
 * @galasa.required No
 * 
 * @galasa.default runuser.GALASA
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.run.[image].dataset.hlq=USERID.GALASA</code><br>
 *
 */
public class RunDatasetHLQ extends CpsProperties {
    
    public static String get(@NotNull IZosImage image) throws ZosManagerException {
        String imageId = image.getImageID();
        try {
            String runDatasetHLQ = getStringNulled(ZosPropertiesSingleton.cps(), "run." + imageId, "dataset.hlq");
            if (runDatasetHLQ == null) {
                ICredentials creds = image.getDefaultCredentials();                
                if (!(creds instanceof ICredentialsUsernamePassword)) {
                    throw new ZosManagerException("Unable to get the run username for image "  + imageId);
                }
                return ((ICredentialsUsernamePassword) creds).getUsername() + ".GALASA".toUpperCase();
            }
            return runDatasetHLQ.toUpperCase();
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS run data set HLQ for image "  + imageId, e);
        }
    }

}
