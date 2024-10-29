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
 * ElasticLog Endpoint Local Run CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name elastic.local.run.log
 * 
 * @galasa.description Activates the ElasticLog Manager for local runs
 * 
 * @galasa.required Yes
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true, false
 * 
 * @galasa.examples 
 * <code>elastic.local.run.log=true</code>
 * 
 * @galasa.extra
 * ElasticLog Manager will not run automatically for a local run. <br>
 * By setting this property to true, the manager will activate locally.
 * 
 */
public class ElasticLogLocalRun extends CpsProperties {

    public static String get() throws ElasticLogManagerException {
		return getStringWithDefault(ElasticLogPropertiesSingleton.cps(), "false", "local", "run.log");
	}
}