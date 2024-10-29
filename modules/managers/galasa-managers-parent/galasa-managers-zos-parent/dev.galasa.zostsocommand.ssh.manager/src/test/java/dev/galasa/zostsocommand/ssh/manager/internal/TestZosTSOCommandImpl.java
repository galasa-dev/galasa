/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.ICredentials;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.zos.IZosImage;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;

//@RunWith(PowerMockRunner.class)
public class TestZosTSOCommandImpl {
//    
//    private static final String FIXED_COMMAND = "command";
//
//	private static final String TSOCMD_PATH = "/tsocmd/path";
//    
//    private ZosTSOCommandImpl zosTSOCommand;
//    
//    private ZosTSOCommandImpl zosTSOCommandSpy;
//    
//    @Mock
//    private ZosUNIXCommandManagerImpl zosUnixManagerMock;
//    
//    @Mock
//    private IZosUNIXCommand zosUNIXCommandMock;
//    
//    @Mock
//    private IZosUNIXCommand zosTSOCommandMock;
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
//        Mockito.when(zosUnixManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUNIXCommandMock);
//        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenReturn(FIXED_COMMAND);
//        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenReturn(FIXED_COMMAND);
//
//        zosTSOCommand = new ZosTSOCommandImpl(zosUNIXCommandMock, TSOCMD_PATH);
//        zosTSOCommandSpy = Mockito.spy(zosTSOCommand);
//    }
//    
//    @Test
//    public void testIssueCommand() throws ZosTSOCommandException, ZosUNIXCommandException {
//        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand(FIXED_COMMAND));
//        
//        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand("\"" + FIXED_COMMAND + "\""));
//
//        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any())).thenThrow(new ZosUNIXCommandException());
//        String expectedMessage = "Unable to issue zOS TSO Command";
//        ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
//        	zosTSOCommandSpy.issueCommand(FIXED_COMMAND);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testIssueCommandTimout() throws ZosTSOCommandException, ZosUNIXCommandException {
//        Assert.assertEquals("Error in issueCommand() method", FIXED_COMMAND, zosTSOCommandSpy.issueCommand(FIXED_COMMAND, 1L));
//
//        Mockito.when(zosUNIXCommandMock.issueCommand(Mockito.any(), Mockito.anyLong())).thenThrow(new ZosUNIXCommandException());
//        String expectedMessage = "Unable to issue zOS TSO Command";
//        ZosTSOCommandException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandException.class, ()->{
//        	zosTSOCommandSpy.issueCommand(FIXED_COMMAND, 1L);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
}