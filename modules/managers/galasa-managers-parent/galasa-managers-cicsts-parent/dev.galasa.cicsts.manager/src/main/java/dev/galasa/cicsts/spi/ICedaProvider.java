/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.ICeda;
import dev.galasa.cicsts.ICicsRegion;

/**
 * Provides CICS Region related CEDA objects
 *
 */
public interface ICedaProvider {
    
    /**
     * Returns a unique instance of the ICemt per CICS region 
     * 
     * @param cicsRegion
     * @return ICeda object for this CICS region, will a different instance for different regions
     */
    @NotNull
    ICeda getCeda(ICicsRegion cicsRegion);

}
