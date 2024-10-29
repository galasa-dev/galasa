/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty;

import dev.galasa.zos.IZosImage;

/**
 * Create a new zOS Liberty server
 */
public interface IZosLiberty {

    /**
     * Create a zOS Liberty server object using the Liberty/Galasa default properties
     * @param zosImage      the zOS image for this Liberty server
     * @return the zOS Liberty server object
     */
    public IZosLibertyServer newZosLibertyServer(IZosImage zosImage) throws ZosLibertyServerException;
    
    /**
     * Create a zOS Liberty server object using the Liberty/Galasa default properties
     * @param zosImage      the zOS image for this Liberty server
     * @param wlpInstallDir the Liberty install directory ($WLP_INSTALL_DIR) - can be null
     * @param wlpUserDir    the Liberty user directory ($WLP_USER_DIR) - can be null
     * @param wlpOutputDir    the Liberty output directory ($WLP_OUTPUT_DIR) - can be null
     * @return the zOS Liberty server object
     */
    public IZosLibertyServer newZosLibertyServer(IZosImage zosImage, String wlpInstallDir, String wlpUserDir, String wlpOutputDir) throws ZosLibertyServerException;
}
