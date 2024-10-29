/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker;

import java.io.InputStream;

/** 
 * A Galasa object to track, bind and provision Docker volumes with.
 * 
 */
public interface IDockerVolume {

    /**
     * @return the volume names, specified or provisioned.
     */
    public String getVolumeName();

    /**
     * @return the volume tag
     */
    public String getVolumeTag();

    /**
     * @return the specified mount path.
     */
    public String getMountPath();

    /**
     * @return the read state of the volume.
     */
    public boolean readOnly();

    /**
     * @return the Tag of the engine used to host the volume.
     */
    public String getEngineTag();


    /**
     * Pre-populate a volume with some data. The filename needs to be passed
     * 
     * @param fileName
     * @param data
     * @throws DockerManagerException
     */
    public void LoadFile(String fileName, InputStream data) throws DockerManagerException;

    /**
     * Pre-populate a volume with some string data. Filename is required.
     * 
     * @param fileName
     * @param data
     * @throws DockerManagerException
     */
    public void LoadFileAsString(String fileName, String data) throws DockerManagerException;
    
    
    /**
     * Will execute a `chown USER:GROUP filename` on a file inside the volume.
     * 
     * @param userGroup
     * @param filename
     * @throws DockerManagerException
     */
    public void fileChown(String userGroup, String filename) throws DockerManagerException;
    
    /**
     * Will execute a `chmod XXX filename` where the XXX is the permission string, on a file inside a volume.
     * 
     * @param permissions
     * @param filename
     * @throws DockerManagerException
     */
    public void fileChmod(String permissions, String filename) throws DockerManagerException;
    
}