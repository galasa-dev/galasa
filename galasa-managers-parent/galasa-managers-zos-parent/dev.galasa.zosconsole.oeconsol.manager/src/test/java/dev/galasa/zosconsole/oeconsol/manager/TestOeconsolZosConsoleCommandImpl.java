/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ICredentialsUsername;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandAuthFailException;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestOeconsolZosConsoleCommandImpl {
//    
//    private OeconsolZosConsoleCommandImpl zosConsoleCommand;
//    
//    private OeconsolZosConsoleCommandImpl zosConsoleCommandSpy;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//	private IZosUNIXCommand zosUnixCommandMock;
//	
//    @Mock
//	private ICredentialsUsername credentialsMock;
//
//    private static final String CONSOLE_COMMAND = "ZOS CONSOLE_COMMAND";
//
//    private static final String CONSOLE_NAME = "CNAME";
//
//    private static final String CONSOLE_RESOPNSE = "ZOS CONSOLE_RESPONSE";
//    
//    private static final String IMAGE = "IMAGE";
//
//	private static final String OECONSOL_PATH = "/path/to/oeconsol";
//
//	private static final String USER_NAME = "USER";
//    
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        Answer<String> answer = new Answer<String>() {
//            @Override
//            public String answer(InvocationOnMock invocation) throws Throwable {
//                logMessage = invocation.getArgument(0);
//                System.err.println("Captured Log Message:\n" + logMessage);
//                if (invocation.getArguments().length > 1 && invocation.getArgument(1) instanceof Throwable) {
//                    ((Throwable) invocation.getArgument(1)).printStackTrace();
//                }
//                return null;
//            }
//        };
//        Mockito.doAnswer(answer).when(logMock).debug(Mockito.any());
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        Mockito.when(credentialsMock.getUsername()).thenReturn(USER_NAME);
//        Mockito.when(zosUnixCommandMock.issueCommand(Mockito.any())).thenReturn(CONSOLE_RESOPNSE);
//        Mockito.when(zosUnixCommandMock.issueCommand(Mockito.any(), Mockito.any())).thenReturn(CONSOLE_RESOPNSE);
//        
//        zosConsoleCommand = new OeconsolZosConsoleCommandImpl(zosUnixCommandMock, OECONSOL_PATH, zosImageMock.getImageID(), CONSOLE_COMMAND, CONSOLE_NAME, credentialsMock);
//        zosConsoleCommandSpy = Mockito.spy(zosConsoleCommand);
//    }
//    
//    @Test
//    public void testIssueCommand() throws ZosConsoleException, ZosUNIXCommandException {
//        zosConsoleCommandSpy.issueCommand();
//        String expectedMessage = "Issuing command '" + CONSOLE_COMMAND + "' on image '" + IMAGE + "'" + " with console name '" + CONSOLE_NAME + "'" + " using credentials for user name '" + USER_NAME + "'";
//		Assert.assertEquals("getCommand() should log expected message", expectedMessage, logMessage);
//		
//		Whitebox.setInternalState(zosConsoleCommandSpy, "consoleName", (String) null);		
//		Whitebox.setInternalState(zosConsoleCommandSpy, "credentials", (ICredentialsUsername) null);
//        zosConsoleCommandSpy.issueCommand();
//        expectedMessage = "Issuing command '" + CONSOLE_COMMAND + "' on image '" + IMAGE + "'" + " using default credentials";
//		Assert.assertEquals("getCommand() should log expected message", expectedMessage, logMessage);
//    }
//    
//    @Test
//    public void testIssueCommandException() throws ZosUNIXCommandException {		
//		Mockito.when(zosUnixCommandMock.issueCommand(Mockito.any(), Mockito.any())).thenThrow(new ZosUNIXCommandAuthFailException());
//        String expectedMessage = "Unable to issue console command '" + CONSOLE_COMMAND + "'" + " - user not authenticated";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//			zosConsoleCommandSpy.issueCommand();
//        });
//		Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//		
//		Whitebox.setInternalState(zosConsoleCommandSpy, "consoleName", (String) null);		
//		Whitebox.setInternalState(zosConsoleCommandSpy, "credentials", (ICredentialsUsername) null);		
//		Mockito.when(zosUnixCommandMock.issueCommand(Mockito.any())).thenThrow(new ZosUNIXCommandException(""));
//        expectedMessage = "Unable to issue console command '" + CONSOLE_COMMAND + "'";
//		expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//			zosConsoleCommandSpy.issueCommand();
//        });
//		Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testGetResponse() throws ZosConsoleException {   
//        zosConsoleCommandSpy.issueCommand();     
//        Assert.assertEquals("getResponse() should return the expected response", CONSOLE_RESOPNSE, zosConsoleCommandSpy.getResponse());
//    }
//
//    @Test
//    public void testRequestResponse() {
//        String expectedMessage = "oeconsol does not provide support for delayed response";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//			zosConsoleCommandSpy.requestResponse();
//        });
//		Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//
//    @Test
//    public void testGetCommand() {    
//        Assert.assertEquals("getCommand() should return the expected response", CONSOLE_COMMAND, zosConsoleCommandSpy.getCommand());
//    }
}
