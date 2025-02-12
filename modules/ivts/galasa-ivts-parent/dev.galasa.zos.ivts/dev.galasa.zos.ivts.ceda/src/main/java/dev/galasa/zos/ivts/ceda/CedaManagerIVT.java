/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.zos.ivts.ceda;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;

import dev.galasa.BeforeClass;
import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.cicsts.CedaException;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class CedaManagerIVT {
	
   @Logger
   public Log logger;

   @CicsRegion(cicsTag = "A")
   public ICicsRegion cics;

   @CicsTerminal(cicsTag = "A")
   public ICicsTerminal cedaTerminal;

   @CicsTerminal(cicsTag = "A")
   public ICicsTerminal cemtTerminal;
   
   @CicsTerminal(cicsTag = "A")
   public ICicsTerminal terminal;
   
   public String programName1 = "PROG1";

   public String programName2 = "PROG2";
   
   public String trxName = "TRX1";
   
   public String libName = "LIB1";
   
   public String groupName1 = "GROUP1";
   
   public String groupName2 = "GROUP2";
   
   @BeforeClass
   public void setup() throws Exception {
	  logger.info("CICS Region provisioned for this test: " + cics.getApplid());
      
	  cedaTerminal.clear();
      cedaTerminal.waitForKeyboard();
      
      cemtTerminal.clear();
      cemtTerminal.waitForKeyboard();
      
      terminal.clear();
      terminal.waitForKeyboard();
   }

   @BeforeClass
   public void checkCedaLoaded() throws CicstsManagerException {
      assertThat(cics.ceda()).isNotNull();
   }
   
   /**
    * Creating, installing and deleting a Program resource with CEDA
    * 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testResourceProgram() throws CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException  {
      String resourceType = "PROGRAM";

      logger.info("Now checking that the Program " + programName1 + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(false);

      logger.info("Now creating the Program " + programName1 + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, programName1, groupName1, null);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(true);

      logger.info("Now installing the Program " + programName1 + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, programName1, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName1).get("program")).isEqualToIgnoringCase(programName1);
      
      logger.info("Manually testing that Program " + programName1 + " was installed using CEMT");
      terminal.type("CEMT INQUIRE PROGRAM(" + programName1 + ")").enter().waitForKeyboard();
      assertThat(terminal.retrieveScreen().contains("RESPONSE: NORMAL")).isTrue();
      
      logger.info("Now deleting the Program " + programName1 + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, programName1, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, programName1, groupName1)).isEqualTo(false);
   }

   /**
    * Creating, installing and deleting a Transaction resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceTransaction() throws CicstsManagerException{
      String resourceType = "TRANSACTION";
      String resourceParameters = "PROGRAM(" + programName1 + ")";
     
      logger.info("Now checking that the Transaction " + trxName + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(false);

      logger.info("Now creating the Transaction " + trxName + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, trxName, groupName1, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(true);

      logger.info("Now installing the Transaction " + trxName + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, trxName, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, trxName).get("transaction")).isEqualToIgnoringCase(trxName);
      
      logger.info("Now deleting the Transaction " + trxName + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, trxName, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, trxName, groupName1)).isEqualTo(false);
   }

   /**
    * Creating, installing and deleting a Library resource with CEDA
    * 
    * @throws CicstsManagerException 
    */
   @Test
   public void testResourceLibrary() throws CicstsManagerException {
      String resourceType = "LIBRARY";
      String resourceParameters = "DSNAME01(CTS.USER.APPL1.CICS.LOAD)";
     
      logger.info("Now checking that the Library " + libName + " does not already exist");
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(false);

      logger.info("Now creating the Library " + libName + " using CEDA, and checking that it does exist");
      cics.ceda().createResource(cedaTerminal, resourceType, libName, groupName1, resourceParameters);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(true);

      logger.info("Now installing the Library " + libName + " into the CICS using CEDA, and inquiring on it using CEMT");
      cics.ceda().installResource(cedaTerminal, resourceType, libName, groupName1);
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, libName).get("library")).isEqualToIgnoringCase(libName);

      logger.info("Now deleting the Library " + libName + " using CEDA, and checking that it does not exist");
      cics.ceda().deleteResource(cedaTerminal, resourceType, libName, groupName1);
      assertThat(cics.ceda().resourceExists(cedaTerminal, resourceType, libName, groupName1)).isEqualTo(false);
   }

   /**
    * Creating and installing resources in different groups with CEDA
    * Deleting a group with CEDA
    * 
    * @throws CedaException 
    * @throws CicstsManagerException 
    * @throws FieldNotFoundException 
    * @throws NetworkException 
    * @throws TerminalInterruptedException 
    * @throws KeyboardLockedException 
    * @throws TimeoutException 
    */
   @Test
   public void testGroup() throws CedaException, CicstsManagerException, TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
      String resourceType = "PROGRAM";
      
      logger.info("Creating resources in the first Group " + groupName1);
      cics.ceda().createResource(cedaTerminal, resourceType, programName1, groupName1, null);
      
      logger.info("Creating resources in the second Group " + groupName2);
      cics.ceda().createResource(cedaTerminal, resourceType, programName2, groupName2, null);
     
      logger.info("Installing only Group " + groupName1 + ". Testing that Group " + groupName1 + " appears in CEMT and Group " + groupName2 + " does not");
      cics.ceda().installGroup(cedaTerminal, groupName1);     
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName1)).isNotNull();
      assertThat(cics.cemt().inquireResource(cemtTerminal, resourceType, programName2)).isNull();

      logger.info("Testing CEDA group delete on Group " + groupName1);
      cics.ceda().deleteGroup(cedaTerminal, groupName1);
      
      logger.info("Manually testing that group delete worked using CEDA EXPAND. Should state that the Group " + groupName1 + " is not found");
      cedaTerminal.type("CEDA EXPAND GROUP(" + groupName1 + ")").enter().waitForKeyboard();
      assertThat(cedaTerminal.retrieveScreen().contains("Group " + groupName1 + " not found")).isTrue();
      
      // Installing a Group after deleting it should throw an exception
      assertThatThrownBy(() -> {
         cics.ceda().installGroup(cedaTerminal, groupName1);
      }).isInstanceOf(CedaException.class).hasMessageContaining("Problem determining the result from the CEDA command");
   }

}