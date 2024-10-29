/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal.properties;

import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;

/**
 * The Default Timeout value in seconds for CICS resources on a zOS Image. Used during resource enable and disable
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsresource.default.[image].timeout
 * 
 * @galasa.description Provides a value for the default timeout for JVM servers on a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default 120 seconds
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>cicsresource.default.[image].timeout=120</code><br>
 *
 */
public class DefaultResourceTimeout extends CpsProperties {

    public static int get(IZosImage image) throws CicsResourceManagerException {
    	return getIntWithDefault(CicstsResourcePropertiesSingleton.cps(), 120, "default", "timeout", image.getImageID());
    }
}
