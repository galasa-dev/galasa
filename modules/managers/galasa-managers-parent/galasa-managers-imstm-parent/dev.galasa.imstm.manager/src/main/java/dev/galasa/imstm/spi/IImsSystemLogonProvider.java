/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.IImsTerminal;

public interface IImsSystemLogonProvider {
    
    boolean logonToImsSystem(IImsTerminal cicsTerminal) throws ImstmManagerException;
}