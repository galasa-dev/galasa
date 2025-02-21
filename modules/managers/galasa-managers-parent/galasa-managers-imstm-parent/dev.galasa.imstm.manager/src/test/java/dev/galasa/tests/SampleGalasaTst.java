/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.tests;

import dev.galasa.Test;
import dev.galasa.imstm.ImsSystem;
import dev.galasa.imstm.IImsSystem;

@Test
public class SampleGalasaTst {

    @ImsSystem
    public IImsSystem system1;

    @ImsSystem(imsTag="SECONDARY")
    public IImsSystem system2;
    
}