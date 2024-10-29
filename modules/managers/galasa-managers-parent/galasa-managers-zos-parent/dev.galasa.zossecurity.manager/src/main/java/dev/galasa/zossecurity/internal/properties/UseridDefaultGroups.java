/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.properties;

import java.util.List;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zossecurity.ZosSecurityManagerException;

/**
 * zOS Security Userid Default Groups
 */
public class UseridDefaultGroups extends CpsProperties {
    
    public static List<String> get() throws ZosSecurityManagerException {
        try {
        	return getStringList(ZosSecurityPropertiesSingleton.cps(), "userid", "default.groups");
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Userid default groups", e);
        }
    }
}
