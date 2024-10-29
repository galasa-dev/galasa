/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.internal.HttpManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.Https;
import dev.galasa.zosrseapi.internal.properties.ImageServers;
import dev.galasa.zosrseapi.internal.properties.RequestRetry;
import dev.galasa.zosrseapi.internal.properties.RseapiPropertiesSingleton;
import dev.galasa.zosrseapi.internal.properties.ServerCreds;
import dev.galasa.zosrseapi.internal.properties.ServerPort;
import dev.galasa.zosrseapi.internal.properties.SysplexServers;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({ServerPort.class, Https.class, ServerCreds.class, ImageServers.class, SysplexServers.class, RequestRetry.class})
public class TestRseapiManagerImpl {
//    
//    private RseapiManagerImpl rseapiManager;
//    
//    private RseapiManagerImpl rseapiManagerSpy;
//    
//    private RseapiPropertiesSingleton rseapiRseapiPropertiesSingleton;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
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
//    private RseapiImpl rseapiMock;
//
//    @Mock
//	private ICredentialsService credentialsServiceMock;
//
//    @Mock
//	private ICredentials credentialsMock;
//
//    private static final String IMAGE = "image";
//
//    private static final String CREDS_ID = "credsid";
//
//    private static final String SERVER = "server";
//
//    private static final String IMAGE_TAG = "tag";
//
//    @Before
//    public void setup() throws Exception {
//        rseapiRseapiPropertiesSingleton = new RseapiPropertiesSingleton();
//        rseapiRseapiPropertiesSingleton.activate();
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE);
//        
//        rseapiManager = new RseapiManagerImpl();
//        rseapiManagerSpy = Mockito.spy(rseapiManager);
//        Mockito.when(rseapiManagerSpy.getFramework()).thenReturn(frameworkMock);
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
//        rseapiManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestRseapiManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", rseapiManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(rseapiManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        rseapiManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", rseapiManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(rseapiManagerSpy, "generateAnnotatedFields", Mockito.any());
//        rseapiManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(httpManagerMock);
//        rseapiManagerSpy.youAreRequired(allManagers, activeManagers ,null);
//        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(rseapiManagerSpy);
//        rseapiManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(rseapiManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The HTTP Manager is not available";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , rseapiManager.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IHttpManagerSpi" , rseapiManager.areYouProvisionalDependentOn(httpManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , rseapiManager.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateRseapi() throws ZosManagerException, NoSuchFieldException, SecurityException, CredentialsException {
//        setupRseapiImplInitialize();
//        
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        rseapiManagerSpy.setZosManager(zosManagerMock);
//        
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosrseapi.Rseapi.class);
//        annotations.add(annotation);
//        
//        Object rseapiImplObject = rseapiManagerSpy.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
//        Assert.assertTrue("Error in generateRseapi() method", rseapiImplObject instanceof RseapiImpl);
//        
//        Map<String, RseapiImpl> taggedRseapis = new HashMap<>();
//        RseapiImpl rseapiImpl = Mockito.mock(RseapiImpl.class);
//        taggedRseapis.put(IMAGE_TAG.toUpperCase(), rseapiImpl);
//        Whitebox.setInternalState(rseapiManagerSpy, "taggedRseapis", taggedRseapis);
//        
//        rseapiImplObject = rseapiManagerSpy.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
//        Assert.assertEquals("generateRseapi() should retrn the supplied instance of ZosBatchImpl", rseapiImpl, rseapiImplObject);
//        
//        
//        taggedRseapis = new HashMap<>();
//        Whitebox.setInternalState(rseapiManagerSpy, "taggedRseapis", taggedRseapis);
//		Mockito.doReturn(new HashMap<String, IRseapi>()).when(rseapiManagerSpy).getRseapis(Mockito.any());
//    	String expectedMessage = "Unable to provision RSE API server, no RSE API server defined for image tag '" + IMAGE_TAG.toUpperCase() + "'";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//
//    	Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenThrow(new ZosManagerException());
//        expectedMessage = "Unable to locate z/OS image for tag '" + IMAGE_TAG.toUpperCase() + "'";
//        expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.generateRseapi(DummyTestClass.class.getDeclaredField("rseapi"), annotations);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testNewRseapi() throws ZosManagerException, CredentialsException {
//        setupRseapiImplInitialize();
//        
//        IRseapi rseapi = rseapiManagerSpy.newRseapi(SERVER);
//        Assert.assertNotNull("getRseapi() should not be null", rseapi);
//        IRseapi rseapi2 = rseapiManagerSpy.newRseapi(SERVER);
//        Assert.assertEquals("getRseapi() should return the existing Irseapi instance", rseapi, rseapi2);
//    }
//    
//    @Test
//    public void testGetRseapis() throws ZosManagerException, CredentialsException {
//        setupRseapiImplInitialize();
//        
//        Mockito.doReturn(rseapiMock).when(rseapiManagerSpy).newRseapi(Mockito.any());
//        Mockito.when(zosManagerMock.getImage(Mockito.anyString())).thenReturn(zosImageMock);
//        Assert.assertTrue("getRseapis() should return the mocked RseapiImpl", rseapiManagerSpy.getRseapis(zosImageMock).containsValue(rseapiMock));
//
//        Mockito.when(ImageServers.get(Mockito.any())).thenReturn(Arrays.asList());
//        Assert.assertTrue("getRseapis() should return the mocked RseapiImpl", rseapiManagerSpy.getRseapis(zosImageMock).containsValue(rseapiMock));
//        
//        Mockito.when(SysplexServers.get(Mockito.any())).thenReturn(Arrays.asList());
//        Assert.assertTrue("getRseapis() should return the mocked RseapiImpl", rseapiManagerSpy.getRseapis(zosImageMock).containsValue(rseapiMock));
//    }
//    
//    @Test
//    public void testGetRseapisException() throws ZosManagerException, CredentialsException {
//        setupRseapiImplInitialize();
//
//        Mockito.when(ImageServers.get(Mockito.any())).thenThrow(new RseapiException());
//        String expectedMessage = "Unable to get RSE API servers for image \"" + IMAGE + "\"";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.getRseapis(zosImageMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testNewRseapiRestApiProcessor() throws ZosManagerException, CredentialsException {
//        setupRseapiImplInitialize();
//        
//        HashMap<String, IRseapi> rseapis = new HashMap<>();
//        rseapis.put(IMAGE, rseapiMock);
//        Mockito.doReturn(rseapis).when(rseapiManagerSpy).getRseapis(zosImageMock);
//        Mockito.when(rseapiMock.getImage()).thenReturn(zosImageMock);
//        
//        Assert.assertEquals("newRseapiRestApiProcessor() should return the mocked RseapiImpl", rseapiMock, ((RseapiRestApiProcessor) rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock, false)).getCurrentRseapiServer());
//        
//        Whitebox.setInternalState(rseapiManagerSpy, "rseapis", rseapis);
//        Assert.assertEquals("newRseapiRestApiProcessor() should return the mocked RseapiImpl", rseapiMock, ((RseapiRestApiProcessor) rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock, true)).getCurrentRseapiServer());
//        
//        IZosImage zosImageMock1 = Mockito.mock(IZosImage.class);
//		Mockito.when(zosImageMock1 .getImageID()).thenReturn(IMAGE + "1");
//        String expectedMessage = "No RSE API server configured on " + IMAGE + "1";
//        RseapiManagerException expectedException = Assert.assertThrows("expected exception should be thrown", RseapiManagerException.class, ()->{
//        	rseapiManagerSpy.newRseapiRestApiProcessor(zosImageMock1, true);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//
//    private void setupRseapiImplInitialize() throws ZosManagerException, CredentialsException {        
//        PowerMockito.mockStatic(ServerCreds.class);
//        Mockito.when(ServerCreds.get(Mockito.any())).thenReturn(CREDS_ID);
//        
//        PowerMockito.mockStatic(ImageServers.class);
//        Mockito.when(ImageServers.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
//        
//        PowerMockito.mockStatic(SysplexServers.class);
//        Mockito.when(SysplexServers.get(Mockito.any())).thenReturn(Arrays.asList(IMAGE));
//        
//        PowerMockito.mockStatic(Https.class);
//        Mockito.when(Https.get(Mockito.any())).thenReturn(true);
//        
//        Mockito.when(rseapiManagerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosManagerMock.getUnmanagedImage(Mockito.any())).thenReturn(zosImageMock);
//        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
//        Mockito.when(rseapiManagerSpy.getHttpManager()).thenReturn(httpManagerMock);
//        Mockito.when(httpManagerMock.newHttpClient()).thenReturn(httpClientMock);
//        Mockito.when(rseapiManagerSpy.getFramework()).thenReturn(frameworkMock);
//        Mockito.when(frameworkMock.getCredentialsService()).thenReturn(credentialsServiceMock);
//        Mockito.when(credentialsServiceMock.getCredentials(Mockito.any())).thenReturn(credentialsMock);
//       
//        PowerMockito.mockStatic(RequestRetry.class);
//        Mockito.when(RequestRetry.get(Mockito.any())).thenReturn(5);
//    }
//
//    class DummyTestClass {
//        @dev.galasa.zosrseapi.Rseapi(imageTag="tag")
//        public dev.galasa.zosrseapi.IRseapi rseapi;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws RseapiException {
//            rseapi.delete("path", null);
//        }
//    }
}
