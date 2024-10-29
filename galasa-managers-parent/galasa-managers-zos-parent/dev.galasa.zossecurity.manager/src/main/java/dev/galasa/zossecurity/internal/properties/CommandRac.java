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
 * zOS Security Command Rac
 */
public class CommandRac extends CpsProperties {
    
    public static String get() throws ZosSecurityManagerException {
        try {
            String value = getStringNulled(ZosSecurityPropertiesSingleton.cps(), "command", "rac");
            if (value == null) {
                throw new ZosSecurityManagerException("Missing property for the CPS for the zOS Security Command Rac");
            }
            return value;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosSecurityManagerException("Problem asking the CPS for the zOS Security Command Rac", e);
        }
    }
}
