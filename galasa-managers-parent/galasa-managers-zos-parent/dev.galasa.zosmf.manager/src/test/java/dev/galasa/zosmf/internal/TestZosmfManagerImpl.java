/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.mockito.internal.util.reflection.FieldSetter;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.internal.HttpManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.Https;
import dev.galasa.zosmf.internal.properties.ImageServers;
import dev.galasa.zosmf.internal.properties.RequestRetry;
import dev.galasa.zosmf.internal.properties.ServerImage;
import dev.galasa.zosmf.internal.properties.ServerPort;
import dev.galasa.zosmf.internal.properties.SysplexServers;
import dev.galasa.zosmf.internal.properties.ZosmfPropertiesSingleton;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({SysplexServers.class, ServerPort.class, ImageServers.class, Https.class, RequestRetry.class})
public class TestZosmfManagerImpl {
//
//    private ZosmfManagerImpl zosmfManager;
//    
//    private ZosmfManagerImpl zosmfManagerSpy;
//    
//    private ZosmfPropertiesSingleton zosmfZosmfPropertiesSingleton;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
//
//    @Mock
//    private IConfigurationPropertyStoreService cps;
//
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private IResultArchiveStore resultArchiveStoreMock;
//    
//    @Mock
//    public IManager managerMock;
//    
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private HttpManagerImpl httpManagerMock;
//    
//    @Mock
//    private IHttpClient httpClientMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private ZosmfImpl zosmfMock;
//
//    private static final String SERVER_ID = "SERVER1";
//
//    private static final String IMAGE = "image";
//
//    private static final String IMAGE_TAG = "tag";
//
//    private static final int PORT = 999;
//
//    @Before
//    public void setup() throws Exception {
//        zosmfZosmfPropertiesSingleton = new ZosmfPropertiesSingleton();
//        zosmfZosmfPropertiesSingleton.activate();
//        ZosmfPropertiesSingleton.setCps(cps);
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        
//        Mockito.when(zosManagerMock.getUnmanagedImage(IMAGE)).thenReturn(zosImageMock);
//        
//        zosmfManager = new ZosmfManagerImpl();
//        FieldSetter.setField(zosmfManager, ZosmfManagerImpl.class.getDeclaredField("zosManager"), zosManagerMock);
//        FieldSetter.setField(zosmfManager, ZosmfManagerImpl.class.getDeclaredField("httpManager"), httpManagerMock);
//        zosmfManagerSpy = Mockito.spy(zosmfManager);
//        Mockito.when(zosmfManagerSpy.getFramework()).thenReturn(frameworkMock);
//        Mockito.when(zosmfManagerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
//        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        zosmfManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosmfManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosmfManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosmfManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosmfManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosmfManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosmfManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosmfManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(httpManagerMock);
//        zosmfManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosmfManagerSpy);
//        zosmfManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosmfManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The HTTP Manager is not available";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosmfManager.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IHttpManagerSpi" , zosmfManager.areYouProvisionalDependentOn(httpManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosmfManager.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateZosmf() throws ZosManagerException, NoSuchFieldException, SecurityException {
//        setupZosmfImplInitialize();
//        
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosmf.Zosmf.class);
//        annotations.add(annotation);
//        
//        Object zosmfImplObject = zosmfManager.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
//        Assert.assertTrue("Error in generateZosmf() method", zosmfImplObject instanceof ZosmfImpl);
//        
//        HashMap<String, ZosmfImpl> taggedZosmfs = new HashMap<>();
//        ZosmfImpl zosmfImpl = Mockito.mock(ZosmfImpl.class);
//        taggedZosmfs.put(IMAGE_TAG.toUpperCase(), zosmfImpl);
//        FieldSetter.setField(zosmfManager, ZosmfManagerImpl.class.getDeclaredField("taggedZosmfs"), taggedZosmfs);
//        
//        zosmfImplObject = zosmfManager.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
//        Assert.assertEquals("generateZosmf() should retrn the supplied instance of ZosBatchImpl", zosmfImpl, zosmfImplObject);
//        
//        taggedZosmfs = new HashMap<>();
//        Whitebox.setInternalState(zosmfManagerSpy, "taggedZosmfs", taggedZosmfs);
//		Mockito.doReturn(new HashMap<String, IZosmf>()).when(zosmfManagerSpy).getZosmfs(Mockito.any());
//    	String expectedMessage = "Unable to provision zOS/MF, no zOS/MF server defined for image tag '" + IMAGE_TAG.toUpperCase() + "'";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//    	Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenThrow(new ZosManagerException());
//        expectedMessage = "Unable to locate z/OS image for tag '" + IMAGE_TAG.toUpperCase() + "'";
//        expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.generateZosmf(DummyTestClass.class.getDeclaredField("zosmf"), annotations);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testNewZosmf() throws ZosmfManagerException {
//        setupZosmfImplInitialize();
//        
//        IZosmf zosmf = zosmfManager.newZosmf(SERVER_ID);
//        Assert.assertNotNull("getZosmf() should not be null", zosmf);
//        IZosmf zosmf2 = zosmfManager.newZosmf(SERVER_ID);
//        Assert.assertEquals("getZosmf() should return the existing IZosmf instance", zosmf, zosmf2);
//    }
//    
//    @Test
//    public void testGetZosmfs() throws ZosManagerException {
//        setupZosmfImplInitialize();
//        
//        Mockito.doReturn(zosmfMock).when(zosmfManagerSpy).newZosmf(Mockito.any());
//        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenReturn(zosImageMock);
//        Assert.assertTrue("getZosmfs() should return the mocked ZosmfImpl", zosmfManagerSpy.getZosmfs(zosImageMock).containsValue(zosmfMock));
//
//        Mockito.when(ImageServers.get(Mockito.any())).thenReturn(Arrays.asList());
//        Assert.assertTrue("getZosmfs() should return the mocked ZosmfImpl", zosmfManagerSpy.getZosmfs(zosImageMock).containsValue(zosmfMock));
//        
//        Mockito.when(SysplexServers.get(Mockito.any())).thenReturn(Arrays.asList());
//        Assert.assertTrue("getZosmfs() should return the mocked ZosmfImpl", zosmfManagerSpy.getZosmfs(zosImageMock).containsValue(zosmfMock));
//    }
//    
//    @Test
//    public void testGetZosmfsException() throws ZosManagerException, CredentialsException {
//        setupZosmfImplInitialize();;
//
//        Mockito.when(ImageServers.get(Mockito.any())).thenThrow(new ZosmfException());
//        String expectedMessage = "Unable to get zOSMF servers for image \"" + IMAGE + "\"";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.getZosmfs(zosImageMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//        
//    @Test
//    public void testNewZosmfRestApiProcessor() throws Exception {
//        setupZosmfImplInitialize();
//        
//        HashMap<String, IZosmf> zosmfs = new HashMap<>();
//        zosmfs.put(IMAGE, zosmfMock);
//        Mockito.doReturn(zosmfs).when(zosmfManagerSpy).getZosmfs(zosImageMock);
//        Mockito.when(zosmfMock.getImage()).thenReturn(zosImageMock);
//        
//        Assert.assertEquals("newZosmfRestApiProcessor() should return the mocked ZosmfImpl", zosmfMock, ((ZosmfRestApiProcessor) zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock, false)).getCurrentZosmfServer());
//        
//        Whitebox.setInternalState(zosmfManagerSpy, "zosmfs", zosmfs);
//        Assert.assertEquals("newZosmfRestApiProcessor() should return the mocked ZosmfImpl", zosmfMock, ((ZosmfRestApiProcessor) zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock, true)).getCurrentZosmfServer());
//        
//        IZosImage zosImageMock1 = Mockito.mock(IZosImage.class);
//		Mockito.when(zosImageMock1 .getImageID()).thenReturn(IMAGE + "1");
//        String expectedMessage = "No zOSMF server configured on " + IMAGE + "1";
//        ZosmfManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosmfManagerException.class, ()->{
//        	zosmfManagerSpy.newZosmfRestApiProcessor(zosImageMock1, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//        
//    }
//
//    private void setupZosmfImplInitialize() throws ZosmfManagerException {        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        
//        PowerMockito.mockStatic(SysplexServers.class);
//        Mockito.when(SysplexServers.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
//        
//        PowerMockito.mockStatic(ServerPort.class);
//        Mockito.when(ServerPort.get(Mockito.any())).thenReturn(PORT);
//        
//        PowerMockito.mockStatic(ServerPort.class);
//        Mockito.when(ServerImage.get(SERVER_ID)).thenReturn(IMAGE);
//        
//        PowerMockito.mockStatic(ImageServers.class);
//        Mockito.when(ImageServers.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
//        
//        PowerMockito.mockStatic(Https.class);
//        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
//        
//        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
//       
//        PowerMockito.mockStatic(RequestRetry.class);
//        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(5);
//    }
//
//    class DummyTestClass {
//        @dev.galasa.zosmf.Zosmf(imageTag="tag")
//        public dev.galasa.zosmf.IZosmf zosmf;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosmfException {
//            zosmf.delete("path", null);
//        }
//    }
}
