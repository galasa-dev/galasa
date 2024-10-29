/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.util.List;

import com.google.gson.JsonObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerImage;
import dev.galasa.framework.spi.IFramework;

/**
 * DockerImageImpl. Allows for the checking of images on registries and pulling of images to the docker engine.
 * 
 *   
 */
public class DockerImageImpl implements IDockerImage {
    private final IFramework            framework;
    private final DockerManagerImpl     dockerManager;
    private final DockerEngineImpl      dockerEngine;
    private final String                fullImageName;
    private String                      fullName;
    private String                      authToken;

    private String                      imageName;
    private String                      tag;
    
    private boolean                     authRequired = false;
    private boolean                     local = false;

    private static final Log            logger = LogFactory.getLog(DockerImageImpl.class);

    /**
     * DockerImageImpl
     * 
     * @param framework
     * @param dockerManager
     * @param dockerEngine
     * @param fullImageName
     */
    public DockerImageImpl(IFramework framework, DockerManagerImpl dockerManager, 
            DockerEngineImpl dockerEngine, String fullImageName) {
        this.framework          = framework;
        this.dockerManager      = dockerManager;
        this.dockerEngine       = dockerEngine;
        this.fullImageName      = fullImageName;
        getWorkingName(this.fullImageName);
    }

    /**
     * returns the full name of the docker container
     * 
     * @return String
     */
    public String getFullName() {
        return this.fullName;
    }

    public String getImageName() {
        return this.imageName;
    }

    public String getTag() {
        return this.tag;
    }

    /**
     * Using the full name to try and locate the image in question in the registries or locally on the docker engine. 
     * 
     * @throws DockerManagerException
     */
    public void locateImage() throws DockerManagerException {
        if (this.fullName != null) {
            logger.info("Image already located.");
            return;
        }

        String workingName = getWorkingName(this.fullImageName);

		List<DockerRegistryImpl> registries = dockerManager.getRegistries();
		for(DockerRegistryImpl registry : registries) {
			if (registry.doYouHave(this)) {
                this.fullName = registry.getHost() + "/" + registry.getPrefix() + workingName;
                this.authToken = registry.getAuthToken();
                if (this.authToken != null) {
                    authRequired = true;
                }
				logger.info("Docker Image located in registry: " + registry.getHost());
				logger.info( "Docker image full name is '" + this.fullName + "'");
				return;
            }
        }

        JsonObject image = dockerEngine.getImage(workingName);
		if (image != null) {
			this.fullName = workingName;
            logger.info("Docker Image located only on the server as name '" + this.fullName + "'");
            this.local = true;
			return;
		}

		throw new DockerManagerException("Unable to locate Docker Image '" + this.fullImageName + "'");
    }

    private String getWorkingName(String fullImageName) {
        if(!fullImageName.contains(":")){
            fullImageName = fullImageName+":latest";
        }
        splitName(fullImageName);
        return fullImageName;
    }

    private void splitName(String fullImageName) {
        this.imageName = fullImageName.substring(0, fullImageName.indexOf(":"));
        this.tag = fullImageName.substring(fullImageName.indexOf(":")+1);
    }

    /**
     * Sets the full name of the image
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Pulls the image onto the docker engine.
     * 
     * @throws DockerManagerException
     */
    public void pullImage() throws DockerManagerException {
        String pull;
        if (this.fullName == null) {
            throw new DockerManagerException("Unable to pull image, full image name not set");
        }

        if (this.local) {
            logger.info("No need to pull " + this.fullName + ", image is local");
            return;
        }

        if(authRequired) {
            pull = dockerEngine.pullImage(this.fullName, this.authToken);
        } else {
            pull = dockerEngine.pullImage(this.fullName);
        }
        if (pull == null) {
            throw new DockerManagerException("Docker daemon did not respond to pull request");
        }

        if (pull.contains("Status: Downloaded newer image")) {
			logger.info("Docker Image " + this.fullName + " pulled");
		} else if (pull.contains("Status: Image is up to date")) {
			logger.info("Docker Image " + this.fullName + " was not pulled, current image up to date");
		} else {
			throw new DockerManagerException("Unrecognised response from pull request\n" + pull);
		}
    }
}