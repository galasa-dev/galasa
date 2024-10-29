/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Userid Default Group
 */
public class UseridDefaultGroup extends CpsProperties {
    
    public static String get() throws ZosSecurityManagerException {
        try {
        	String useridDefaultGroup = getStringNulled(ZosSecurityPropertiesSingleton.cps(), "userid", "default.group");
        	if (useridDefaultGroup == null) {
        		throw new ZosSecurityManagerException("Userid default group not supplied");
        	}
        	return useridDefaultGroup;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Userid default group", e);
        }
    }
}
