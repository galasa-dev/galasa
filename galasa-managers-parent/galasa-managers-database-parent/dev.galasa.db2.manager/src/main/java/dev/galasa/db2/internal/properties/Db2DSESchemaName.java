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
 * DB2 DSE Schema name
 * 
 * @galasa.cps.property
 * 
 * @galasa.name db2.dse.schema.[tag].name
 * 
 * @galasa.description A dse TAG which refers to an existing Schema
 * 
 * @galasa.required no
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values The name of an existing schema on the database being used.
 * 
 * @galasa.examples 
 * <code>db2.dse.schema.MYSCHEMA.name=SCHEMANAME<br>
 * </code>
 * */
public class Db2DSESchemaName extends CpsProperties{
	public static String get(String tag) throws Db2ManagerException {
		try {

			final String name = getStringNulled(Db2PropertiesSingleton.cps(), "dse.schema", "name", tag);

			if (name == null) {
				throw new Db2ManagerException("Could not find DSE Db2 Instance with Tag: " + tag);
			}
			return name;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new Db2ManagerException("Problem asking the CPS for properties", e);
        }
	}
}
