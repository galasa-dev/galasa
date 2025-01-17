/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

public class DefaultLogonInitialText extends CpsProperties {

    public static String get() throws ImstmManagerException {
        try {
            return getStringNulled(ImstmPropertiesSingleton.cps(), "default.logon", "initial.text");
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Problem asking CPS for the default logon initial text", e); 
        }
    }
}
