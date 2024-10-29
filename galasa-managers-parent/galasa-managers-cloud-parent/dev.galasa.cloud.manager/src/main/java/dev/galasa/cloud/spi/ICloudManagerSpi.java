/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.spi;

import java.util.Properties;

import javax.validation.constraints.NotNull;

import dev.galasa.cloud.CloudManagerException;
import dev.galasa.cloud.ICloudContainer;

/**
 * SPI for the Cloud Manager
 * 
 *  
 *
 */
public interface ICloudManagerSpi {
	
	/**
	 * Retrieve the cloud container by tag
	 * 
	 * @param tag - The tag name
	 * @return the Cloud Container if found
	 * @throws CloudManagerException - If the tag is unknown
	 */
	@NotNull
	public ICloudContainer getCloudContainerByTag(@NotNull String tag) throws CloudManagerException;
	
	/**
	 * Generate a new Cloud Container, normal called by an application Manager during provision generate,
	 * but can be called at anytime (exception shutdown)
	 * 
	 * @param tag - The tag to assign, must not exist already
	 * @param provider - optional (null), the provider to use to provision the Cloud Container
	 * @param image - The image name to use
	 * @param ports - The ports to be exposed
	 * @param environmentProperties - Any environment properties to provide to the container
	 * @param runArguments - Any run arguments to start the container with
	 * @param autoStart - The container is to be started during provisionStart, only relevant if this method is called during provisionGenerate.
	 * @param startOrder - If autoStart = true, then the order the container is to be started, only relevant if this method is called during provisionGenerate.
	 * @return a ICloudContainer instance.
	 * @throws CloudManagerException
	 */
	@NotNull
	public ICloudContainer generateCloudContainer(
			@NotNull String                tag,
			@NotNull String                provider,
			@NotNull String                image,
			@NotNull ICloudContainerPort[] ports,
			         Properties            environmentProperties,
			         String[]              runArguments,
		       	     boolean               autoStart,
			         int                   startOrder) throws CloudManagerException;

	/**
	 * Register a provider for Cloud Containers
	 * 
	 * @param containerProvider A Cloud Container provisioner
	 */
	public void registerCloudContainerProvider(@NotNull ICloudContainerProvider containerProvider);
}
