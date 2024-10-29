/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.spi;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.cloud.CloudManagerException;
import dev.galasa.cloud.ICloudContainer;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;

/**
 * Interface from the Cloud Manager to Cloud Container Providers 
 * 
 *  
 *
 */
public interface ICloudContainerProvider {
	
	@NotNull
	public String getName();
	
	/**
	 * Generate a new Cloud Container
	 * 
	 * @param tag - The tag to assign
	 * @param platform - The platform to install the container on
	 * @param image - The image name to use, at this stage this will be the full image name
	 * @param ports - The ports to expose
	 * @param environmentProperties - Environment properties to provide to the container
	 * @param runArguments - Run arguments to provide to the container
	 * @return A ICloudContainer instance
	 * @throws CloudManagerException
	 */
	public ICloudContainer generateCloudContainer(
			@NotNull String                tag,
			@NotNull String                platform,
			@NotNull String                image,
			@NotNull ICloudContainerPort[] ports,
			         Properties            environmentProperties,
			         String[]              runArguments) throws ManagerException, InsufficientResourcesAvailableException;

}
