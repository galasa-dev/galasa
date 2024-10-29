/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Should Create Userid
 */
public class CreateUserid extends CpsProperties {
    
    public static boolean get() throws ZosSecurityManagerException {
    	return Boolean.parseBoolean(getStringWithDefault(ZosSecurityPropertiesSingleton.cps(), "false", "create", "userid"));
    }

}
