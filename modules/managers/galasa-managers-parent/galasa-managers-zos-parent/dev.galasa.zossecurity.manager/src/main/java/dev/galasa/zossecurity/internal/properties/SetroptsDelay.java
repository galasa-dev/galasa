/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Setropts Delay
 */
public class SetroptsDelay extends CpsProperties {
    
    public static int get() throws ZosSecurityManagerException {
        return getIntWithDefault(ZosSecurityPropertiesSingleton.cps(), 5000, "setropts", "delay");
    }

}
