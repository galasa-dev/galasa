/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.Zos3270ManagerException;

/**
 * The 3270 device types to connect with
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * zos3270.image.xxxxxx.device.types=IBM-DYNAMIC,IBM-3278-2
 * </p>
 * <p>
 * default is IBM-DYNAMIC,IBM-3278-2
 * </p>
 * 
 *  
 *
 */
public class TerminalDeviceTypes extends CpsProperties {

    public static List<String> get(IZosImage image) throws Zos3270ManagerException {
        return getStringListWithDefault(Zos3270PropertiesSingleton.cps(), "IBM-DYNAMIC,IBM-3278-2", "image", "device.types", image.getImageID());
    }

}
