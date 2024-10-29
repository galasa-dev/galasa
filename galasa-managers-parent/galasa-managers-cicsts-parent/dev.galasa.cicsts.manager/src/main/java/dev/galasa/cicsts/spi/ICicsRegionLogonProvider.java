/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.spi;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsTerminal;

public interface ICicsRegionLogonProvider {
    
    boolean logonToCicsRegion(ICicsTerminal cicsTerminal) throws CicstsManagerException;
}