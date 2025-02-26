/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.spi;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.imstm.ImstmManagerException;
import dev.galasa.imstm.internal.ImstmManagerImpl;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.properties.ApplyConfidentialTextFiltering;
import dev.galasa.zos3270.internal.properties.LiveTerminalUrl;
import dev.galasa.zos3270.internal.properties.LogConsoleTerminals;
import dev.galasa.zos3270.internal.properties.TerminalDeviceTypes;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;

@RunWith(MockitoJUnitRunner.class)
public class TestImsTerminalImpl {
    
    private ImsTerminalImpl terminal;

    @Mock private ImstmManagerImpl imsManager;
    @Mock private IFramework framework;
    @Mock private IImsSystem system;
    @Mock private ITextScannerManagerSpi textScanManager;
    @Mock private IZosImage zosImage;
    @Mock private IResultArchiveStore ras;
    @Mock private IIpHost ipHost;
    @Mock private IImsSystemLogonProvider logonProvider;
    List<IImsSystemLogonProvider> logonProviders;

    private static final String TERMID = "TEST_TERMINAL_ID";
    private static final String HOST = "my.test.host";
    private static final int PORT = 12345;
    private static final boolean SSL = true;
    private static final boolean AUTOCONNECT = true;
    private static final String CREDENTIALS_TAG = "TEST_CREDENTIALS";

    @Before
    public void setup() throws Exception {
        Mockito.when(system.getZosImage()).thenReturn(zosImage);
        Mockito.when(framework.getResultArchiveStore()).thenReturn(ras);
        Mockito.when(ras.getStoredArtifactsRoot()).thenReturn(FileSystems.getDefault().getPath("testdir"));   
        Mockito.when(ipHost.getHostname()).thenReturn(HOST);
        Mockito.when(ipHost.getTelnetPort()).thenReturn(PORT);
        Mockito.when(ipHost.isTelnetPortTls()).thenReturn(SSL);
        Mockito.when(zosImage.getIpHost()).thenReturn(ipHost);
        logonProviders = new ArrayList<IImsSystemLogonProvider>();
        logonProviders.add(logonProvider);
        Mockito.when(imsManager.getNextTerminalId(system)).thenReturn(TERMID);
        Mockito.when(imsManager.getLogonProviders()).thenReturn(logonProviders);
    }

    @Test
    public void testConstructor1() throws Exception{
        try (MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            Assert.assertEquals("Wrong host name passed to Zos3270TerminalImpl constructor",HOST, (String) arguments.get(0));
            Assert.assertEquals("Wrong port passed to Zos3270TerminalImpl constructor",PORT, (int) arguments.get(1));
            Assert.assertEquals("Wrong SSL flag passed to Zos3270TerminalImpl constructor",SSL, (boolean) arguments.get(2));
            // Set up an exception so that a network call (that we will trigger later in this test)
            // fails quickly.
            Mockito.when(mock.connectClient()).thenThrow(new NetworkException());
        });
        MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            TerminalSize ts = (TerminalSize) arguments.get(0);
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 24, ts.getRows());
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 80, ts.getColumns());
            ts = (TerminalSize) arguments.get(1);
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 0, ts.getRows());
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 0, ts.getColumns());
        });
        MockedStatic<ApplyConfidentialTextFiltering> filtering = Mockito.mockStatic(ApplyConfidentialTextFiltering.class);
        MockedStatic<LiveTerminalUrl> url = Mockito.mockStatic(LiveTerminalUrl.class);
        MockedStatic<TerminalDeviceTypes> deviceTypes = Mockito.mockStatic(TerminalDeviceTypes.class);
        MockedStatic<LogConsoleTerminals> consoles = Mockito.mockStatic(LogConsoleTerminals.class)) {
            terminal = new ImsTerminalImpl(imsManager, framework, system, HOST, PORT, SSL, AUTOCONNECT, textScanManager, CREDENTIALS_TAG);
            verifyConstructorActions(networks.constructed(), deviceTypes, screens.constructed());
            Assert.assertEquals("Wrong login credentials tag was saved", CREDENTIALS_TAG, terminal.getLoginCredentialsTag());
        }
    }

    @Test
    public void testConstructor2() throws Exception{
        try (MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            Assert.assertEquals("Wrong host name passed to Zos3270TerminalImpl constructor",HOST, (String) arguments.get(0));
            Assert.assertEquals("Wrong port passed to Zos3270TerminalImpl constructor",PORT, (int) arguments.get(1));
            Assert.assertEquals("Wrong SSL flag passed to Zos3270TerminalImpl constructor",SSL, (boolean) arguments.get(2));
            // Set up an exception so that a network call (that we will trigger later in this test)
            // fails quickly.
            Mockito.when(mock.connectClient()).thenThrow(new NetworkException());
        });
        MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            TerminalSize ts = (TerminalSize) arguments.get(0);
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 24, ts.getRows());
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 80, ts.getColumns());
            ts = (TerminalSize) arguments.get(1);
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 0, ts.getRows());
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 0, ts.getColumns());
        });
        MockedStatic<ApplyConfidentialTextFiltering> filtering = Mockito.mockStatic(ApplyConfidentialTextFiltering.class);
        MockedStatic<LiveTerminalUrl> url = Mockito.mockStatic(LiveTerminalUrl.class);
        MockedStatic<TerminalDeviceTypes> deviceTypes = Mockito.mockStatic(TerminalDeviceTypes.class);
        MockedStatic<LogConsoleTerminals> consoles = Mockito.mockStatic(LogConsoleTerminals.class)) {
            terminal = new ImsTerminalImpl(imsManager, framework, system, ipHost, AUTOCONNECT, textScanManager, CREDENTIALS_TAG);
            verifyConstructorActions(networks.constructed(), deviceTypes, screens.constructed());
            Assert.assertEquals("Wrong login credentials tag was saved", CREDENTIALS_TAG, terminal.getLoginCredentialsTag());
        }
    }

    @Test
    public void testConstructor3() throws Exception{
        try (MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            Assert.assertEquals("Wrong host name passed to Zos3270TerminalImpl constructor",HOST, (String) arguments.get(0));
            Assert.assertEquals("Wrong port passed to Zos3270TerminalImpl constructor",PORT, (int) arguments.get(1));
            Assert.assertEquals("Wrong SSL flag passed to Zos3270TerminalImpl constructor",SSL, (boolean) arguments.get(2));
            // Set up an exception so that a network call (that we will trigger later in this test)
            // fails quickly.
            Mockito.when(mock.connectClient()).thenThrow(new NetworkException());
        });
        MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            TerminalSize ts = (TerminalSize) arguments.get(0);
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 24, ts.getRows());
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 80, ts.getColumns());
            ts = (TerminalSize) arguments.get(1);
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 0, ts.getRows());
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 0, ts.getColumns());
        });
        MockedStatic<ApplyConfidentialTextFiltering> filtering = Mockito.mockStatic(ApplyConfidentialTextFiltering.class);
        MockedStatic<LiveTerminalUrl> url = Mockito.mockStatic(LiveTerminalUrl.class);
        MockedStatic<TerminalDeviceTypes> deviceTypes = Mockito.mockStatic(TerminalDeviceTypes.class);
        MockedStatic<LogConsoleTerminals> consoles = Mockito.mockStatic(LogConsoleTerminals.class)) {
            terminal = new ImsTerminalImpl(imsManager, framework, system, AUTOCONNECT, textScanManager, CREDENTIALS_TAG);
            verifyConstructorActions(networks.constructed(), deviceTypes, screens.constructed());
            Assert.assertEquals("Wrong login credentials tag was saved", CREDENTIALS_TAG, terminal.getLoginCredentialsTag());
        }
    }

    @Test
    public void testConstructor4() throws Exception{
        try (MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            Assert.assertEquals("Wrong host name passed to Zos3270TerminalImpl constructor",HOST, (String) arguments.get(0));
            Assert.assertEquals("Wrong port passed to Zos3270TerminalImpl constructor",PORT, (int) arguments.get(1));
            Assert.assertEquals("Wrong SSL flag passed to Zos3270TerminalImpl constructor",SSL, (boolean) arguments.get(2));
            // Set up an exception so that a network call (that we will trigger later in this test)
            // fails quickly.
            Mockito.when(mock.connectClient()).thenThrow(new NetworkException());
        });
        MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class, (mock, context) -> {
            List<?> arguments = context.arguments();
            TerminalSize ts = (TerminalSize) arguments.get(0);
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 24, ts.getRows());
            Assert.assertEquals("Primary terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 80, ts.getColumns());
            ts = (TerminalSize) arguments.get(1);
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of rows", 0, ts.getRows());
            Assert.assertEquals("Alternate terminal size passed to Zos3270TerminalImpl constructor has wrong number of columns", 0, ts.getColumns());
        });
        MockedStatic<ApplyConfidentialTextFiltering> filtering = Mockito.mockStatic(ApplyConfidentialTextFiltering.class);
        MockedStatic<LiveTerminalUrl> url = Mockito.mockStatic(LiveTerminalUrl.class);
        MockedStatic<TerminalDeviceTypes> deviceTypes = Mockito.mockStatic(TerminalDeviceTypes.class);
        MockedStatic<LogConsoleTerminals> consoles = Mockito.mockStatic(LogConsoleTerminals.class)) {
            terminal = new ImsTerminalImpl(imsManager, framework, system, AUTOCONNECT, textScanManager);
            verifyConstructorActions(networks.constructed(), deviceTypes, screens.constructed());
            Assert.assertEquals("Wrong login credentials tag was saved", "", terminal.getLoginCredentialsTag());
        }
    }

    @Test
    public void testConnectToImsSystemNoLogonProvider() throws Exception {
        instantiateTerminal();
        Mockito.when(imsManager.getLogonProviders()).thenReturn(Collections.emptyList());
        String expectedMessage = "Missing an IMS TM logon provider, none have been registered";
        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            terminal.connectToImsSystem();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testConnectToImsSystemSuccess() throws Exception {
        instantiateTerminal();
        Mockito.when(logonProvider.logonToImsSystem(terminal)).thenReturn(true);
        terminal.connectToImsSystem();
        Mockito.verify(logonProvider).logonToImsSystem(terminal);
    }

    @Test
    public void testConnectToImsSystemException() throws Exception {
        instantiateTerminal();
        Mockito.when(logonProvider.logonToImsSystem(terminal)).thenThrow(new ImstmManagerException());
        String expectedMessage = "Failed to connect terminal";
        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
            terminal.connectToImsSystem();
        });
    	Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
        Mockito.verify(logonProvider).logonToImsSystem(terminal);
    }

    @Test
    public void testResetAndClearSuccess() throws Exception {
        try (MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class);
                MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class)) {
            instantiateTerminal();
            Mockito.when(screens.constructed().get(0).isTextInField("DFS249")).thenReturn(true);
            IImsTerminal spyTerminal = Mockito.spy(terminal);
            Assert.assertEquals("Different terminal returned from resetAndClear()", spyTerminal, spyTerminal.resetAndClear());
            InOrder inOrder = Mockito.inOrder(spyTerminal);
            inOrder.verify(spyTerminal).resetAndClear();
            inOrder.verify(spyTerminal).clear();
            // wfk() calls waitForKeyboard() under the covers so we need to verify both
            inOrder.verify(spyTerminal).wfk();
            inOrder.verify(spyTerminal).waitForKeyboard();
            inOrder.verify(spyTerminal).enter();
            inOrder.verify(spyTerminal).wfk();
            inOrder.verify(spyTerminal).waitForKeyboard();
            inOrder.verify(spyTerminal).isTextInField("DFS249");
            inOrder.verify(spyTerminal).clear();
            inOrder.verify(spyTerminal).wfk();
            inOrder.verify(spyTerminal).waitForKeyboard();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    public void testResetAndClearFail() throws Exception {
        try (MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class);
                MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class)) {
            instantiateTerminal();
            Mockito.when(screens.constructed().get(0).isTextInField("DFS249")).thenReturn(false);
            IImsTerminal spyTerminal = Mockito.spy(terminal);
            Mockito.doReturn(spyTerminal).when(spyTerminal).clear();
            Mockito.when(spyTerminal.wfk()).thenReturn(spyTerminal);
            Mockito.doReturn(spyTerminal).when(spyTerminal).enter();
            String expectedMessage1 = "Unable to reset the IMS TM screen";
            String expectedMessage2 = "Unable to locate the native IMS TM screen";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
                spyTerminal.resetAndClear();
            });
            Assert.assertEquals("Exception should contain expected message", expectedMessage1, expectedException.getMessage());
            Assert.assertEquals("Cause should contain expected message", expectedMessage2, expectedException.getCause().getMessage());
        }
    }

    @Test
    public void testResetAndClearException() throws Exception {
        try (MockedConstruction<Screen> screens = Mockito.mockConstruction(Screen.class);
                MockedConstruction<Network> networks = Mockito.mockConstruction(Network.class)) {
            instantiateTerminal();
            Mockito.doThrow(new NetworkException()).when(networks.constructed().get(0)).sendDatastream(Mockito.any());
            String expectedMessage = "Unable to reset the IMS TM screen";
            ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ImstmManagerException.class, ()->{
                terminal.resetAndClear();
            });
            Assert.assertEquals("Exception should contain expected message", expectedMessage, expectedException.getMessage());
            Assert.assertTrue("Cause is not our injected exception", expectedException.getCause() instanceof NetworkException);
        }
    }

    private void instantiateTerminal() throws Exception{
        try (MockedStatic<ApplyConfidentialTextFiltering> filtering = Mockito.mockStatic(ApplyConfidentialTextFiltering.class);
        MockedStatic<LiveTerminalUrl> url = Mockito.mockStatic(LiveTerminalUrl.class);
        MockedStatic<TerminalDeviceTypes> deviceTypes = Mockito.mockStatic(TerminalDeviceTypes.class);
        MockedStatic<LogConsoleTerminals> consoles = Mockito.mockStatic(LogConsoleTerminals.class)) {
            terminal = new ImsTerminalImpl(imsManager, framework, system, AUTOCONNECT, textScanManager);
        }
    }

    private void verifyConstructorActions(List<Network> networks, MockedStatic<TerminalDeviceTypes> deviceTypes, List<Screen> screens) throws NetworkException {
        Assert.assertEquals("Wrong terminal id is set", TERMID, terminal.getId());
        // Verifying that a network was constructed confirms that the asserts in the Network
        // MockedConstruction were executed
        Assert.assertEquals("Wrong number of networks created",1, networks.size());
        Assert.assertEquals("Wrong 'Connect at startup' value passed to Zos3270TerminalImpl",false, terminal.doAutoConnect());
        // The following verify confirms that the correct framework was passed to Zos3270TerminalImpl
        Mockito.verify(framework).getTestRunName();  
        // The following verify confirms that the correct zOS image was passed to Zos3270TerminalImpl
        deviceTypes.verify(() -> TerminalDeviceTypes.get(zosImage));
        // Verifying that a screen was constructed confirms that the asserts in the Screen
        // MockedConstruction were executed
        Assert.assertEquals("Wrong number of screens created",1, screens.size());
        Assert.assertEquals("Wrong text scan manager is set", textScanManager, terminal.textScan);

        Assert.assertEquals("Wrong IMS System was saved", system, terminal.getImsSystem());
        // Saving of correct IMS TM Manager will be verified in tests that use the saved value
        Assert.assertEquals("Wrong autoconnect flag was saved", AUTOCONNECT, terminal.isConnectAtStartup());
    
        // The only way to verify that the setAutoConnect logic is called with the correct value
        // is to call networkClosed() and verify that the autoconnect logic is driven.
        terminal.networkClosed();
        Mockito.verify(networks.get(0)).connectClient();
}
}
