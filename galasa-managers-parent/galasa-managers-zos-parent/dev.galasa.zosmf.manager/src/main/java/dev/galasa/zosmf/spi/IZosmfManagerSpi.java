/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.spi;

import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfManager;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfManagerException;

/**
 * Provides the SPI access to the zOSMF Manager
 *
 */
public interface IZosmfManagerSpi extends IZosmfManager {
    
    /**
     * Returns a zOSMF server 
     * @param serverId the server ID
     * @return the zOSMF server
     * @throws ZosmfManagerException
     */
    public IZosmf newZosmf(String serverId) throws ZosmfManagerException;
    
    /**
     * Returns a map of zOSMF servers available for use with a z/OS Image
     * @param zosImage the z/OS Image you want the zOS/MF servers for
     * @return the zOSMF servers
     * @throws ZosmfManagerException
     */
    public Map<String, IZosmf> getZosmfs(@NotNull IZosImage zosImage) throws ZosmfManagerException;

    /**
     * Returns a {@link IZosmfRestApiProcessor} for a single image
     * @param image
     * @param restrictToImage
     * @return {@link IZosmfRestApiProcessor}
     * @throws ZosmfManagerException
     */
    public IZosmfRestApiProcessor newZosmfRestApiProcessor(IZosImage image, boolean restrictToImage) throws ZosmfManagerException;
}
