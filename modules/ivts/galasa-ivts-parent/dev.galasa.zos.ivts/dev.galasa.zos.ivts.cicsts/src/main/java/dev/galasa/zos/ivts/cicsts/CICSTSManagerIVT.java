/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.cicsts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cicsresource.ICicsResource;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatchException;

import org.apache.commons.logging.Log;

@Test
public class CICSTSManagerIVT {
	
   @Logger
   public Log logger;

   @CicsRegion(cicsTag = "A")
   public ICicsRegion cics;

   @CicsTerminal(cicsTag = "A")
   public ICicsTerminal terminal;
   
   public String variableName = "VARNAME";
  
   public String programName = "IVTPROG";

   public String groupName = "IVTGROUP";
  
  
   @BeforeClass
   public void setup() throws KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException, FieldNotFoundException {   
	  logger.info("CICS Region provisioned for this test: " + cics.getApplid());
	  
      terminal.clear();
      terminal.waitForKeyboard();
   }
	
   @BeforeClass
   public void checkCicsLoaded() {
	   assertThat(cics).isNotNull();
   }
   
   /**
    * Tests that the CICS TS Manager retrieves the correct APPLID for the CICS Region
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test 
   public void testGetApplid() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
	   logger.info("Testing that the CICS TS Manager gets the correct APPLID for the CICS Region");
	   String testApplid = cics.getApplid();
	   assertThat(checkTerminalScreenContains(testApplid)).isTrue();
   }
   
   /**
    * Tests that the CICS TS Manager retrieves a CICS Resource
    * @throws CicstsManagerException 
    */
   @Test
   public void testCicsResource() throws CicstsManagerException  {
	   logger.info("Testing that the CICS TS Manager retrieves a CICS Resource");
	   ICicsResource cicsResource = cics.cicsResource();
	   assertThat(cicsResource).isNotNull();
   }
   
   /**
    * Tests that the CICS TS Manager retrieves the correct job
    * @throws ZosBatchException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGetRegionJob() throws CicstsManagerException, ZosBatchException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS TS Manager finds the CICS job for the CICS Region - there should be an active job as the CICS Region is running");
	   IZosBatchJob regionJob = cics.getRegionJob();
	   assertThat(regionJob).isNotNull();
	   
	   logger.info("Testing that the CICS Region job information is correct");
	   assertThat(regionJob.getJobname().toString()).isEqualToIgnoringCase(cics.getApplid());
	   assertThat(regionJob.getType()).isEqualToIgnoringCase("JOB");
	   assertThat(regionJob.getStatusString()).isEqualToIgnoringCase("ACTIVE");
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager retrieves the correct CICS Region
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGetCicsRegion() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS Terminal gets the correct CICS Region");
	   String testCicsRegion = terminal.getCicsRegion().toString().replace("CICS Region[", "").replace("]", "");
	   assertThat(checkTerminalScreenContains(testCicsRegion)).isTrue();
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager correctly connects to the CICS Region
    * @throws CicstsManagerException
    * @throws TerminalInterruptedException 
    * @throws NetworkException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testConnectToCicsRegion() throws CicstsManagerException, TerminalInterruptedException, KeyboardLockedException, NetworkException, TimeoutException {
	   logger.info("Testing that the CICS Terminal connects to the same CICS Region after being disconnected");
	   
	   terminal.disconnect();
	   assertThat(terminal.isConnected()).isFalse();
	   
	   terminal.connectToCicsRegion();
	   assertThat(terminal.isConnected()).isTrue();
	   assertThat(terminal.isClearScreen()).isTrue();
	   assertThat(terminal.getCicsRegion().toString().replace("CICS Region[", "").replace("]", "")).isEqualTo(cics.getApplid());
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager resets and clears correctly
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResetAndClear() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, CicstsManagerException {
	   logger.info("Testing that the CICS Terminal resets and clears screen correctly");
	   terminal.resetAndClear();
	   assertThat(terminal.isClearScreen()).isTrue();
   }
   
   /**
    * Tests that the CICS Terminal in the CICS TS Manager sets uppercase translation correctly
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testUppercaseTranslation() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   logger.info("Testing that the CICS Terminal sets uppercase translation correctly");
	   terminal.setUppercaseTranslation(true);
	   assertThat(terminal.isUppercaseTranslation()).isTrue();
	   
	   logger.info("Testing that uppercase translation is on by defining and retrieving variable " + variableName);
	   String variableValue = "lowercasegalasa";
	   startCeciSession();
	   cics.ceci().defineVariableText(terminal, variableName, variableValue);
	   assertThat(cics.ceci().retrieveVariableText(terminal, "&" + variableName)).isUpperCase();
	   
	   terminal.setUppercaseTranslation(false);
	   assertThat(terminal.isUppercaseTranslation()).isFalse();
	   
	   logger.info("Testing that uppercase translation is off by re-defining and retrieving variable " + variableName);
	   startCeciSession();
	   cics.ceci().defineVariableText(terminal, variableName, variableValue);
	   assertThat(cics.ceci().retrieveVariableText(terminal, "&" + variableName)).isEqualTo(variableValue);
   }
   
   /**
    * Tests that the CICS TS Hash Map implementation
    * @throws CicstsManagerException 
    * @throws CemtException
    */
   @Test
   public void testCicstsHashMap() throws CemtException, CicstsManagerException {
	   cics.ceda().createResource(terminal, "PROGRAM", programName, groupName, null);
	   assertThat(cics.ceda().resourceExists(terminal, "PROGRAM", programName, groupName)).isTrue();
	   
	   cics.ceda().installResource(terminal, "PROGRAM", programName, groupName);
	   CicstsHashMap resource = cics.cemt().inquireResource(terminal, "PROGRAM", programName);
	   assertThat(resource).isNotNull();
	   
	   resource.checkParameterEquals("program", programName);
	   assertThatThrownBy(() -> {
	      resource.checkParameterEquals("program", "WRONG");
	   }).isInstanceOf(CicstsManagerException.class).hasMessageContaining("Parameter program does not equal WRONG"); 
	   
	   assertThat(resource.isParameterEquals("program", programName)).isTrue();
	   assertThat(resource.isParameterEquals("program", "WRONG")).isFalse();
   }
   
   //@Test
   public void testAlterSit() throws Exception {
	   
	   // Sit value for GMTEXT
	   String testData = "Lewis is a cool guy";
	   
	   logger.info("Shutting down CICS");
	   cics.shutdown();
	   
	   logger.info("Setting GMTEXT sit parameter");
	   cics.alterSit("GMTEXT", testData);
	   
	   logger.info("Starting CICS");
	   cics.startup();
	   
	   // Show Good morning screen to test SIT change
	   terminal.waitForKeyboard().type("CSGM").enter().waitForTextInField("******\\\\(R)");
	   
	   assertThat(terminal.retrieveScreen()).contains(testData);
   }
   
   //@Test
   public void testAlterSitRestricted() throws Exception {
	   
	   logger.info("Shutting down CICS to alter SIT");
	   cics.shutdown();
	   
	   // Test that altering a restricted SIT parameter fails
	   assertThatThrownBy(() -> {
		   cics.alterSit("APPLID", "LEWIS");
		   }).isInstanceOf(CicstsManagerException.class).hasMessageContaining("APPLID is a restricted SIT parameter. It cannot be changed");
	   
	   cics.startup();
   }
   
   //@Test
   public void testAlterSitCicsActive() throws CicstsManagerException {
	   
	   logger.info("Altering SIT whilst CICS is active");
	   
	   // Test that altering a SIT parameter whilst CICS is active fails
	   assertThatThrownBy(() -> {
		   cics.alterSit("GMTEXT", "Hello World!");
		}).isInstanceOf(CicstsManagerException.class).hasMessageContaining("The CICS region " + cics.getApplid()
				+ " with tag " + cics.getTag() + " is still running. Cannot alter a running CICS region");
   }
   
   //@Test
   public void testRemoveSit() throws Exception {
	   
	   String testData = "Lewis is not a cool guy";
	   
	   // Shutdown CICS to alter parameter
	   cics.shutdown();
	   
	   logger.info("Adding GMTEXT SIT parameter");
	   cics.alterSit("GMTEXT", testData);
	   
	   // Startup CICS with new parameter change
	   cics.startup();
	   
	   terminal.waitForKeyboard().type("CSGM").enter().waitForTextInField("******\\\\(R)");
	   
	   assertThat(terminal.retrieveScreen()).contains(testData);
	   
	   // Shutdown CICS to allow a removal of SIT parameter
	   cics.shutdown();
	   
	   logger.info("Removing GMTEXT SIT parameter");
	   cics.removeSit("GMTEXT");
	   
	   // Restart CICS with the removed parameter changes
	   cics.startup();
	   
	   terminal.waitForKeyboard().type("CSGM").enter().waitForTextInField("******\\\\(R)");
	   
	   // Ensure the GMTEXT parameter is not effecting text on CSGM
	   assertThat(terminal.retrieveScreen()).doesNotContain(testData);
   }
   
   //@Test
   public void testRemoveSitRestricted() throws Exception {
	   
	   logger.info("Shutting down CICS to remove SIT");
	   cics.shutdown();
	   
	   // Test that altering a restricted SIT parameter fails
	   assertThatThrownBy(() -> {
		   cics.alterSit("APPLID", "LEWIS");
		   }).isInstanceOf(CicstsManagerException.class).hasMessageContaining("APPLID is a restricted SIT parameter. It cannot be removed");
	   
	   cics.startup();
   }
   
	//@Test
	public void testRemoveSitCicsActive() throws Exception {

		logger.info("Removing SIT whilst CICS is active");

		// Test that removing a SIT parameter whilst CICS is active fails
		assertThatThrownBy(() -> {
			cics.removeSit("GMTEXT");
		}).isInstanceOf(CicstsManagerException.class).hasMessageContaining("The CICS region " + cics.getApplid()
				+ " with tag " + cics.getTag() + " is still running. Cannot remove a SIT from a running region");
	}
   
   private boolean checkTerminalScreenContains(String expectedString) throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   terminal.type("CEMT INQUIRE").enter().waitForKeyboard();
	   if (terminal.retrieveScreen().contains(expectedString)) {
		   return true;
	   } else {
		   return false;
	   }
   }
   
   private void startCeciSession() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
	   terminal.clear();
	   terminal.waitForKeyboard();
	   terminal.type("CECI").enter().waitForKeyboard();
   } 

}