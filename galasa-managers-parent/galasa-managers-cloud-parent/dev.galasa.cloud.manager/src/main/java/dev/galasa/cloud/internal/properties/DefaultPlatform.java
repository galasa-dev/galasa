/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.internal.properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Default platform for cloud containers
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cloud.default.platform
 * 
 * @galasa.description Specifies the default platform for cloud containers
 * 
 * @galasa.required No
 * 
 * @galasa.default none
 * 
 * @galasa.valid_values A valid platform name, depending on CPS configuration could be, homek8s, livegcp etc
 * 
 * @galasa.examples 
 * <code>cloud.default.platform=homek8s<br>
 * </code>
 * */
public class DefaultPlatform extends CpsProperties {

	public static String get() throws CloudManagerException {
		try {
			return getStringNulled(CloudPropertiesSingleton.cps(), "default", "platform");
		} catch (Exception e) {
			throw new CloudManagerException("Problem accessing CPS for default platform", e);
		}
	}
}
