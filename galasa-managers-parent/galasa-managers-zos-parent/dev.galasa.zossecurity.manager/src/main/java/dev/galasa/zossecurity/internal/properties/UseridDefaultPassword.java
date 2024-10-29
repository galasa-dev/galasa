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
 * zOS Security Default Password
 */
public class UseridDefaultPassword extends CpsProperties {
    
    public static String get() throws ZosSecurityManagerException {
    	String password;
        try {
            password = getStringNulled(ZosSecurityPropertiesSingleton.cps(), "userid", "default.password");
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Default Password", e);
        }
        if (password == null) {
        	throw new ZosSecurityManagerException("A password must always be provided");
        }
        return password;
    }

}
