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
 * zOS Security Userid Default Groups for Sysplex
 */
public class UseridSysplexGroups extends CpsProperties {
    
    public static List<String> get(String sysplexId) throws ZosSecurityManagerException {
        try {
        	return getStringList(ZosSecurityPropertiesSingleton.cps(), "userid.sysplex", "groups", sysplexId);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Userid default groups for Sysplex " + sysplexId, e);
        }
    }
}
