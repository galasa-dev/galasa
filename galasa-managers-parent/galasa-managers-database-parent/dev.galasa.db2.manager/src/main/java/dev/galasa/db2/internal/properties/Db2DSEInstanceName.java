/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.db2.internal.properties;

import dev.galasa.db2.Db2ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * DB2 DSE instance name
 * 
 * @galasa.cps.property
 * 
 * @galasa.name db2.dse.instance.[tag].name
 * 
 * @galasa.description Provide a DSE instance name
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid tag for a defined Db2 instance
 * 
 * @galasa.examples 
 * <code>db2.dse.instance.[tag].name=TESTDB<br>
 * </code>
 * */
public class Db2DSEInstanceName extends CpsProperties {

    public static String get(String tag) throws Db2ManagerException {
		try {

			final String instance = getStringNulled(Db2PropertiesSingleton.cps(), "dse.instance", "name", tag);

			if (instance == null) {
				throw new Db2ManagerException("Could not find DSE Db2 Instance with Tag: " + tag);
			}
			return instance;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new Db2ManagerException("Problem asking the CPS for properties", e);
        }
	}
}
