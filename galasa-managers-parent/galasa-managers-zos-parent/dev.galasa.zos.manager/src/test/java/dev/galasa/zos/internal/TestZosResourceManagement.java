/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.modules.junit4.PowerMockRunner;

import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.ResourceManagerException;

//@RunWith(PowerMockRunner.class)
public class TestZosResourceManagement {

//    private ZosResourceManagement zosResourceManagement;
//    
//    private ZosResourceManagement zosResourceManagementSpy;
//    
//    @Mock
//    private ScheduledExecutorService scheduledExecutorServiceMock;
//    
//    @Mock
//    private Random randomMock;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private IResourceManagement resourceManagementMock;
//
//    private static final String RUN_NAME = "RUN-NAME";
//    
//    @Test
//    public void testZosResourceManagement() throws ResourceManagerException, DynamicStatusStoreException {
//        zosResourceManagement = new ZosResourceManagement();
//        zosResourceManagementSpy = PowerMockito.spy(zosResourceManagement);
//        
//        Assert.assertTrue("initialise() should return true", zosResourceManagementSpy.initialise(frameworkMock, resourceManagementMock));
//
//        Mockito.doReturn(scheduledExecutorServiceMock).when(resourceManagementMock).getScheduledExecutorService();
//        Mockito.when(frameworkMock.getRandom()).thenReturn(randomMock);
//        zosResourceManagementSpy.start();
//        
//        zosResourceManagementSpy.shutdown();
//        
//        zosResourceManagementSpy.runFinishedOrDeleted(RUN_NAME);
//        
//        PowerMockito.when(frameworkMock.getDynamicStatusStoreService(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
//        String expectedMessage = "Unable to initialise zOS resource monitor";
//        ResourceManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ResourceManagerException.class, ()->{
//        	zosResourceManagementSpy.initialise(frameworkMock, resourceManagementMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//
}
