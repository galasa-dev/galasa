/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;

public interface ICicsRegionProvisioned extends ICicsRegion {

    String getNextTerminalId();
    
    
    void submitRuntimeJcl() throws CicstsManagerException;
    boolean hasRegionStarted() throws CicstsManagerException; 

}
