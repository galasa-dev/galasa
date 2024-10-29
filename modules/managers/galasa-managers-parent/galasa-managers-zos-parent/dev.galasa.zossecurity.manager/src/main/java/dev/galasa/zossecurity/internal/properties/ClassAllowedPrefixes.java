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
 * zOS Security Class Allowed Prefixes
 */
public class ClassAllowedPrefixes extends CpsProperties {
    
    public static List<String> get(String className) throws ZosSecurityManagerException {
        try {
            return getStringList(ZosSecurityPropertiesSingleton.cps(), "allowed", "prefixes", className);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Class Allowed Prefixes for class " + className, e);
        }
    }
}
