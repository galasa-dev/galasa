/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsername;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.oeconsol.manager.internal.properties.OeconsolPath;
import dev.galasa.zosunixcommand.IZosUNIXCommand;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({OeconsolPath.class})
public class TestOeconsolZosConsoleImpl {
//    
//    private OeconsolZosConsoleImpl zosConsole;
//    
//    private OeconsolZosConsoleImpl zosConsoleSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private IZosManagerSpi zosManagerMock;
//    
//    @Mock
//    private IZosUNIXCommand zosUnixCommandMock;
//    
//    @Mock
//    private OeconsolZosConsoleManagerImpl zosConsoleManagerMock;
//    
//    @Mock
//    private ICredentialsUsername credentialsMock;
//    
//    @Mock
//    private ICredentialsToken credentialsTokenMock;
//
//    private static final String CONSOLE_COMMAND = "ZOS CONSOLE_COMMAND";
//
//    private static final String CONSOLE_NAME = "CNAME";
//
//    private static final String IMAGE_NAME = "IMAGE";
//    
//    @Before
//    public void setup() throws ZosConsoleManagerException {
//    	PowerMockito.mockStatic(OeconsolPath.class);
//        Mockito.when(OeconsolPath.get(Mockito.any())).thenReturn("/oeconsol");
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE_NAME);
//        
//        Mockito.when(zosConsoleManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosConsoleManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUnixCommandMock);
//
//        Mockito.when(zosConsoleManagerMock.getCredentials(Mockito.any(), Mockito.any())).thenReturn(credentialsMock);
//        
//        zosConsole = new OeconsolZosConsoleImpl(zosConsoleManagerMock, zosImageMock);
//        zosConsoleSpy = PowerMockito.spy(zosConsole);
//        
//    }
//    
//    @Test
//    public void testConstructorException() throws ZosConsoleManagerException {
//        Mockito.when(OeconsolPath.get(Mockito.any())).thenThrow(new ZosConsoleManagerException("exception"));
//        String expectedMessage = "exception";
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	new OeconsolZosConsoleImpl(zosConsoleManagerMock, zosImageMock);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIssueCommand() throws ZosConsoleException {
//        IZosConsoleCommand zosConsoleCommand = zosConsoleSpy.issueCommand(CONSOLE_COMMAND);
//        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());
//        
//        zosConsoleCommand = zosConsoleSpy.issueCommand(CONSOLE_COMMAND, CONSOLE_NAME);
//        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());
//
//        Mockito.when(zosConsoleManagerMock.getCredentials(Mockito.any(), Mockito.any())).thenReturn(null);
//        String expectedMessage = "Unable to get user credentials for console name " + CONSOLE_NAME;
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	zosConsoleSpy.issueCommand(CONSOLE_COMMAND, CONSOLE_NAME);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testToString() {
//    	Assert.assertEquals("toString() should return the default console name", IMAGE_NAME, zosConsoleSpy.toString());
//    }
}
