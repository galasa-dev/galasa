/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * The 3270 client device name to connect to.
 *
 * The property takes the following format:
 * zos3270.image.IMAGEID.device.name=LU1
 *
 * If no device name is given, a null value will be returned.
 */
public class TerminalDeviceName extends CpsProperties {

    public static String get(IZosImage image) throws Zos3270ManagerException {
        try {
            return getStringNulled(Zos3270PropertiesSingleton.cps(), "image", "device.name", image.getImageID());
        } catch (ConfigurationPropertyStoreException e) {
            throw new Zos3270ManagerException("Failed to get a value for the terminal device name from the CPS", e);
        }
    }
}
