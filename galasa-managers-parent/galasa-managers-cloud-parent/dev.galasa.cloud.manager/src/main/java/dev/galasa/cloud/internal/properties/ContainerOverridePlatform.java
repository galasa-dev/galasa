/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.internal.properties;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.framework.spi.cps.CpsProperties;


/**
 * Container platform override
 * 
 * @galasa.cps.property
 * 
 * @galasa.name cloud.container.TAG.platform
 * 
 * @galasa.description Overrides the platform used for tagged container 
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values A valid plaform name, depending on CPS configuration could be,  homek8s, livegcp etc
 * 
 * @galasa.examples 
 * <code>cloud.container.API.platform=homek8s<br>
 * </code>
 * */public class ContainerOverridePlatform extends CpsProperties {

	public static String get(final String tag) throws CloudManagerException {
		try {
			return getStringNulled(CloudPropertiesSingleton.cps(), "container." + tag, "platform");
		} catch (Exception e) {
			throw new CloudManagerException("Problem accessing CPS for cloud container platform override", e);
		}
	}
}
