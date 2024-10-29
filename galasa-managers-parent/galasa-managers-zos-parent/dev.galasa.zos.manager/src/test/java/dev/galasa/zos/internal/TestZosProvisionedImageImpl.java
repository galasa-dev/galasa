/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicResource;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.properties.ImageMaxSlots;
import dev.galasa.zos.internal.properties.ZosPropertiesSingleton;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({AbstractManager.class, ZosIpHostImpl.class, ImageMaxSlots.class, LogFactory.class, ZosPropertiesSingleton.class, CpsProperties.class})
public class TestZosProvisionedImageImpl {

//    private ZosProvisionedImageImpl zosProvisionedImage;
//    
//    private ZosProvisionedImageImpl zosProvisionedImageSpy;
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private IConfigurationPropertyStoreService cpsMock;
//    
//    @Mock
//    private IDynamicStatusStoreService dssMock;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private ICredentialsService credentialsServiceMock;
//
//    @Mock
//    private IDynamicResource dynamicResourceMock;
//    
//    @Mock
//    private IConfigurationPropertyStoreService configurationPropertyStoreServiceMock;
//
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//    
//    private static final String IMAGE_ID = "image";
//
//    private static final String CLUSTER_ID = "cluster";
//
//    private static final String PLEX_ID = "sysplex";
//
//    private static final String DEFAULT_CREDENTIALS_ID = "credentials";
//
//    private static final String IPV4_HOSTNAME = "ipv4.hostname";
//
//    private static final String IPV6_HOSTNAME = "ipv6.hostname";
//
//    private static final String RUN_NAME = "RUN-NAME";
//
//    private static final String SLOT_NAME = "SLOT-NAME";
//    
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
//        logMessage = null;
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
//        Mockito.doAnswer(answer).when(logMock).info(Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).warn(Mockito.any(), Mockito.any());
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        
//        PowerMockito.when(zosManagerMock.getCPS()).thenReturn(cpsMock);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(PLEX_ID))).thenReturn(PLEX_ID);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString())).thenReturn(DEFAULT_CREDENTIALS_ID);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV4_HOSTNAME))).thenReturn(IPV4_HOSTNAME);
//        PowerMockito.when(cpsMock.getProperty(Mockito.anyString(), ArgumentMatchers.contains(IPV6_HOSTNAME))).thenReturn(IPV6_HOSTNAME);
//        
//        
//        PowerMockito.when(dssMock.getDynamicResource(Mockito.any())).thenReturn(dynamicResourceMock);
//        PowerMockito.when(zosManagerMock.getDSS()).thenReturn(dssMock);
//        PowerMockito.when(zosManagerMock.getFramework()).thenReturn(frameworkMock);
//        PowerMockito.when(frameworkMock.getCredentialsService()).thenReturn(credentialsServiceMock);
//        PowerMockito.when(frameworkMock.getTestRunName()).thenReturn(RUN_NAME);
//        
//        PowerMockito.mockStatic(AbstractManager.class);
//        PowerMockito.doReturn(PLEX_ID).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(PLEX_ID));
//        PowerMockito.doReturn(DEFAULT_CREDENTIALS_ID).when(AbstractManager.class, "defaultString", ArgumentMatchers.contains(DEFAULT_CREDENTIALS_ID), Mockito.anyString());
//        PowerMockito.doReturn(IPV4_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV4_HOSTNAME));
//        PowerMockito.doReturn(IPV6_HOSTNAME).when(AbstractManager.class, "nulled", ArgumentMatchers.contains(IPV6_HOSTNAME));
//        PowerMockito.spy(ZosPropertiesSingleton.class);
//        PowerMockito.doReturn(configurationPropertyStoreServiceMock).when(ZosPropertiesSingleton.class, "cps");
//        PowerMockito.spy(CpsProperties.class);
//        PowerMockito.doReturn(IMAGE_ID).when(CpsProperties.class, "getStringWithDefault", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        PowerMockito.mockStatic(ImageMaxSlots.class);
//        
//        zosProvisionedImage = new ZosProvisionedImageImpl(zosManagerMock, IMAGE_ID, CLUSTER_ID);
//        zosProvisionedImageSpy = PowerMockito.spy(zosProvisionedImage);
//    }
//    
//    @Test
//    public void testHasCapacity() throws Exception {
//        Mockito.doReturn(0.9f).when(zosProvisionedImageSpy).getCurrentUsage();
//        Assert.assertTrue("hasCapacity() should return true", zosProvisionedImageSpy.hasCapacity());
//        
//        Mockito.doReturn(1.1f).when(zosProvisionedImageSpy).getCurrentUsage();
//        Assert.assertFalse("hasCapacity() should return true", zosProvisionedImageSpy.hasCapacity());
//    }
//    
//    @Test
//    public void testGetCurrentUsage() throws Exception {
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(0);
//        Assert.assertEquals("getCurrentUsage() should return the expected value", (Float) 1.0f, zosProvisionedImageSpy.getCurrentUsage());
//        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(1);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn(null);
//        Assert.assertEquals("getCurrentUsage() should return the expected value", (Float) 0.0f, zosProvisionedImageSpy.getCurrentUsage());
//        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(1);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("1");
//        Assert.assertEquals("getCurrentUsage() should return the expected value", (Float) 1.0f, zosProvisionedImageSpy.getCurrentUsage());
//    }
//    
//    @Test
//    public void testGetCurrentUsageException() throws Exception {        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(1);
//        PowerMockito.when(dssMock.get(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
//        
//        String expectedMessage = "Problem finding used slots for zOS Image " + IMAGE_ID;
//        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
//        	zosProvisionedImageSpy.getCurrentUsage();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAllocateImage() throws Exception {
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(0);
//        Assert.assertFalse("allocateImage() should return the false", zosProvisionedImageSpy.allocateImage());
//        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(0);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn(null);
//        Assert.assertFalse("allocateImage() should return the false", zosProvisionedImageSpy.allocateImage());
//        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(2);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("1").thenReturn("2");
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//        Assert.assertFalse("allocateImage() should return the false", zosProvisionedImageSpy.allocateImage());
//        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(2);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("1");
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        PowerMockito.when(dssMock.putSwap(Mockito.anyString(), Mockito.any(), ArgumentMatchers.contains(RUN_NAME), Mockito.any())).thenReturn(false).thenReturn(true);
//        Assert.assertTrue("allocateImage() should return the true", zosProvisionedImageSpy.allocateImage());
//    }
//    
//    @Test
//    public void testAllocateImageException() throws Exception {
//        PowerMockito.when(dssMock.get(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
//        String expectedMessage = "Problem finding used slots for zOS Image " + IMAGE_ID;
//        ZosManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosManagerException.class, ()->{
//        	zosProvisionedImageSpy.allocateImage();
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testGetSlotName() throws Exception {        
//        PowerMockito.when(ImageMaxSlots.get(Mockito.anyString())).thenReturn(2);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("1");
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        PowerMockito.when(dssMock.putSwap(Mockito.anyString(), Mockito.any(), ArgumentMatchers.contains(RUN_NAME), Mockito.any())).thenReturn(false).thenReturn(true);
//        zosProvisionedImageSpy.allocateImage();
//        
//        Assert.assertEquals("getSlotName() should return the expected value", "SLOT_" + RUN_NAME + "_1", zosProvisionedImageSpy.getSlotName());
//    }
//    
//    @Test
//    public void testFreeImage() throws Exception {
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn(null);
//        zosProvisionedImage.freeImage();
//        
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("1").thenReturn("0");
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(false).thenReturn(true);
//        Whitebox.setInternalState(zosProvisionedImage, "allocatedSlotName", SLOT_NAME);
//        zosProvisionedImage.freeImage();
//        Assert.assertEquals("freeImage() should log specified message", "Discard slot name " + SLOT_NAME + " for zOS Image " + IMAGE_ID, logMessage);
//
//        PowerMockito.when(dssMock.get(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
//        zosProvisionedImage.freeImage();
//        Assert.assertEquals("freeImage() should log specified message", "Failed to free slot on image " + IMAGE_ID + ", slot " + SLOT_NAME + ", leaving for manager clean up routines", logMessage);        
//    }
//    
//    @Test
//    public void testDeleteDss() throws Exception {
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn(null);
//        ZosProvisionedImageImpl.deleteDss(RUN_NAME, IMAGE_ID, SLOT_NAME, dssMock);
//        
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains("slot.run."))).thenReturn("active");        
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains("slot.run."), Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//        ZosProvisionedImageImpl.deleteDss(RUN_NAME, IMAGE_ID, SLOT_NAME, dssMock);
//        
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains("slot.run."))).thenReturn("active");        
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains("slot.run."), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
//        PowerMockito.when(dssMock.get(ArgumentMatchers.contains(".current.slots"))).thenReturn("0").thenReturn("1").thenReturn("0");
//        PowerMockito.when(dssMock.putSwap(ArgumentMatchers.contains(".current.slots"), Mockito.anyString(), Mockito.anyString())).thenReturn(false).thenReturn(true);
//        ZosProvisionedImageImpl.deleteDss(RUN_NAME, IMAGE_ID, SLOT_NAME, dssMock);
//        
//        PowerMockito.when(dssMock.get(Mockito.anyString())).thenThrow(new DynamicStatusStoreException());
//        ZosProvisionedImageImpl.deleteDss(RUN_NAME, IMAGE_ID, SLOT_NAME, dssMock);
//        Assert.assertEquals("deleteDss() should log specified message", "Failed to discard slot " + SLOT_NAME + " on image " + IMAGE_ID, logMessage);        
//    }
}
