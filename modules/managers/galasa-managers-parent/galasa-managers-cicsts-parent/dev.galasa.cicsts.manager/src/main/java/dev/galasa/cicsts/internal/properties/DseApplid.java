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
 * Developer Supplied Environment - CICS TS Region - Applid
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsts.dse.tag.[TAG].applid
 * 
 * @galasa.description Provides the applid of the CICS TS region for the DSE provisioner.  The applid setting
 * is mandatory for a DSE region.
 * 
 * @galasa.required Yes if you want a DSE region, otherwise not required
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A value VTAM applid
 * 
 * @galasa.examples 
 * <code>cicsts.dse.tag.PRIMARY.applid=CICS1A</code><br>
 *
 */
public class DseApplid extends CpsProperties {

    public static String get(String tag) throws CicstsManagerException {
        try {
            String applid = getStringNulled(CicstsPropertiesSingleton.cps(), "dse.tag." + tag, "applid");
            return applid != null? applid.toUpperCase(): applid;
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the DSE applid for tag " + tag, e); 
        }
    }
}
