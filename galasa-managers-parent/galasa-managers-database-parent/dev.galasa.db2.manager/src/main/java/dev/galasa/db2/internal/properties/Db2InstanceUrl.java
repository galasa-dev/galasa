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
 * DB2 instance url
 * 
 * @galasa.cps.property
 * 
 * @galasa.name db2.instance.TESTDB.url
 * 
 * @galasa.description Url description including port for the database
 * 
 * @galasa.required Yes
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid Db2 Url
 * 
 * @galasa.examples 
 * <code>db2.instance.TESTDB.url=db2://example.url.com:40100/DATABASE<br>
 * </code>
 * */
public class Db2InstanceUrl extends CpsProperties {

    public static String get(String tag) throws Db2ManagerException {
		try {

			final String instance = getStringNulled(Db2PropertiesSingleton.cps(), "instance", "url", tag);

			if (instance == null) {
				throw new Db2ManagerException("Could not find Db2 Instance url with Tag: " + tag);
			}
			return instance;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new Db2ManagerException("Problem asking the CPS for properties", e);
        }
	}
}
