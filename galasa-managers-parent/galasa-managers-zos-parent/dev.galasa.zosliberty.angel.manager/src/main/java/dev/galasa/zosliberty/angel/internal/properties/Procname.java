/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;

/**
 * The Liberty angel process JCL procedure name on a zOS Image
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosliberty.angel.[image].procname
 * 
 * @galasa.description Provides a value for the angel process JCL procedure name on a zOS Image
 * 
 * @galasa.required No
 * 
 * @galasa.default BBGZANGL
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosliberty.angel.[image].procname=BBGZANGL</code><br>
 *
 */
public class Procname extends CpsProperties {

    public static String get(IZosImage image) throws ZosLibertyAngelManagerException {
            return getStringWithDefault(ZosLibertyAngelPropertiesSingleton.cps(), "BBGZANGL", "angel", "procname", image.getImageID());
    }
}
