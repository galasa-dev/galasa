/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
public class TestZosmfZosConsoleManagerImpl {
//    
//    private ZosmfZosConsoleManagerImpl zosConsoleManager;
//    
//    private ZosmfZosConsoleManagerImpl zosConsoleManagerSpy;
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
//    private ZosmfManagerImpl zosmfManagerMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Before
//    public void setup() throws Exception {
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        
//        zosConsoleManager = new ZosmfZosConsoleManagerImpl();
//        zosConsoleManagerSpy = Mockito.spy(zosConsoleManager);
//        zosConsoleManagerSpy.setZosManager(zosManagerMock);
//        zosConsoleManagerSpy.setZosmfManager(zosmfManagerMock);
//        Mockito.when(zosConsoleManagerSpy.getFramework()).thenReturn(frameworkMock);
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
//        zosConsoleManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosmfZosConsoleManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosConsoleManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosConsoleManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosConsoleManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosConsoleManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosConsoleManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosConsoleManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosmfManagerMock);
//        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosConsoleManagerSpy);
//        zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosConsoleManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The zOSMF Manager is not available";
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	zosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosConsoleManager.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosConsoleManager.areYouProvisionalDependentOn(zosmfManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosConsoleManager.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateZosConsole() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosconsole.ZosConsole.class);
//        annotations.add(annotation);
//        
//        Whitebox.setInternalState(zosConsoleManager, "zosmfManager", zosmfManagerMock);
//        Whitebox.setInternalState(zosConsoleManager, "zosManager", zosManagerMock);
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        Mockito.when(zosManagerMock.getZosConsolePropertyConsoleRestrictToImage(Mockito.any())).thenReturn(false);
//        
//        
//        Object zosConsoleImplObject = zosConsoleManager.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
//        Assert.assertTrue("Error in generateZosConsole() method", zosConsoleImplObject instanceof ZosmfZosConsoleImpl);
//        
//        HashMap<String, ZosmfZosConsoleImpl> taggedZosConsoles = new HashMap<>();
//        ZosmfZosConsoleImpl zosmfZosConsoleImpl = Mockito.mock(ZosmfZosConsoleImpl.class);
//        taggedZosConsoles.put("TAG", zosmfZosConsoleImpl);
//        Whitebox.setInternalState(zosConsoleManagerSpy, "taggedZosConsoles", taggedZosConsoles);
//        
//        zosConsoleImplObject = zosConsoleManagerSpy.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
//        Assert.assertEquals("generateZosConsole() should retrn the supplied instance of ZosBatchImpl", zosmfZosConsoleImpl, zosConsoleImplObject);
//    }
//    
//    @Test
//    public void testGetZosConsole() throws ZosConsoleException {
//        IZosConsole zosConsole = zosConsoleManagerSpy.getZosConsole(zosImageMock);
//        Assert.assertNotNull("getZosConsole() should not be null", zosConsole);
//        IZosConsole zosConsole2 = zosConsoleManagerSpy.getZosConsole(zosImageMock);
//        Assert.assertEquals("getZosConsole() should return the existing IZosConsole instance", zosConsole, zosConsole2);
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zosconsole.ZosConsole(imageTag="tag")
//        public dev.galasa.zosconsole.IZosConsole zosConsole;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosConsoleException {
//            zosConsole.issueCommand("command");
//        }
//    }
}
