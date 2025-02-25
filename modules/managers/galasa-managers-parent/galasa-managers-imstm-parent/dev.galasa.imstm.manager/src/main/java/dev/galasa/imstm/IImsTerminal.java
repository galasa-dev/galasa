/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm;

import dev.galasa.zos3270.ITerminal;

public interface IImsTerminal extends ITerminal {

    IImsSystem getImsSystem();

    void connectToImsSystem() throws ImstmManagerException;
    
    IImsTerminal resetAndClear() throws ImstmManagerException;

    String getLoginCredentialsTag();

}
