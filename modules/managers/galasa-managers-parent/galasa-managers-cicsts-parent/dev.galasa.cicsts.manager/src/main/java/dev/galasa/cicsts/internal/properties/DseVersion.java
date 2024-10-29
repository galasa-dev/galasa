/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Developer Supplied Environment - CICS TS Region - Version
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsts.dse.tag.[TAG].version
 * 
 * @galasa.description Provides the version of the CICS TS region to the DSE provisioner.  
 * 
 * @galasa.required Only requires setting if the test request it or a Manager performs a version dependent function.
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A value V.R.M version format, eg 5.6.0
 * 
 * @galasa.examples 
 * <code>cicsts.dse.tag.PRIMARY.version=5.6.0</code><br>
 *
 */public class DseVersion extends CpsProperties {

    public static String get(String tag) throws CicstsManagerException {
        try {
            return getStringNulled(CicstsPropertiesSingleton.cps(), "dse.tag." + tag, "version");
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the DSE version for tag " + tag, e); 
        }
    }
}
