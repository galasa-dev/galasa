/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.docker.IDockerVolume;

/**
 * Implementation for the object that represents the container configurations that can be edited for container startup
 * 
 *   
 */
public class DockerContainerConfigImpl implements IDockerContainerConfig {
    private List<IDockerVolume>     volumes     = new ArrayList<>();
    private HashMap<String,String>  envs     = new HashMap<>();
    private List<String>            ports   = new ArrayList<>();

    /**
     * Consturctors that sets all requested volumes to the config
     * 
     * @param volumes
     */
    public DockerContainerConfigImpl(List<IDockerVolume> volumes) {
        this.volumes = volumes;
    }

    /**
     * Sets environment variables
     * 
     * @param envs
     */
    @Override
    public void setEnvs(HashMap<String, String> envs) {
       this.envs = envs;
    }

    /**
     * Returns set Environment variables for this configuration.
     * 
     * @return envs
     */
    @Override
    public HashMap<String,String> getEnvs() {
        return this.envs;
    }

    /**
     * Retruns a list of all the volumes in this configuration
     * 
     * @return volumes
     */
    @Override
    public List<IDockerVolume> getVolumes() {
        return this.volumes;
    }

    /**
     * Returns a specific named volume from the configuration
     * 
     * @return volume
     */
    @Override
    public IDockerVolume getVolumeByTag(String volumeTag) {
        for (IDockerVolume volume : this.volumes) {
            if (volumeTag.equals(volume.getVolumeTag())) {
                return volume;
            }
        }
        return null;
    }

    /**
     * Allows ports to be exposed at container startup
     */
    @Override
    public void setExposedPorts(List<String> ports) {
        this.ports = ports;
    }

    /**
     * Returns a list of ports exposed from the configuration
     * @return ports
     */
    @Override
    public List<String> getExposedPorts() {
        return this.ports;
    }
    
}