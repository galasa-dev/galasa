/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.io.InputStream;
import java.util.Map;

import dev.galasa.docker.DockerManagerException;

public interface IDockerImageBuilder {

	public void buildImage(String imageName, InputStream Dockerfile) throws DockerManagerException;
	
    public void buildImage(String imageName, InputStream Dockerfile, Map<String,InputStream> resources) throws DockerManagerException;
    
    
}