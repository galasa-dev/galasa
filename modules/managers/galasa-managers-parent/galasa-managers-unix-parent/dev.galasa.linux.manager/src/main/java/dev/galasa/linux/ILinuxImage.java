/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;

/**
 * <p>
 * Represents a Linux Image .
 * </p>
 * 
 * <p>
 * Use a {@link LinuxImage} annotation to populate this field with
 * </p>
 * 
 *  
 *
 */
public interface ILinuxImage {

    /**
     * Get the name of the Linux Image
     * 
     * @return The image ID, never null
     */
    @NotNull
    String getImageID();

    /**
     * Retrieve the IP Network Host details
     * 
     * @return
     */
    @NotNull
    IIpHost getIpHost();

    /**
     * Retrieve the default credentials for the Image.
     * 
     * @return The default credentials - see
     *         {@link dev.galasa.ICredentials}
     * @throws LinuxManagerException if the credentials are missing or there is a
     *                               problem with the credentials store
     */
    @NotNull
    ICredentials getDefaultCredentials() throws LinuxManagerException;

    @NotNull
    ICommandShell getCommandShell() throws LinuxManagerException;

    @NotNull
    Path getRoot() throws LinuxManagerException;

    @NotNull
    Path getHome() throws LinuxManagerException;

    @NotNull
    Path getTmp() throws LinuxManagerException;

    @NotNull
    Path getRunDirectory() throws LinuxManagerException;
    
    @NotNull
    Path getArchivesDirectory() throws LinuxManagerException;

}
