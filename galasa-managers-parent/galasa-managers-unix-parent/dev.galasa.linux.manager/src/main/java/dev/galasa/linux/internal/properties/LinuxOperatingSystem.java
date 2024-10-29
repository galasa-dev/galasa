/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux Operating System
 * <p>
 * What the operating system of the shared linux image is
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.image.GALLNX.operating.system=UBUNTU
 * linux.image.operating.system=UBUNTU
 * </p>
 * <p>
 * There is no default, the property must be provided for the image to be selectable
 * </p>
 * 
 *  
 *
 */
public class LinuxOperatingSystem extends CpsProperties {

    public static String get(String imageName) throws ConfigurationPropertyStoreException, LinuxManagerException {
        return getStringNulled(LinuxPropertiesSingleton.cps(), "image", "operating.system", imageName);
    }

}
