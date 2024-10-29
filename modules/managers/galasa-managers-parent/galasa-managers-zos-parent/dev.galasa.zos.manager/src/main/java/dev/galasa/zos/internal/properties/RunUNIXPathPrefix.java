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
 * The run data UNIX path prefix for the zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zos.run.[image].unix.path.prefix
 * 
 * @galasa.description The UNIX path prefix for temporary data sets created on zOS Image.<br> 
 * If CPS property zos.run.[image].unix.path.prefix exists, then that is returned
 * 
 * @galasa.required No
 * 
 * @galasa.default /u/runuser/Galasa
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zos.run.[image].unix.path.prefix=/u/userid/Galasa</code><br>
 *
 */
public class RunUNIXPathPrefix extends CpsProperties {
    
    public static String get(@NotNull IZosImage image) throws ZosManagerException {
        String imageId = image.getImageID();
        try {
            String runUNIXPathePrefix = getStringNulled(ZosPropertiesSingleton.cps(), "run", "unix.path.prefix", imageId);
            if (runUNIXPathePrefix == null) {
                ICredentials creds = image.getDefaultCredentials();                
                if (!(creds instanceof ICredentialsUsernamePassword)) {
                    throw new ZosManagerException("Unable to get the run username for image "  + imageId);
                }
                return "/u/" + ((ICredentialsUsernamePassword) creds).getUsername().toLowerCase() + "/Galasa";
            }
            return runUNIXPathePrefix;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosManagerException("Problem asking the CPS for the zOS run UNIX path prefix for image "  + imageId, e);
        }
    }

}
