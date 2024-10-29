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
//import org.powermock.reflect.Whitebox;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
public class TestZosmfZosConsoleCommandImpl {
//    
//    private ZosmfZosConsoleCommandImpl zosConsoleCommand;
//    
//    private ZosmfZosConsoleCommandImpl zosConsoleCommandSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private ZosmfManagerImpl zosmfManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private IZosmfResponse zosmfResponseMock;
//
//    private static final String CONSOLE_COMMAND = "ZOS CONSOLE_COMMAND";
//
//    private static final String CONSOLE_NAME = "CNAME";
//
//    private static final String CONSOLE_RESOPNSE = "ZOS CONSOLE_RESPONSE";
//    
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//
//        Mockito.when(zosmfManagerMock.newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfApiProcessorMock);
//        
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenReturn(getJsonObject());
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_OK);
//        
//        zosConsoleCommand = new ZosmfZosConsoleCommandImpl(zosmfApiProcessorMock, CONSOLE_COMMAND, CONSOLE_NAME, zosImageMock);
//        zosConsoleCommandSpy = Mockito.spy(zosConsoleCommand);
//    }
//    
//    @Test
//    public void testIssueCommand() throws ZosConsoleException {
//        PowerMockito.when(zosConsoleCommandSpy.isRouteCommand()).thenReturn(false);
//        zosConsoleCommandSpy.issueCommand();
//        Assert.assertEquals("getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommandSpy.getCommand());
//        PowerMockito.when(zosConsoleCommandSpy.isRouteCommand()).thenReturn(true);
//        zosConsoleCommandSpy.issueCommand();
//        Assert.assertEquals("getCommand() should return the supplied value", CONSOLE_COMMAND, zosConsoleCommandSpy.getCommand());
//    }
//    
//    @Test
//    public void testIssueCommandException() throws ZosmfManagerException, ZosConsoleManagerException {
//        String expectedMessage = "exception";
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.PUT_JSON), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleCommand.issueCommand();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testIssueCommandException1() throws ZosConsoleException, ZosmfException {
//        String expectedMessage =  "Unable to issue console command \"" + CONSOLE_COMMAND + "\"";
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException("exception"));
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleCommand.issueCommand();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIssueCommandException2() throws ZosConsoleException {
//        String expectedMessage = "Console command \"" + CONSOLE_COMMAND + "\" failed. Reason \"reason\"";
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleCommand.issueCommand();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIssueCommandConsoleName() throws ZosConsoleException, ZosmfException {
//        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", CONSOLE_RESOPNSE);
//    
//        Assert.assertEquals("getResponse() should return " + CONSOLE_RESOPNSE, CONSOLE_RESOPNSE, zosConsoleCommandSpy.getResponse());        
//    }
//
//    @Test
//    public void testRequestResponse() throws ZosConsoleException, ZosmfException {
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        
//        Assert.assertEquals("requestResponse() should return the expected response", CONSOLE_RESOPNSE, zosConsoleCommand.requestResponse());
//    }
//
//    @Test
//    public void testRequestResponseNotFound() throws ZosConsoleException, ZosmfException {
//        Whitebox.setInternalState(zosConsoleCommandSpy, "commandDelayedResponse", "NONE");
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
//        
//        Assert.assertEquals("requestResponse() should return the expected response", "NONE", zosConsoleCommandSpy.requestResponse());
//    }
//
//    @Test
//    public void testRequestResponseException() throws ZosConsoleException, ZosmfException {
//        String expectedMessage = "exception";
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenThrow(new ZosmfException("exception"));
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleCommand.requestResponse();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//
//    @Test
//    public void testRequestResponseException1() throws ZosConsoleException, ZosmfException {
//        String expectedMessage =  "Unable to issue console command \"" + CONSOLE_COMMAND + "\"";
//        Mockito.when(zosmfApiProcessorMock.sendRequest(Mockito.eq(ZosmfRequestType.GET), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(zosmfResponseMock);
//        Mockito.when(zosmfResponseMock.getJsonContent()).thenThrow(new ZosmfException("exception"));
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleCommand.requestResponse();
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testLogUnableToIsuueCommand() {
//        Whitebox.setInternalState(zosConsoleCommandSpy, "command", CONSOLE_COMMAND);
//        String response = "Unable to issue console command \"" + CONSOLE_COMMAND + "\"";
//        
//        Assert.assertEquals("logUnableToIsuueCommand() should return the correct String", response, zosConsoleCommandSpy.logUnableToIsuueCommand());
//    }
//
//    @Test
//    public void testIsRouteCommand() throws ZosConsoleException {
//        Whitebox.setInternalState(zosConsoleCommandSpy, "command", CONSOLE_COMMAND);
//        Assert.assertFalse("isRouteCommand() should return false", zosConsoleCommandSpy.isRouteCommand());
//        Whitebox.setInternalState(zosConsoleCommandSpy, "command", "RO XX," + CONSOLE_COMMAND);
//        Assert.assertTrue("isRouteCommand() should return true", zosConsoleCommandSpy.isRouteCommand());
//        Whitebox.setInternalState(zosConsoleCommandSpy, "command", "ROUTE XX," + CONSOLE_COMMAND);
//        Assert.assertTrue("isRouteCommand() should return true", zosConsoleCommandSpy.isRouteCommand());
//    }
//
//    @Test
//    public void testToString() {
//        Whitebox.setInternalState(zosConsoleCommandSpy, "command", CONSOLE_COMMAND);
//        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", (String) null);
//        Whitebox.setInternalState(zosConsoleCommandSpy, "image", (String) null);
//        String response = "COMMAND=" + CONSOLE_COMMAND;
//        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());
//
//        Whitebox.setInternalState(zosConsoleCommandSpy, "image", zosImageMock);
//        response = response + " IMAGE=image";
//        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());
//
//        Whitebox.setInternalState(zosConsoleCommandSpy, "commandImmediateResponse", CONSOLE_RESOPNSE);
//        response = response + " RESPONSE:\n " + CONSOLE_RESOPNSE;
//        Assert.assertEquals("toString() should return the correct String", response, zosConsoleCommandSpy.toString());
//        
//        
//    }
//
//    private JsonObject getJsonObject() {
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("cmd-response", CONSOLE_RESOPNSE);
//        jsonObject.addProperty("cmd-response-key", "cmd-response-key");
//        jsonObject.addProperty("reason", "reason");
//        jsonObject.addProperty("return-code", 99);
//        jsonObject.addProperty("reason-code", 99);
//        return jsonObject;
//    }
}
