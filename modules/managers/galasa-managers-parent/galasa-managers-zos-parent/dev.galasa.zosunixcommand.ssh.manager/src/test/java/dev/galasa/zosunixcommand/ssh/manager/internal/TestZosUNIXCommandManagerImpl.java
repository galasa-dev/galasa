/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

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
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.ipnetwork.internal.IpNetworkManagerImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;
import dev.galasa.zosunixcommand.ssh.manager.internal.properties.ZosUNIXCommandSshPropertiesSingleton;

//@RunWith(PowerMockRunner.class)
public class TestZosUNIXCommandManagerImpl {
//    
//    private ZosUNIXCommandManagerImpl zosUnixCommandManager;
//    
//    private ZosUNIXCommandManagerImpl zosUnixCommandManagerSpy;
//    
//    private ZosUNIXCommandSshPropertiesSingleton zosUnixCommandSshPropertiesSingleton;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
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
//    private IpNetworkManagerImpl ipNetworkManagerMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//    private IIpHost ipHostMock;
//    
//    @Mock
//    private ICommandShell commandShellMock;
//
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        zosUnixCommandSshPropertiesSingleton = new ZosUNIXCommandSshPropertiesSingleton();
//        zosUnixCommandSshPropertiesSingleton.activate();
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");        
//        Mockito.when(zosImageMock.getIpHost()).thenReturn(ipHostMock);
//        
//        zosUnixCommandManager = new ZosUNIXCommandManagerImpl();
//        zosUnixCommandManagerSpy = Mockito.spy(zosUnixCommandManager);        
//        zosUnixCommandManagerSpy.setZosManager(zosManagerMock);
//        zosUnixCommandManagerSpy.setIpNetworkManager(ipNetworkManagerMock);
//        
//        Mockito.when(zosUnixCommandManagerSpy.getFramework()).thenReturn(frameworkMock);
//        
//        Mockito.when(ipNetworkManagerMock.getCommandShell(Mockito.any(), Mockito.any())).thenReturn(commandShellMock);
//        Mockito.doNothing().when(commandShellMock).reportResultStrings(Mockito.anyBoolean());
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        zosUnixCommandManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosUNIXCommandManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosUnixCommandManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosUnixCommandManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosUnixCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosUnixCommandManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        ZosUNIXCommandManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosUNIXCommandManagerException.class, ()->{
//        	zosUnixCommandManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosUnixCommandManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosUnixCommandManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosUnixCommandManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(ipNetworkManagerMock);
//        zosUnixCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosUnixCommandManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosUnixCommandManagerSpy);
//        zosUnixCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosUnixCommandManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//        	zosUnixCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The IP Network Manager is not available";
//        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//        	zosUnixCommandManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosUnixCommandManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IIpNetworkManagerSpi" , zosUnixCommandManagerSpy.areYouProvisionalDependentOn(ipNetworkManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosUnixCommandManagerSpy.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateZosUNIX() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosunixcommand.ZosUNIXCommand.class);
//        annotations.add(annotation);
//        
//        Object zosUNIXImplObject = zosUnixCommandManagerSpy.generateZosUNIXCommand(DummyTestClass.class.getDeclaredField("zosUnixCommand"), annotations);
//        Assert.assertTrue("Error in generateZosUNIX() method", zosUNIXImplObject instanceof ZosUNIXCommandImpl);
//        
//        HashMap<String, ZosUNIXCommandImpl> taggedZosUNIXCommands = new HashMap<>();
//        ZosUNIXCommandImpl zosUNIXCommandImpl = Mockito.mock(ZosUNIXCommandImpl.class);
//        taggedZosUNIXCommands.put("TAG", zosUNIXCommandImpl);
//        Whitebox.setInternalState(zosUnixCommandManagerSpy, "taggedZosUNIXCommands", taggedZosUNIXCommands);
//        
//        zosUNIXImplObject = zosUnixCommandManagerSpy.generateZosUNIXCommand(DummyTestClass.class.getDeclaredField("zosUnixCommand"), annotations);
//        Assert.assertEquals("generateZosUNIX() should retrn the supplied instance of ZosUNIXCommandImpl", zosUNIXCommandImpl, zosUNIXImplObject);
//    }
//    
//    @Test
//    public void testGetZosUNIX() throws ZosUNIXCommandManagerException {
//        IZosUNIXCommand zosUnix = zosUnixCommandManagerSpy.getZosUNIXCommand(zosImageMock);
//        Assert.assertNotNull("getZosUNIX() should not be null", zosUnix);
//        IZosUNIXCommand zosUnix2 = zosUnixCommandManagerSpy.getZosUNIXCommand(zosImageMock);
//        Assert.assertEquals("getZosUNIX() should return the existing IZosUNIXCommand instance", zosUnix, zosUnix2);
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zosunixcommand.ZosUNIXCommand(imageTag="TAG")
//        public dev.galasa.zosunixcommand.IZosUNIXCommand zosUnixCommand;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosUNIXCommandException {
//            zosUnixCommand.issueCommand("command");
//        }
//    }
}