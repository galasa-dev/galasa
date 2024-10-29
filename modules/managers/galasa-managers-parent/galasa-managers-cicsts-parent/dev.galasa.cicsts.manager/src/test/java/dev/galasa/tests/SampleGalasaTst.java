/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.tests;

import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.ICicsRegion;

@Test
public class SampleGalasaTst {

    @CicsRegion
    public ICicsRegion region1;

    @CicsRegion(cicsTag="SECONDARY")
    public ICicsRegion region2;
    
}