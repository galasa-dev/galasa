/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.manager.ivt;

import static org.assertj.core.api.Assertions.assertThat;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.imstm.ImsSystem;
import dev.galasa.imstm.ImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

import org.apache.commons.logging.Log;

@Test
public class IMSTMManagerIVT {
	
   @Logger
   public Log logger;

   @ImsSystem(imsTag = "A")
   public IImsSystem ims;

   @ImsTerminal(imsTag = "A", loginCredentialsTag = "USER1")
   public IImsTerminal terminal;
   
  
   @BeforeClass
   public void setup() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {   
	  logger.info("IMS System provisioned for this test: " + ims.getApplid());
	  
      terminal.clear();
      terminal.waitForKeyboard();
   }
	
   @BeforeClass
   public void checkImsLoaded() {
	   assertThat(ims).isNotNull();
   }
   
   /**
    * Tests that the IMS Terminal in the IMS TM Manager retrieves the correct IMS System
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGetImsSystem() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the IMS Terminal gets the correct IMS System");
	   String testImsSystem = terminal.getImsSystem().toString().replace("IMS System[", "").replace("]", "");
	   assertThat(testImsSystem).isEqualTo(ims.getApplid());
   }
   
   /**
    * Tests that the IMS Terminal in the IMS TM Manager correctly connects to the IMS System
    * @throws ImstmManagerException
    * @throws TerminalInterruptedException 
    * @throws NetworkException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testConnectToImsSystem() throws ImstmManagerException, TerminalInterruptedException, KeyboardLockedException, NetworkException, TimeoutException {
	   logger.info("Testing that the IMS Terminal connects to the same IMS System after being disconnected");
	   
	   terminal.disconnect();
	   assertThat(terminal.isConnected()).isFalse();
	   
	   terminal.connectToImsSystem();
	   assertThat(terminal.isConnected()).isTrue();
	   assertThat(terminal.isClearScreen()).isTrue();
	   assertThat(terminal.getImsSystem().toString().replace("IMS System[", "").replace("]", "")).isEqualTo(ims.getApplid());
   }
   
   /**
    * Tests that the IMS Terminal in the IMS TM Manager resets and clears correctly
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws ImstmManagerException 
    */
   @Test
   public void testResetAndClear() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, ImstmManagerException {
	   logger.info("Testing that the IMS Terminal resets and clears screen correctly");
	   terminal.resetAndClear();
	   assertThat(terminal.isClearScreen()).isTrue();
   }
   
 }