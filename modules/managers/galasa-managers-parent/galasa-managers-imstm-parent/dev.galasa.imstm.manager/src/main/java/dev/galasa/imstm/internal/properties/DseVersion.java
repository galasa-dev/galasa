/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal.properties;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Developer Supplied Environment - IMS TM System - Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name imstm.dse.tag.[TAG].version
 * 
 * @galasa.description Provides the version of the IMS TM system to the DSE provisioner.  
 * 
 * @galasa.required Only requires setting if the test request it or a Manager performs a version dependent function.
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A value V.R.M version format, eg 15.4.0
 * 
 * @galasa.examples 
 * <code>imstm.dse.tag.PRIMARY.version=15.4.0</code><br>
 *
 */public class DseVersion extends CpsProperties {

    public static String get(String tag) throws ImstmManagerException {
        try {
            return getStringNulled(ImstmPropertiesSingleton.cps(), "dse.tag." + tag, "version");
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Problem asking CPS for the DSE version for tag " + tag, e); 
        }
    }
}
