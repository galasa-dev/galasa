/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.windows;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;

/**
 * <p>
 * Represents a Windows Image .
 * </p>
 * 
 * <p>
 * Use a {@link WindowsImage} annotation to populate this field with
 * </p>
 * 
 *  
 *
 */
public interface IWindowsImage {

    /**
     * Get the name of the Windows Image
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
     * @throws WindowsManagerException if the credentials are missing or there is a
     *                               problem with the credentials store
     */
    @NotNull
    ICredentials getDefaultCredentials() throws WindowsManagerException;

    @NotNull
    ICommandShell getCommandShell() throws WindowsManagerException;

    @NotNull
    Path getRoot() throws WindowsManagerException;

    @NotNull
    Path getHome() throws WindowsManagerException;

    @NotNull
    Path getTmp() throws WindowsManagerException;

    @NotNull
    Path getRunDirectory() throws WindowsManagerException;

}
