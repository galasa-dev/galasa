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
 * Developer Supplied Environment - CICS TS Region - USSHOME
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsts.dse.tag.[TAG].usshome
 * 
 * @galasa.description Provides the USSHOME value of the CICS TS region.
 * 
 * @galasa.required Yes if you want to use a Manager that requires the value, e.g. for JVMSERVER, otherwise not required
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A value for USSHOME that matches the value supplied to the DSE CICS region.
 * 
 * @galasa.examples 
 * <code>cicsts.dse.tag.PRIMARY.usshome=/cics/cics730</code><br>
 *
 */
public class DseUssHome extends CpsProperties {

    public static String get(String tag) throws CicstsManagerException {
        try {
        	return getStringNulled(CicstsPropertiesSingleton.cps(), "dse.tag." + tag, "usshome");
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the DSE USSHOME for tag " + tag, e); 
        }
    }
}
