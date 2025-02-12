/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos3270;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Test;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.RunName;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.Zos3270Terminal;
import dev.galasa.zos3270.spi.Colour;
import dev.galasa.zos3270.spi.Highlight;
import dev.galasa.zos3270.spi.NetworkException;

@Test
public class Zos3270IVT {

    @Logger
    public Log       logger;

    @ZosImage(imageTag = "PRIMARY")
    public IZosImage image;

    @Zos3270Terminal(imageTag = "PRIMARY")
    public ITerminal terminal;
    
    @CoreManager
    public ICoreManager coreManager;
    
	@RunName
    public String runName;
    
	private String credentialsId = "PRIMARY";
    private String applid = "IYK2ZNB5";

	@TestProperty(prefix = "IVT.REGION", suffix = "APPLID", required = false)
	public String cbsaApplid;

    @Test
    public void checkInjection() {
        assertThat(terminal.isConnected()).isTrue();
        logger.info("Using Run ID of: " + runName);
    }

    @Test
    public void logon() throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, TimeoutException, KeyboardLockedException, Zos3270Exception {
    	terminal.wfk().waitForTextInField("HIT ENTER FOR LATEST STATUS");
    	terminal.type("logon applid(" +applid+ ")").enter();
    	terminal.waitForTextInField("CICS FOR GALASA TEST").clear(); 	
    }
    
    @Test
    public void reconnectTest() throws TextNotFoundException, ErrorTextFoundException, TimeoutException, KeyboardLockedException, Zos3270Exception {
    	terminal.disconnect();
    	assertThat(terminal.isConnected()).isFalse();
    	terminal.connect();
    	logon();
    	assertThat(terminal.isConnected()).isTrue();
    }
    
    @Test
    public void clearScreenCheck() {
    	//we should be at a clear screen - so lets check that 
    	assertThat(terminal.isClearScreen()).isTrue();
    }
    
    @Test 
    public void cursorControlTest() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException, TextNotFoundException {
    	terminal.wfk().type("CEMT I SYS").enter().wfk();
    	//tab to the next two fields checking we get to the right place
    	terminal.tab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("00500");
    	terminal.tab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("04000");
    	//go back to the previous item
    	terminal.backTab();
    	assertThat(terminal.retrieveFieldAtCursor()).contains("00500");
    	
    	//going home and then down and right should take us to the STATUS: text
    	terminal.home();
    	terminal.cursorRight().cursorDown();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("STATUS: ");
    	
    	//re-position to the output of the Aging: field going up and left should take us to the System field
    	terminal.positionCursorToFieldContaining("00500");
    	terminal.cursorUp().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft().cursorLeft();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("System");
    	
    	//let's wrap around the terminal
    	terminal.home().cursorUp();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("PF");
    	terminal.cursorDown().cursorRight();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith(" I SYS");
    	
    	terminal.positionCursorToFieldContaining("PF").cursorLeft().cursorLeft().cursorLeft();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("TIME:");
    	terminal.cursorRight().cursorRight().cursorRight();
    	assertThat(terminal.retrieveFieldAtCursor()).startsWith("PF");
    	
    	terminal.pf3().wfk().clear().wfk();	
    }
    
    @Test
    public void mustBeABetterWayToTestEOF() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException, NetworkException, FieldNotFoundException {
    	//hear me out.........
    	//create a TSQ called Hobbit containing THE RING
    	terminal.type("CECI WRITEQ TS QNAME(HOBBIT" + runName + ") FROM('THE RING')").enter().wfk().enter().wfk().pf3().wfk().clear().wfk();
    	//try and browse the queue - but get the name wrong - no rings here
    	terminal.type("CEBR BILBOBAGGINS").enter().wfk();
    	assertThat(terminal.retrieveScreen()).doesNotContain("THE RING");
    	//so erase that and try the hobbit queue
    	terminal.home().eraseEof().type("HOBBIT"+runName).enter().wfk();
    	assertThat(terminal.retrieveScreen()).contains("THE RING");
    	//purge the queue so we can run again (YES I KNOW THIS WILL NOT  RUN IN PARALELL
    	terminal.home().tab().tab().type("purge").enter().wfk().pf3().wfk().clear().wfk();
    }
    
    @Test
    public void reportScreenTests() throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, KeyboardLockedException, NetworkException, FieldNotFoundException, Zos3270Exception {
    	terminal.type("CEDA").enter().wfk().waitForTextInField("ENTER ONE OF THE FOLLOWING");
    	terminal.reportScreen();
    	assertThat(terminal.isTextInField("DSN=CTS.YATESW.CICSNB5.CSD")).isTrue();
    	assertThat(terminal.retrieveScreen()).contains("DSN=CTS.YATESW.CICSNB5.CSD");
    	terminal.pf3().wfk().clear().wfk();
    	
    }
    
    @Test
    public void driveWaitForTextInField() throws TextNotFoundException, ErrorTextFoundException, Zos3270Exception {
    	terminal.type("CEDA").enter().wfk();
    	String [] okValues = {"FRODO","LOUIE","STATUS:","SESSION ENDED"};
    	String [] emptyValues = {};
    	int valueFound = terminal.pf3().waitForTextInField(okValues, emptyValues,5000);
    	assertThat(valueFound).isEqualTo(2);
    	String [] failValues = {"SESSION ENDED"};
    	String [] newValues = {"FRODO","LOUIE","STATUS:"};
    	try {
    		terminal.waitForTextInField(newValues, failValues, 5000);
    	}catch(ErrorTextFoundException etfe) {
    		logger.info("error text was correctly driven");
    		assertThat(etfe.getMessage()).contains("Found error text 'SESSION ENDED' on screen");
    	}
    	String [] thingsThatDontExist = {"Hello"};
    	try {
    		terminal.waitForTextInField(thingsThatDontExist, emptyValues,5000);
    	} catch (TextNotFoundException tnfe) {
    		logger.info("No text found exception correctly thrown");
    		assertThat(tnfe.getMessage()).contains("Unable to find a field containing any of the request text");
    	}
    }

	// TODO: Re-enable colour support IVTs once CBSA is installed on a region provisioned for the tests
	// @Test
	// public void cursorColourTest() throws Zos3270Exception, CoreManagerException {
	// 	ICredentialsUsernamePassword credentials = (ICredentialsUsernamePassword) coreManager.getCredentials(credentialsId);
	// 	coreManager.registerConfidentialText(credentials.getPassword(), "password");
	// 	terminal.disconnect();
	// 	terminal.connect();

	// 	terminal.wfk().type(image.getVtamLogonString(cbsaApplid)).enter().wfk().waitForTextInField("Signon to CICS");
	// 	terminal.wfk().type(credentials.getUsername()).tab().tab().type(credentials.getPassword()).enter().wfk();

	// 	// access CBSA and look up customer with ID 1
	// 	terminal.type("OMEN").enter().wfk().waitForTextInField("CICS Bank Sample Application");
	// 	terminal.type("1").enter().wfk().type("1").enter().wfk();
		
	// 	terminal.reportExtendedScreen(true, true, true, false, false, false, false);
	// 	assertThat(terminal.retrieveColourAtCursor()).isNull();

	// 	terminal.positionCursorToFieldContaining("CUSTOMER NUMBER").cursorRight();
	// 	assertThat(terminal.retrieveColourAtCursor()).isEqualTo(Colour.TURQUOISE);

	// 	terminal.positionCursorToFieldContaining("CICS Bank Sample Application").cursorRight();
	// 	assertThat(terminal.retrieveColourAtCursor()).isEqualTo(Colour.RED);

	// 	terminal.positionCursorToFieldContaining("Sort Code").cursorRight();
	// 	assertThat(terminal.retrieveColourAtCursor()).isEqualTo(Colour.NEUTRAL);
	// }
	
	// @Test
	// public void cursorHighlightingTest() throws Zos3270Exception {
	// 	// press f10 to switch to the view used to update customer information in CBSA
	// 	terminal.pf10().wfk();
	// 	terminal.reportExtendedScreen(true, true, true, false, false, false, false);
	// 	assertThat(terminal.retrieveHighlightAtCursor()).isEqualTo(Highlight.UNDERSCORE);

	// 	// the "Customer Number" field cannot be modified, so it is not highlighted
	// 	terminal.positionCursorToFieldContaining("Customer Number");
	// 	assertThat(terminal.retrieveHighlightAtCursor()).isNull();
	// }
}
