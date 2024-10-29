/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.IDockerVolume;

/**
 * A implementation of the docker volumes that will be brought up on the engine
 * 
 *   
 */
public class DockerVolumeImpl implements IDockerVolume {
    private DockerManagerImpl dockerManager;

    private final String volumeName;
    private final String mountPath;
    private final DockerEngineImpl engine;
    private final boolean readOnly;
    private final String tag;

    private static final Log logger = LogFactory.getLog(DockerVolumeImpl.class);

    /**
     * Constructor that determines the nature of the volume (readOnly or not), and
     * provisions or ensures the volumes exists.
     * 
     * @param volumeName
     * @param mountPath
     * @param engine
     * @throws DockerManagerException
     */
    public DockerVolumeImpl(DockerManagerImpl dockerManager, String volumeName, String tag, String mountPath,
            DockerEngineImpl engine, boolean readOnly, boolean provision) throws DockerManagerException {
        this.dockerManager = dockerManager;
        this.volumeName = volumeName;
        this.mountPath = mountPath;
        this.engine = engine;
        this.readOnly = readOnly;
        this.tag = tag;

        if (provision) {
            logger.info("Generating volume");
            engine.createVolume(volumeName);
        }

        if (!doesVolumeExist()) {
            logger.error("No volume found with name: " + this.volumeName);
            throw new DockerManagerException("Could not find volume with name: " + this.volumeName);

        }
        logger.info("Volume found.");
    }

    /**
     * Returns volume name
     * 
     * @return volumeName
     */
    @Override
    public String getVolumeName() {
        return this.volumeName;
    }

    /**
     * Returns mount path
     * 
     * @return mountPath
     */
    @Override
    public String getMountPath() {
        return this.mountPath;
    }

    /**
     * Returns readonly state
     * 
     * @return readOnly
     */
    @Override
    public boolean readOnly() {
        return this.readOnly;
    }

    /**
     * Return tag of hosted engine.
     */
    @Override
    public String getEngineTag() {
        return engine.getEngineTag();
    }

    @Override
    public String getVolumeTag() {
        return this.tag;
    }

    /**
     * Checks the docker engine for a specific named volume
     * 
     * @return boolean exists
     */
    private boolean doesVolumeExist() throws DockerManagerException {
        if (engine.getVolume(this.volumeName) != null) {
            return true;
        }
        return false;
    }

    /**
     * Discard a volume. This should only be called for galasa created volumes.
     * 
     * @throws DockerManagerException
     */
    public void discard() throws DockerManagerException {

        engine.deleteVolume(this.volumeName);
    }

    /**
     * Load a file from an InputStream into the volume. This can be called before the volume is used or 
     * mounted to any containers.
     * 
     * @param fileName
     * @param data
     * @throws DockerManagerException
     */
    @Override
    public void LoadFile(String fileName, InputStream data) throws DockerManagerException {
        DockerImageBuilderImpl builder = new DockerImageBuilderImpl(engine);
        
        Map<String,Object> subs = new HashMap<>();
        
        subs.put("BUSYBOX", this.engine.getBusybox());
        subs.put("FILENAME", fileName);
        subs.put("MOUNTPATH", this.mountPath);
        
        // Create a busy box image to load the volume
        InputStream dockerfile = createDockerfile("VolumeBusyboxDockerfile", subs);
        Map<String, InputStream> resources = new HashMap<>();
        resources.put(fileName, data);
        builder.buildImage("galasa-volume-loader", dockerfile, resources);

        //  Run the busybox, then remove it
        JsonObject json = engine.createContainer(this.volumeName + "_LOADER", generateMetadata("galasa-volume-loader"));
        if (json == null) {
        	throw new DockerManagerException("Create container did not return JSON object.");
        }
        JsonElement oId = json.get("Id");
        if (oId == null) {
        	throw new DockerManagerException("Id property missing from create container JSON :-\n" + json.toString());
        }
        String containerId = oId.getAsString();

        String status = "";
        engine.startContainer(containerId);
        
        while (!"exited".equals(status)) {
            json = engine.getContainer(containerId);
            json = json.get("State").getAsJsonObject();
            status = json.get("Status").getAsString();
        }
        
        engine.deleteContainer(containerId);
    }

    /**
     * Pass a fileName and String to load into a docker volume. This can be called before the volume is used or 
     * mounted to any containers.
     * 
     * @param fileName
     * @param data
     * @throws DockerManagerException
     */
    @Override
    public void LoadFileAsString(String fileName, String data) throws DockerManagerException {
        LoadFile(fileName, new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * A private method used to create a dockerfile for the busybox image used to load files into the volume.
     * 
     * @param path
     * @param fileName
     * @return
     * @throws DockerManagerException
     */
    private InputStream createDockerfile(String dockerfile, Map<String,Object> subs) throws DockerManagerException {
        try {
            String dockerfileTemplate = this.dockerManager.getArtifactManager()
            .getBundleResources(this.getClass())
            .retrieveSkeletonFileAsString(dockerfile, subs);

            return new ByteArrayInputStream(dockerfileTemplate.getBytes());
        } catch (IOException | TestBundleResourceException e) {
            throw new DockerManagerException("Failed to generate the Dockerfile for loading volumes", e);
        }
    }

    /**
     * Generates the metadata used by container that will load a docker volume
     * @param imageName
     * @return
     */
    private JsonObject generateMetadata(String imageName) {
        JsonObject metadata = new JsonObject();
        metadata.addProperty("Image", imageName);

        JsonObject hostConfig = new JsonObject();
        metadata.add("HostConfig", hostConfig);
        
        JsonObject labels = new JsonObject();
        labels.addProperty("GALASA", "GALASA");
        metadata.add("Labels", labels);
        
        // Volumes
        JsonArray mounts = new JsonArray();

        JsonObject mount = new JsonObject();

        mount.addProperty("Target", this.mountPath);
        mount.addProperty("Source", this.volumeName);
        mount.addProperty("Type", "volume");
        mount.addProperty("ReadOnly", this.readOnly);

        mounts.add(mount);
        
        if (mounts.size() > 0 ) {
            hostConfig.add("Mounts", mounts);
            metadata.remove("HostConfig");
            metadata.add("HostConfig", hostConfig);
        }
        return metadata;
    }
    
    
    public void runCommand(String command) throws DockerManagerException {
        DockerImageBuilderImpl builder = new DockerImageBuilderImpl(engine);
        
        Map<String,Object> subs = new HashMap<>();
        subs.put("BUSYBOX", engine.getBusybox());
        subs.put("COMMAND", command);
        logger.info("Command: " + command);

        // Create a busy box image to load the volume
        InputStream dockerfile = createDockerfile("CommandBusyboxDockerfile", subs);

        builder.buildImage("galasa-volume-loader", dockerfile);

        //  Run the busybox, then remove it
        JsonObject json = engine.createContainer(this.volumeName + "_LOADER", generateMetadata("galasa-volume-loader"));
        String containerId = json.get("Id").getAsString();

        String status = "";
        engine.startContainer(containerId);
        
        while (!"exited".equals(status)) {
            json = engine.getContainer(containerId);
            json = json.get("State").getAsJsonObject();
            status = json.get("Status").getAsString();
        }
        
        engine.deleteContainer(containerId);
    }

	@Override
	public void fileChown(String userGroup, String filename) throws DockerManagerException {
		runCommand("\"chown\",\"" + userGroup + "\",\"" + this.mountPath + "/"+ filename +"\"");
	}

	@Override
	public void fileChmod(String permissions, String filename) throws DockerManagerException {
		runCommand("\"chmod\",\"" + permissions + "\",\"" + this.mountPath + "/"+ filename +"\"");
		
	}
}