/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import javax.validation.constraints.NotNull;

import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICicsRegion;

/**
 * Provides CICS Region related CECI objects
 *
 */
public interface ICeciProvider {
    
    /**
     * Returns a unique instance of the ICeci per CICS region 
     * 
     * @param cicsRegion
     * @return ICeci object for this CICS region, will a different instance for different regions
     */
    @NotNull
    ICeci getCeci(ICicsRegion cicsRegion);

}
