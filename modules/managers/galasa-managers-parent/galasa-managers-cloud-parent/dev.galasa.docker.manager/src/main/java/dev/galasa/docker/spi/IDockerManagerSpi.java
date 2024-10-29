/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerManager;

public interface IDockerManagerSpi extends IDockerManager {
    
    /**
     * This method is able to provide the dockerContainer instance with a defined Dockerhub Container Tag
     */
    
    @NotNull
    IDockerContainer provisionContainer(String dockerContainerTag, String image, boolean start, String dockerEngineTag) throws DockerManagerException;


    @NotNull
    String getEngineHostname(String dockerEngineTag) throws DockerManagerException;
}