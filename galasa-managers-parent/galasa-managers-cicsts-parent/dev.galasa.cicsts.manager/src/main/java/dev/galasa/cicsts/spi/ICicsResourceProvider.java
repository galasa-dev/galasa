/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.cicsresource.CicsResourceManagerException;
import dev.galasa.cicsts.cicsresource.ICicsResource;

/**
 * Provides CICS Region related ICicsResource objects
 *
 */
public interface ICicsResourceProvider {
    
    /**
     * Returns a unique instance of the ICicsResource per CICS region 
     * 
     * @param cicsRegion
     * @return ICicsResource object for this CICS region, will a different instance for different regions
     * @throws CicsResourceManagerException 
     */
    @NotNull
    ICicsResource getCicsResource(ICicsRegion cicsRegion) throws CicsResourceManagerException;

}
