/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

/**
 * Phoenix 2 Endpoint Address CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name phoenix2.endpoint.address
 * 
 * @galasa.description Provides the address to send PME reports to
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values Any valid URI string
 * 
 * @galasa.examples 
 * <code>phoenix2.endpoint.address=http://phoenix.ibm.com/pme</code>
 * 
 */
public class Phoenix2Endpoint extends CpsProperties {

    public static String get() throws Phoenix2ManagerException {
		try {
			String elasticLogEndpoint = getStringNulled(Phoenix2PropertiesSingleton.cps(), "endpoint", "address");

			if (elasticLogEndpoint == null) {
				throw new Phoenix2ManagerException("Could not find a Phoenix PME endpoint in CPS.");
			}
			return elasticLogEndpoint;
		} catch (ConfigurationPropertyStoreException e) {
			throw new Phoenix2ManagerException("Problem asking the CPS for the Phoenix PME", e);
        }
	}
}