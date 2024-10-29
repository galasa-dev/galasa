/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.properties;

import java.util.Map;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.internal.DockerContainerImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;

/**
 * Docker LeaveRunning DSE CPS Property
 * 
 * @galasa.cps.property
 * 
 * @galasa.name docker.container.TAG.leave.running
 * 
 * @galasa.description A property that allows a developer to specify a container should be left running
 * 
 * @galasa.required No 
 * 
 * @galasa.valid_values An ID for the container tag, e.g. MYCONTAINER
 * 
 * @galasa.examples 
 * <code>docker.container.MYCONTAINER.leave.running=true<br>
 * </code>
 * 
 * */
public class DockerLeaveRunning extends CpsProperties {

    public static String get(final DockerContainerImpl dockerContainerImpl) throws DockerManagerException {
		try {
            // Check for a DSE defined container leave running state
			String containerTag = dockerContainerImpl.getContainerTag();
			final String leaveRunning = getStringNulled(DockerPropertiesSingleton.cps(), "container", "leave.running", containerTag);
			return leaveRunning;
		} catch (final ConfigurationPropertyStoreException e) {
			throw new DockerManagerException("Problem asking the CPS for the container leave running value", e);
        }
	}
}
