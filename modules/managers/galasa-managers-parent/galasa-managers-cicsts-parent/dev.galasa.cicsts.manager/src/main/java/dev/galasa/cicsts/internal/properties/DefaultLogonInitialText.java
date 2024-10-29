/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultLogonInitialText extends CpsProperties {

    public static String get() throws CicstsManagerException {
        try {
            return getStringNulled(CicstsPropertiesSingleton.cps(), "default.logon", "initial.text");
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the default logon initial text", e); 
        }
    }
}
