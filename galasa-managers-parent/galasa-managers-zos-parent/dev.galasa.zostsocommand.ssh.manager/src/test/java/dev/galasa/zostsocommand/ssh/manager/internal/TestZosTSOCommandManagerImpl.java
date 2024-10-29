/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

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
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandException;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;
import dev.galasa.zostsocommand.ssh.manager.internal.properties.TsocmdPath;
import dev.galasa.zostsocommand.ssh.manager.internal.properties.ZosTSOCommandSshPropertiesSingleton;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandImpl;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({TsocmdPath.class})
public class TestZosTSOCommandManagerImpl {
//    
//    private ZosTSOCommandManagerImpl zosTSOCommandManager;
//    
//    private ZosTSOCommandManagerImpl zosTSOCommandManagerSpy;
//    
//    private ZosTSOCommandSshPropertiesSingleton zosTSOCommandSshPropertiesSingleton;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
//
//	private static final String TSOCMD_PATH = "/tsocmd/path";
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    public IManager managerMock;
//    
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//    
//    @Mock
//    private ZosUNIXCommandManagerImpl zosUNIXCommandManagerMock;
//    
//    @Mock
//    private ZosUNIXCommandImpl zosUnixCommandMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Before
//    public void setup() throws Exception {
//        zosTSOCommandSshPropertiesSingleton = new ZosTSOCommandSshPropertiesSingleton();
//        zosTSOCommandSshPropertiesSingleton.activate();
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        
//        zosTSOCommandManager = new ZosTSOCommandManagerImpl();
//        zosTSOCommandManagerSpy = Mockito.spy(zosTSOCommandManager);        
//        zosTSOCommandManagerSpy.setZosManager(zosManagerMock);        
//        zosTSOCommandManagerSpy.setZosUnixCommandManager(zosUNIXCommandManagerMock);;
//        
//        Mockito.when(zosTSOCommandManagerSpy.getFramework()).thenReturn(frameworkMock);
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosTSOCommandManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosTSOCommandManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosTSOCommandManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
//        	zosTSOCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosTSOCommandManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosTSOCommandManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosUNIXCommandManagerMock);
//        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosTSOCommandManagerSpy);
//        zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosTSOCommandManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
//        	zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The zOS UNIX Command Manager is not available";
//        ZosTSOCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosTSOCommandManagerException.class, ()->{
//        	zosTSOCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosTSOCommandManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IZosUNIXManagerSpi" , zosTSOCommandManagerSpy.areYouProvisionalDependentOn(zosUNIXCommandManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosTSOCommandManagerSpy.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateZosTSO() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zostsocommand.ZosTSOCommand.class);
//        annotations.add(annotation);
//        Mockito.doReturn(zosUnixCommandMock).when(zosTSOCommandManagerSpy).getZosUNIXCommand(Mockito.any());
//        Mockito.doReturn(TSOCMD_PATH).when(zosTSOCommandManagerSpy).getTsocmdPath(Mockito.any());
//        
//        Object zosTSOImplObject = zosTSOCommandManagerSpy.generateZosTSOCommand(DummyTestClass.class.getDeclaredField("zosTSOCommand"), annotations);
//        Assert.assertTrue("Error in generateZosTSO() method", zosTSOImplObject instanceof ZosTSOCommandImpl);
//        
//        HashMap<String, ZosTSOCommandImpl> taggedZosTSOCommands = new HashMap<>();
//        ZosTSOCommandImpl zosTSOCommandImpl = Mockito.mock(ZosTSOCommandImpl.class);
//        taggedZosTSOCommands.put("TAG", zosTSOCommandImpl);
//        Whitebox.setInternalState(zosTSOCommandManagerSpy, "taggedZosTSOCommands", taggedZosTSOCommands);
//        
//        zosTSOImplObject = zosTSOCommandManagerSpy.generateZosTSOCommand(DummyTestClass.class.getDeclaredField("zosTSOCommand"), annotations);
//        Assert.assertEquals("generateZosTSO() should retrn the supplied instance of ZosTSOCommandImpl", zosTSOCommandImpl, zosTSOImplObject);
//    }
//    
//    @Test
//    public void testGetZosTSOCommand() throws ZosTSOCommandManagerException, ZosUNIXCommandManagerException {
//        Mockito.doReturn(zosUnixCommandMock).when(zosTSOCommandManagerSpy).getZosUNIXCommand(Mockito.any());
//        IZosTSOCommand zosTSOCommand = zosTSOCommandManagerSpy.getZosTSOCommand(zosImageMock);
//        Assert.assertNotNull("getZosTSO() should not be null", zosTSOCommand);
//        IZosTSOCommand zosTSOCommand2 = zosTSOCommandManagerSpy.getZosTSOCommand(zosImageMock);
//        Assert.assertEquals("getZosTSO() should return the existing IZosTSO instance", zosTSOCommand, zosTSOCommand2);
//    }
//    
//    @Test
//    public void testGetZosUNIXCommand() throws ZosUNIXCommandManagerException, ZosTSOCommandManagerException {
//    	Mockito.when(zosUNIXCommandManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUnixCommandMock);
//    	Assert.assertEquals("getZosUNIXCommand() should return the expected object", zosUnixCommandMock, zosTSOCommandManagerSpy.getZosUNIXCommand(zosImageMock));
//    }
//    
//    @Test
//    public void testGetTsocmdPath() throws ZosUNIXCommandManagerException, ZosTSOCommandManagerException {
//    	PowerMockito.mockStatic(TsocmdPath.class);
//        Mockito.when(TsocmdPath.get(Mockito.any())).thenReturn(TSOCMD_PATH);
//    	Assert.assertEquals("getTsocmdPath() should return the expected value", TSOCMD_PATH, zosTSOCommandManagerSpy.getTsocmdPath(zosImageMock));
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zostsocommand.ZosTSOCommand(imageTag="TAG")
//        public dev.galasa.zostsocommand.IZosTSOCommand zosTSOCommand;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosTSOCommandException {
//            zosTSOCommand.issueCommand("command");
//        }
//    }
}