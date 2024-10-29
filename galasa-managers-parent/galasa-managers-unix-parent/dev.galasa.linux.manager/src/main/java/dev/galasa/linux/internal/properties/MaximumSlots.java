/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.linux.LinuxManagerException;

/**
 * Linux Maximum Slots for an Image
 * <p>
 * This property restricts the maximum slots, ie max number of tests that can run against a linux image
 * </p>
 * <p>
 * The property is:-<br>
 * <br>
 * linux.image.GALLNX01.max.slots=9
 * linux.image.max.slots=9
 * </p>
 * <p>
 * default value is 2 slots
 * </p>
 * 
 *  
 *
 */
public class MaximumSlots extends CpsProperties {

    public static int get(String imageId) throws LinuxManagerException {
        return getIntWithDefault(LinuxPropertiesSingleton.cps(), 2, "image", "max.slots", imageId);
    }

}
