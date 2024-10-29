/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.util.Collection;
import java.util.List;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;

public interface IDockerEnvironment {
	
	/**
	 * Build all docker resources, including finding and pulling images, and building containers.
	 * 
	 * @param testClasses
	 * @throws DockerProvisionException
	 */
	public void build(List<Class<?>> testClasses) throws DockerProvisionException;

	/**
	 * Clean up the docker environment, discard all containers in the instance.
	 * 
	 * @throws DockerManagerException
	 */
	public void discard() throws DockerManagerException;

	/**
	 * Return the docker engine implementation used in this docker environment instance.
	 * 
	 * @return
	 * @throws DockerManagerException
	 */
	public DockerEngineImpl getDockerEngineImpl(String dockerEngineTag) throws DockerManagerException;
	
	/**
	 * Return a specified docker containers implementation
	 * 
	 * @param dockerContainerTag
	 * @return
	 * @throws DockerManagerException
	 */
	public DockerContainerImpl getDockerContainerImpl(String dockerContainerTag) throws DockerManagerException;

	/**
	 * Return a collection of all the docker containers running in this environment, via there tags
	 * 
	 * @return
	 */
	public Collection<DockerContainerImpl> getContainers();

	/**
	 * Provision a docker container of the passed specifications.
	 * 
	 * @param tag
	 * @param image
	 * @param start
	 * @return
	 * @throws DockerProvisionException
	 */
	public DockerContainerImpl provisionDockerContainer(String tag, String image, boolean start, String DockerEngineTag) throws DockerProvisionException;
	//public void preAllocate(Resource rm) throws ResourceManagementException;

	/**
	 * Allocates a docker volume with a passed name. If the volume already exsists on the Engine, that volume will be used.
	 * 
	 * @param volumeName
	 * @return
	 * @throws DockerProvisionException
	 */
	public DockerVolumeImpl allocateDockerVolume(String volumeName, String tag, String mountPath, String DockerEngineTag, boolean persist) throws DockerProvisionException;

	/**
	 * Removes a docker volume. 
	 * 
	 * @param volume
	 * @throws DockerManagerException
	 */
	public void removeDockerVolume(DockerVolumeImpl volume) throws DockerManagerException;


	/**
	 *  Free up the docker slot used to house a docker container.
	 * 
	 * @param dockerSlot
	 * @throws DockerProvisionException
	 */
	public void freeDockerSlot(DockerSlotImpl dockerSlot) throws DockerProvisionException;

	//public void abortPreAllocation();
}