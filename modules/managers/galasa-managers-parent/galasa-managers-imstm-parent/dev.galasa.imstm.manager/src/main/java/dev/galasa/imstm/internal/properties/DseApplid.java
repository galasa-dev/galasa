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
 * Developer Supplied Environment - IMS TM System - Applid
 * 
 * @galasa.cps.property
 * 
 * @galasa.name imstm.dse.tag.[TAG].applid
 * 
 * @galasa.description Provides the applid of the IMS TM system for the DSE provisioner.  The applid setting
 * is mandatory for a DSE region.
 * 
 * @galasa.required Yes if you want a DSE region, otherwise not required
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid VTAM applid
 * 
 * @galasa.examples 
 * <code>imstm.dse.tag.PRIMARY.applid=IMS1</code><br>
 *
 */
public class DseApplid extends CpsProperties {

    public static String get(String tag) throws ImstmManagerException {
        try {
            String applid = getStringNulled(ImstmPropertiesSingleton.getCps(), "dse.tag", "applid", tag);
            return applid != null? applid.toUpperCase().trim(): applid;
        } catch (ConfigurationPropertyStoreException e) {
            throw new ImstmManagerException("Problem asking CPS for the DSE applid for tag " + tag, e); 
        }
    }
}
