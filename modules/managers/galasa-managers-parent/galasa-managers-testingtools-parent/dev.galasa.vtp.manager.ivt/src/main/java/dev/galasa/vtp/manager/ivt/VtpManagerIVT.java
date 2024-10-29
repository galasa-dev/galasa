/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.ivt;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

 @Test
 public class VtpManagerIVT {
	 
	 @Logger
	 public Log logger;

	 @CicsRegion
	 public ICicsRegion cics;

	 @CicsTerminal
	 public ICicsTerminal terminal;

    @Test
    public void test1() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
    	terminal.type("TSQT").enter().wfk().clear().wfk();
    }
    
    
    @Test
    public void test2() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
    	terminal.type("TSQT").enter().wfk().clear().wfk();
    }
  
 }
 