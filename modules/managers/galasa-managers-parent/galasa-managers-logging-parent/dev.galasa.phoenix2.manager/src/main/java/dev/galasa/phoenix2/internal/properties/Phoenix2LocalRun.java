/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal.properties;

import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.phoenix2.internal.Phoenix2ManagerException;

/**
 * Phoeniux Local Run CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name phoenix2.local.run.log
 * 
 * @galasa.description Activates the logging for local runs
 * 
 * @galasa.required no
 * 
 * @galasa.default false
 * 
 * @galasa.valid_values true, false
 * 
 * @galasa.examples 
 * <code>phoenix2.local.run.log=true</code>
 * 
 * @galasa.extra
 * Phoenix PME logging Manager will not run automatically for a local run. <br>
 * By setting this property to true, the event emitting will occur for local runs.
 * 
 */
public class Phoenix2LocalRun extends CpsProperties {

    public static boolean get() throws Phoenix2ManagerException {
		return Boolean.parseBoolean(getStringWithDefault(Phoenix2PropertiesSingleton.cps(), "false", "local", "run.log"));
	}
}