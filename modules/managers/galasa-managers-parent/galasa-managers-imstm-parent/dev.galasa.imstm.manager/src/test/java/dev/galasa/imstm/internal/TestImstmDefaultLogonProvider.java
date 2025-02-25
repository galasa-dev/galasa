/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

@RunWith(MockitoJUnitRunner.class)
public class TestImstmDefaultLogonProvider {
    
    private ImstmDefaultLogonProvider idlp; 
    @Mock
    private IFramework framework;
    @Mock
    private ICredentialsService cs;
    @Mock
    private IConfidentialTextService cts;
    @Mock
    private IImsTerminal terminal;
    @Mock
    private IImsSystem system;
    @Mock
    private IZosImage image;
    @Mock
    private ICredentialsUsername userCreds;
    @Mock
    private ICredentialsUsernamePassword userPassCreds;

    private static String LOGON_STRING = "TEST LOGON STRING";
    private static String APPLID = "APPLID";
    private static String USER = "USER";
    private static String PASS = "X1234567";
    private static String USERONLY = "USERONLY";
    private static String USERPASS = "USERPASS";
    private static String INIT_STRING = "TEST INITIAL TEXT";
    private static String SIGNON_SCREEN = "DFS3649A";
    private static String[] SIGNON_PASS = { "DFS3650I" };
    private static String[] SIGNON_FAIL = { "REJECTED" };

    @Before
    public void setup() throws Exception {
        Mockito.when(framework.getCredentialsService()).thenReturn(cs);
        Mockito.when(framework.getConfidentialTextService()).thenReturn(cts);
        Mockito.when(cs.getCredentials(USERONLY)).thenReturn(userCreds);
        Mockito.when(cs.getCredentials(USERPASS)).thenReturn(userPassCreds);
        Mockito.when(userCreds.getUsername()).thenReturn(USER);
        Mockito.when(userPassCreds.getUsername()).thenReturn(USER);
        Mockito.when(userPassCreds.getPassword()).thenReturn(PASS);
        Mockito.when(terminal.getImsSystem()).thenReturn(system);
        Mockito.when(system.getZosImage()).thenReturn(image);
        Mockito.when(system.getApplid()).thenReturn(APPLID);
        Mockito.when(image.getVtamLogonString(Mockito.anyString())).thenReturn(LOGON_STRING);
        Mockito.when(image.getLogonInitialText()).thenReturn(INIT_STRING);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(terminal.type(Mockito.anyString())).thenReturn(terminal);
        Mockito.when(terminal.enter()).thenReturn(terminal);
        Mockito.when(terminal.clear()).thenReturn(terminal);
    }

    @Test
    public void testConstructorNoCs() throws Exception {
        Mockito.when(framework.getCredentialsService()).thenThrow(new CredentialsException());
        String expectedMessage = "Could not obtain the Credentials service.";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
        	idlp = new ImstmDefaultLogonProvider(framework);
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemNoCredentials() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn("");
        String expectedMessage = "No login credentials provided";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextNoPassword() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        idlp.logonToImsSystem(terminal);
        verifyTerminalInteraction(true, false, false);
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextPassword() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERPASS);
        idlp.logonToImsSystem(terminal);
        verifyTerminalInteraction(true, false, true);
    }

    @Test
    public void testLogonToImsSystemNotConnectedTextNoPassword() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.isConnected()).thenReturn(false);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        idlp.logonToImsSystem(terminal);
        verifyTerminalInteraction(false, true, false);
    }

    @Test
    public void testLogonToImsSystemConnectedTextNotFound() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(INIT_STRING)).thenThrow(new TextNotFoundException());
        String expectedMessage = "Unable to logon to IMS, initial screen does not contain '" + INIT_STRING + "'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedTextInterrupted() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(INIT_STRING)).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to logon to IMS, initial screen does not contain '" + INIT_STRING + "'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedText3270Error() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(INIT_STRING)).thenThrow(new Zos3270Exception());
        String expectedMessage = "Unable to logon to IMS, initial screen does not contain '" + INIT_STRING + "'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextNoSignonScreen() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(SIGNON_SCREEN)).thenThrow(new TextNotFoundException());
        String expectedMessage = "Unable to wait for the initial IMS screen, looking for 'DFS3649A'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextConnectInterrupted() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(SIGNON_SCREEN)).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to wait for the initial IMS screen, looking for 'DFS3649A'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextConnect3270Error() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.waitForTextInField(SIGNON_SCREEN)).thenThrow(new Zos3270Exception());
        String expectedMessage = "Unable to wait for the initial IMS screen, looking for 'DFS3649A'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextSignonFail() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        Mockito.when(terminal.waitForTextInField(SIGNON_PASS, SIGNON_FAIL)).thenThrow(new ErrorTextFoundException(null, 0));
        String expectedMessage = "Unable to sign on, looking for 'DFS3650I'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextSignonUnexpectedFail() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        Mockito.when(terminal.waitForTextInField(SIGNON_PASS, SIGNON_FAIL)).thenThrow(new TextNotFoundException());
        String expectedMessage = "Unable to sign on, looking for 'DFS3650I'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextSignonInterrupted() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        Mockito.when(terminal.waitForTextInField(SIGNON_PASS, SIGNON_FAIL)).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to sign on, looking for 'DFS3650I'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testLogonToImsSystemConnectedNoTextSignon3270Error() throws Exception {
        idlp = new ImstmDefaultLogonProvider(framework);
        Mockito.when(terminal.getLoginCredentialsTag()).thenReturn(USERONLY);
        Mockito.when(terminal.waitForTextInField(SIGNON_PASS, SIGNON_FAIL)).thenThrow(new Zos3270Exception());
        String expectedMessage = "Unable to sign on, looking for 'DFS3650I'";
        ImstmManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            idlp.logonToImsSystem(terminal);
        });
        Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    private void verifyTerminalInteraction(boolean connected, boolean initialText, boolean password) throws Exception{
        InOrder inOrder = Mockito.inOrder(terminal);
        if (!connected) {
            inOrder.verify(terminal).connect();
        }
        inOrder.verify(terminal).waitForKeyboard();
        if (initialText) {
            inOrder.verify(terminal).waitForTextInField(INIT_STRING);
        }
        inOrder.verify(terminal).type(LOGON_STRING);
        inOrder.verify(terminal).enter();
        inOrder.verify(terminal).wfk();
        inOrder.verify(terminal).waitForTextInField(SIGNON_SCREEN);
        inOrder.verify(terminal).positionCursorToFieldContaining("USERID:");
        inOrder.verify(terminal).tab();
        inOrder.verify(terminal).type(USER);
        if (password) {
            inOrder.verify(terminal).positionCursorToFieldContaining("PASSWORD:");
            inOrder.verify(terminal).tab();
            inOrder.verify(terminal).type(PASS);
        }
        inOrder.verify(terminal).enter();
        inOrder.verify(terminal).wfk();
        inOrder.verify(terminal).waitForTextInField(new String[] { "DFS3650I" }, new String[] { "REJECTED"});
        inOrder.verify(terminal).clear();
        inOrder.verify(terminal).wfk();

    }
}
