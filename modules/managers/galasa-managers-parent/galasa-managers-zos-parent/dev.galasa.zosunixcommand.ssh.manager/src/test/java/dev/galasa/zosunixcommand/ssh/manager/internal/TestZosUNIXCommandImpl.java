/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsername;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.IpNetworkManagerException;
import dev.galasa.ipnetwork.SSHAuthFailException;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

//@RunWith(PowerMockRunner.class)
public class TestZosUNIXCommandImpl {
//    
//    private static final String FIXED_COMMAND = "command";
//
//    private static final String IMAGE = "image";
//    
//    private ZosUNIXCommandImpl zosUNIXCommand;
//    
//    private ZosUNIXCommandImpl zosUNIXCommandSpy;
//
//    @Mock
//    private IpNetworkManagerImpl ipNetworkManagerMock;
//    
//    @Mock
//    private ICommandShell commandShellMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private ICredentials credentialsMock;
//    
//    @Mock
//    private IIpHost hostMock;
//
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//
//        zosUNIXCommand = new ZosUNIXCommandImpl(ipNetworkManagerMock, zosImageMock);
//        zosUNIXCommandSpy = Mockito.spy(zosUNIXCommand);
//    }
//    
//    @Test
//    public void testIssueCommand() throws ZosUNIXCommandException, IpNetworkManagerException {
//        Mockito.doReturn(credentialsMock).when(zosUNIXCommandSpy).getDefaultCredentials();
//        Mockito.doReturn(commandShellMock).when(zosUNIXCommandSpy).getCommandShell(Mockito.any());
//        Mockito.doReturn(FIXED_COMMAND).when(commandShellMock).issueCommand(Mockito.any());
//        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXCommandSpy.issueCommand(FIXED_COMMAND));
//        
//        Mockito.when(commandShellMock.issueCommand(Mockito.any())).thenThrow(new IpNetworkManagerException());
//        String expectedMessage = "Unable to issue zOS UNIX Command";
//        ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
//        	zosUNIXCommandSpy.issueCommand(FIXED_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIssueCommandTimout() throws ZosUNIXCommandException, IpNetworkManagerException {
//        Mockito.doReturn(credentialsMock).when(zosUNIXCommandSpy).getDefaultCredentials();
//        Mockito.doReturn(commandShellMock).when(zosUNIXCommandSpy).getCommandShell(Mockito.any());
//        Mockito.doReturn(FIXED_COMMAND).when(commandShellMock).issueCommand(Mockito.any(), Mockito.anyLong());
//        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosUNIXCommandSpy.issueCommand(FIXED_COMMAND, 1L));
//        
//        Mockito.when(commandShellMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new IpNetworkManagerException());
//        String expectedMessage = "Unable to issue zOS UNIX Command";
//        ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
//        	zosUNIXCommandSpy.issueCommand(FIXED_COMMAND, 1L);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//	public void testGetDefaultCredentials() throws ZosManagerException {
//		Whitebox.setInternalState(zosUNIXCommandSpy, "defaultCredentials", (ICredentials) null);
//	    Mockito.doReturn(credentialsMock).when(zosImageMock).getDefaultCredentials();
//		Assert.assertEquals("Error in getDefaultCredentials() method", credentialsMock, zosUNIXCommandSpy.getDefaultCredentials());
//	
//		Assert.assertEquals("Error in getDefaultCredentials() method", credentialsMock, zosUNIXCommandSpy.getDefaultCredentials());
//	
//		Whitebox.setInternalState(zosUNIXCommandSpy, "defaultCredentials", (ICredentials) null);
//	    Mockito.when(zosImageMock.getDefaultCredentials()).thenThrow(new ZosManagerException());
//	    String expectedMessage = "Unable to get default credentials for image " + IMAGE;
//	    ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
//	    	zosUNIXCommandSpy.getDefaultCredentials();
//	    });
//		Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//	}
//
//	@Test
//    public void testGetCommandShell() throws IpNetworkManagerException, ZosUNIXCommandException {
//        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenReturn(commandShellMock);
//        Mockito.doNothing().when(commandShellMock).reportResultStrings(Mockito.anyBoolean());
//        Assert.assertEquals("Error in getCommandShell() method", commandShellMock, zosUNIXCommandSpy.getCommandShell(credentialsMock));
//        
//        Mockito.doThrow(new SSHAuthFailException()).when(commandShellMock).connect();
//        Mockito.doReturn(" - authentication failed").when(zosUNIXCommandSpy).logAuthFail(Mockito.any());
//        String expectedMessage = "Unable to issue zOS UNIX Command - authentication failed";
//        ZosUNIXCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
//        	zosUNIXCommandSpy.getCommandShell(credentialsMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    	
//        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenThrow(new IpNetworkManagerException());
//        expectedMessage = "Unable to issue zOS UNIX Command";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandException.class, ()->{
//        	zosUNIXCommandSpy.getCommandShell(credentialsMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//	
//	@Test
//	public void testLogAuthFail() {
//		String expectedString = " - authentication failed";
//    	Assert.assertEquals("exception should contain expected message", expectedString, zosUNIXCommandSpy.logAuthFail(credentialsMock));
//    	
//    	ICredentialsUsername credentialsMock1 = Mockito.mock(ICredentialsUsername.class);
//    	Mockito.when(credentialsMock1.getUsername()).thenReturn("USERNAME");
//		expectedString = " - user name 'USERNAME' not authenticated";
//    	Assert.assertEquals("exception should contain expected message", expectedString, zosUNIXCommandSpy.logAuthFail(credentialsMock1));
//	}
}