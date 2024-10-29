/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxManagerException;

/**
 * Retain the run directory after the test is complete, for diagnostic purposes
 * 
 * @galasa.cps.property
 * 
 * @galasa.name linux.image.[imageid].retain.run.directory
 * 
 * @galasa.description Informs the Linux Manager that you would like the retain the run directory after the test run is complete
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>linux.image.UBT.retain.run.directory=true</code>
 * 
 */
public class RetainRunDirectory extends CpsProperties {

    public static boolean get(ILinuxImage image) throws LinuxManagerException {
        
        try {
            return Boolean.parseBoolean(getStringNulled(LinuxPropertiesSingleton.cps(), "image", "retain.run.directory", image.getImageID()));
        } catch (ConfigurationPropertyStoreException e) {
            throw new LinuxManagerException("Problem retrieving the retain run directory property", e);
        }
    }
}