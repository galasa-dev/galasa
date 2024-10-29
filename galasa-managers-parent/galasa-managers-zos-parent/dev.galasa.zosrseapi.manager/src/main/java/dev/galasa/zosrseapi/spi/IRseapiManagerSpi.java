/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiManager;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiManagerException;

/**
 * Provides the SPI access to the RSE API Manager
 *
 */
public interface IRseapiManagerSpi extends IRseapiManager {
    
    /**
     * Returns a RSE API server
     * @param serverId the server ID
     * @return the RSE API server
     * @throws RseapiManagerException
     */
    public IRseapi newRseapi(String serverId) throws RseapiManagerException;
    
    /**
     * Returns a map of RSE API servers available for use with a z/OS Image
     * @param zosImage the z/OS Image you want the RSE API servers for
     * @return the RSE API servers
     * @throws RseapiManagerException
     */
    public Map<String, IRseapi> getRseapis(@NotNull IZosImage zosImage) throws RseapiManagerException;

    /**
     * Returns a {@link IRseapiRestApiProcessor} for a single image
     * @param image
     * @param restrictToImage
     * @return {@link IRseapiRestApiProcessor}
     * @throws RseapiManagerException
     */
    public IRseapiRestApiProcessor newRseapiRestApiProcessor(IZosImage image, boolean restrictToImage) throws RseapiManagerException;
}
