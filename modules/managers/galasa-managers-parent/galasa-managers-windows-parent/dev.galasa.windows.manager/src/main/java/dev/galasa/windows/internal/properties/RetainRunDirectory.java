/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.WindowsManagerException;

/**
 * Retain the run directory after the test is complete, for diagnostic purposes
 * 
 * @galasa.cps.property
 * 
 * @galasa.name windows.image.[imageid].retain.run.directory
 * 
 * @galasa.description Informs the Windows Manager that you would like the retain the run directory after the test run is complete
 * 
 * @galasa.required No
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true or false
 * 
 * @galasa.examples 
 * <code>windows.image.UBT.retain.run.directory=true</code>
 * 
 */
public class RetainRunDirectory extends CpsProperties {

    public static boolean get(IWindowsImage image) throws WindowsManagerException {
        
        try {
            return Boolean.parseBoolean(getStringNulled(WindowsPropertiesSingleton.cps(), "image", "retain.run.directory", image.getImageID()));
        } catch (ConfigurationPropertyStoreException e) {
            throw new WindowsManagerException("Problem retrieving the retain run directory property", e);
        }
    }
}