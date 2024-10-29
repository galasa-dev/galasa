/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.properties;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zos.ZosManagerException;

/**
 * Developer Supplied Environment - CICS TS Region - Java home
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cicsts.dse.tag.[TAG].javahome
 * 
 * @galasa.description Provides the Java home value for a CICS TS region.
 * 
 * @galasa.required Yes if you want to use a Manager that requires the value, e.g. for JVMSERVER, otherwise not required
 * 
 * @galasa.default The value of Java home for the zOS Image for this CICS TS region. 
 * 
 * @galasa.valid_values A value for Java home required for the supplied to the DSE CICS region.
 * 
 * @galasa.examples 
 * <code>cicsts.dse.tag.PRIMARY.javahome=/usr/lpp/java/java/J8.0_64/</code><br>
 *
 */
public class DseJavaHome extends CpsProperties {
    public static String get(ICicsRegion region) throws CicstsManagerException {
		String tag = region.getTag();
        try {
        	String javaHome = getStringNulled(CicstsPropertiesSingleton.cps(), "dse.tag." + tag, "javahome");
        	if (javaHome == null) {
        		try {
					javaHome = region.getZosImage().getJavaHome();
				} catch (ZosManagerException e) {
					throw new CicstsManagerException("Problem getting value of Java home for zOS image", e);
				}        		
        	}
        	return javaHome;
        } catch (ConfigurationPropertyStoreException e) {
            throw new CicstsManagerException("Problem asking CPS for the DSE USSHOME for tag " + tag, e); 
        }
    }
}
