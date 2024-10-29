/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

/**
 * Phoenix logging enabled
 * 
 * @galasa.cps.property
 * 
 * @galasa.name phoenix2.logging.enabled
 * 
 * @galasa.description Activates the logging 
 * 
 * @galasa.required no
 * 
 * @galasa.default true
 * 
 * @galasa.valid_values true, false
 * 
 * @galasa.examples 
 * <code>phoenix2.logging.enabled=true</code>
 * 
 * @galasa.extra
 * Ability to stop phoenix2 from logging on some runs
 * 
 */
public class Phoenix2Enabled extends CpsProperties {

    public static boolean get() throws Phoenix2ManagerException {
		return Boolean.parseBoolean(getStringWithDefault(Phoenix2PropertiesSingleton.cps(), "true", "logging", "enabled"));
	}
}