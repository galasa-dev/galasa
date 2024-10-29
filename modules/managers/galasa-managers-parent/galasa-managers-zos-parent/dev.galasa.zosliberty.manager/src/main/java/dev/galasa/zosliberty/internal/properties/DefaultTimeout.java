/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.ZosLibertyManagerException;

/**
 * The Default Timeout value in seconds for Liberty servers on a zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosliberty.server.[image].default.timeout
 * 
 * @galasa.description Provides a value for the default timeout for Liberty servers on a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default 20 seconds
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosliberty.server.[image].default.timeout=20</code><br>
 *
 */
public class DefaultTimeout extends CpsProperties {

    public static int get(IZosImage image) throws ZosLibertyManagerException {
            return getIntWithDefault(ZosLibertyPropertiesSingleton.cps(), 20, "server", "default.timeout", image.getImageID());
    }
}
