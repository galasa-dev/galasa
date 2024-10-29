/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

public class Phoenix2DefaultTestingEnvironment extends CpsProperties {
    public static String get() throws Phoenix2ManagerException{
        return getStringWithDefault(Phoenix2PropertiesSingleton.cps(),"NOT_ASSIGNED", "default", "testing.environment");
    }
}
