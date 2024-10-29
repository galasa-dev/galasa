/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.elasticlog.internal.properties;

import dev.galasa.elasticlog.internal.ElasticLogManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * ElasticLog Endpoint Address CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name elastic.endpoint.address
 * 
 * @galasa.description Provides an address to send elastic requests to
 * 
 * @galasa.required Yes
 * 
 * @galasa.valid_values Any valid URI string
 * 
 * @galasa.examples 
 * <code>elastic.endpoint.address=https://yoursitehere.com/elasticendpoint</code>
 * 
 */
public class ElasticLogEndpoint extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		try {
			String elasticLogEndpoint = getStringNulled(ElasticLogPropertiesSingleton.cps(), "endpoint", "address");

			if (elasticLogEndpoint == null) {
				throw new ElasticLogManagerException("Could not find a ElasticLog endpoint in CPS.");
			}
			return elasticLogEndpoint;
		} catch (ConfigurationPropertyStoreException e) {
			throw new ElasticLogManagerException("Problem asking the CPS for the ElasticLog Endpoint", e);
        }
	}
}