/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cemt.internal.properties;

import dev.galasa.cicsts.CemtManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;

/**
 * The Default Timeout value in seconds for CEMT resources on a zOS Image. Used during resource enable and disable
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cemt.default.[image].timeout
 * 
 * @galasa.description Provides a value for the default timeout for enable / disabling of resources.
 * 
 * @galasa.required No
 * 
 * @galasa.default 300 seconds
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>cemt.default.[image].timeout=300</code><br>
 *
 */
public class DefaultResourceTimeout extends CpsProperties {

    public static long getInMilliseconds(IZosImage image) throws CemtManagerException {
    	int defaultTimeInSeconds = getIntWithDefault(CemtPropertiesSingleton.cps(), 300, "default", image.getImageID(), "timeout");
        return defaultTimeInSeconds*1000;
    }
}
