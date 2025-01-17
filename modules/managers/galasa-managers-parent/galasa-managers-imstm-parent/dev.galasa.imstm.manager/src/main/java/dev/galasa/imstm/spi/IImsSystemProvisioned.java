/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.IImsSystem;

public interface IImsSystemProvisioned extends IImsSystem {

    String getNextTerminalId();
    
    
    void submitRuntimeJcl() throws ImstmManagerException;
    boolean hasRegionStarted() throws ImstmManagerException; 

}
