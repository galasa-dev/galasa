/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.JsonObject;

import dev.galasa.ICredentialsToken;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

//@RunWith(PowerMockRunner.class)
public class TestZosmfZosConsoleImpl {
//    
//    private ZosmfZosConsoleImpl zosConsole;
//    
//    private ZosmfZosConsoleImpl zosConsoleSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private IZosManagerSpi zosManagerMock;
//    
//    @Mock
//    private ZosmfZosConsoleManagerImpl zosConsoleManagerMock;
//    
//    @Mock
//    private IZosmfManagerSpi zosmfManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMock;
//    
//    @Mock
//    private ICredentialsUsernamePassword credentialsUsernamePasswordMock;
//    
//    @Mock
//    private ICredentialsToken credentialsTokenMock;
//
//    private static final String CONSOLE_COMMAND = "ZOS CONSOLE_COMMAND";
//
//    private static final String CONSOLE_NAME = "CNAME";
//
//    private static final String IMAGE_NAME = "image";
//    
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE_NAME);
//
//        Mockito.when(zosConsoleManagerMock.getZosmfManager()).thenReturn(zosmfManagerMock);
//        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfApiProcessorMock);
//        Mockito.when(zosConsoleManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("cmd-response", "cmd-response");
//        jsonObject.addProperty("cmd-response-key", "cmd-response-key");
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(jsonObject );
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosConsole = new ZosmfZosConsoleImpl(zosImageMock, zosConsoleManagerMock);
//        zosConsoleSpy = PowerMockito.spy(zosConsole);
//        
//    }
//    
//    @Test
//    public void testConstructorException() throws ZosmfManagerException {
//        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfManagerException("exception"));
//        String expectedMessage = "exception";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	new ZosmfZosConsoleImpl(zosImageMock, zosConsoleManagerMock);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testIssueCommand() throws Exception {
//        PowerMockito.doReturn(CONSOLE_NAME).when(zosConsoleSpy, "consoleName", Mockito.any());
//        IZosConsoleCommand zosConsoleCommand = zosConsoleSpy.issueCommand(CONSOLE_COMMAND);
//
//        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());
//    }
//    
//    @Test
//    public void testIssueCommandConsoleName() throws ZosConsoleException, ZosmfException {        
//        IZosConsoleCommand zosConsoleCommand = zosConsole.issueCommand(CONSOLE_COMMAND, CONSOLE_NAME);
//
//        Assert.assertEquals("IZosConsoleCommand.getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommand.getCommand());        
//    }
//
//    @Test
//    public void testConsoleName() throws Exception {
//         Assert.assertEquals("setConsoleName() should return " + CONSOLE_NAME, CONSOLE_NAME, zosConsole.consoleName(CONSOLE_NAME));
//         
//         PowerMockito.doReturn(credentialsUsernamePasswordMock).when(zosImageMock, "getDefaultCredentials");
//         PowerMockito.doReturn("USERID").when(credentialsUsernamePasswordMock, "getUsername");
//         Assert.assertEquals("setConsoleName() should return the default console name", "USERID", zosConsoleSpy.consoleName(null));
//    }
//
//    @Test
//    public void testConsoleNameExceptionZosManagerException() throws Exception {
//         PowerMockito.doThrow(new ZosManagerException()).when(zosImageMock, "getDefaultCredentials");
//         String expectedMessage = "Unable to get the run username for image image";
//         ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	 zosConsoleSpy.consoleName(null);
//         });
//         Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testConsoleNameExceptionNotICredentialsUsernamePassword() throws Exception {
//        PowerMockito.doReturn(credentialsTokenMock).when(zosImageMock, "getDefaultCredentials");
//        String expectedMessage = "Unable to get the run username for image image";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	zosConsoleSpy.consoleName(null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testConsoleNameExceptionTooShort() throws ZosConsoleException {
//        String consoleName = "1";
//        String expectedMessage = "Invalid console name \"" + consoleName + "\" must be between 2 and 8 characters long";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	zosConsole.consoleName(consoleName);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testConsoleNameExceptionTooLong() throws ZosConsoleException {
//        String consoleName = "123456789";
//        String expectedMessage = "Invalid console name \"" + consoleName + "\" must be between 2 and 8 characters long";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	zosConsole.consoleName(consoleName);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testToString() {
//    	Assert.assertEquals("toString() should return the default console name", IMAGE_NAME, zosConsoleSpy.toString());
//    }
}
