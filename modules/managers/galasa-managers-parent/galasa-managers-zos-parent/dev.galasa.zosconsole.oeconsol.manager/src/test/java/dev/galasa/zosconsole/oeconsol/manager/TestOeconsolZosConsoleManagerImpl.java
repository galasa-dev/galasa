/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

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

import dev.galasa.ICredentials;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.oeconsol.manager.internal.properties.OeconsolPath;
import dev.galasa.zosconsole.oeconsol.manager.internal.properties.OeconsolPropertiesSingleton;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({OeconsolPath.class})
public class TestOeconsolZosConsoleManagerImpl {
//
//	private OeconsolZosConsoleManagerImpl oeconsolZosConsoleManager;
//    
//    private OeconsolZosConsoleManagerImpl oeconsolZosConsoleManagerSpy;
//    
//    private OeconsolPropertiesSingleton oeconsolPropertiesSingleton;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
//    
//    @Mock
//    private IFramework frameworkMock;
//    
//    @Mock
//    private IConfigurationPropertyStoreService cpsMock; 
//    
//    @Mock
//    public IManager managerMock;
//    
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//    
//    @Mock
//    private ZosUNIXCommandManagerImpl zosUnixCommandManagerMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    @Mock
//	private IZosUNIXCommand zosUnixCommandMock;
//    
//    private static final String IMAGE_ID = "image";
//
//    @Before
//    public void setup() throws Exception {
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenReturn(cpsMock);
//        oeconsolPropertiesSingleton = new OeconsolPropertiesSingleton();
//        oeconsolPropertiesSingleton.activate();
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn(IMAGE_ID); 
//        
//        oeconsolZosConsoleManager = new OeconsolZosConsoleManagerImpl();
//        oeconsolZosConsoleManagerSpy = Mockito.spy(oeconsolZosConsoleManager);        
//        oeconsolZosConsoleManagerSpy.setZosManager(zosManagerMock);
//        oeconsolZosConsoleManagerSpy.setZosUnixCommandManager((IZosUNIXCommandSpi) zosUnixCommandManagerMock);
//        Mockito.when(zosUnixCommandManagerMock.getZosUNIXCommand(Mockito.any())).thenReturn(zosUnixCommandMock);
//        
//        Mockito.when(oeconsolZosConsoleManagerSpy.getFramework()).thenReturn(frameworkMock);
//               
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        oeconsolZosConsoleManager.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestOeconsolZosConsoleManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", oeconsolZosConsoleManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(oeconsolZosConsoleManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        oeconsolZosConsoleManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", oeconsolZosConsoleManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ConfigurationPropertyStoreException, ManagerException {
//        Mockito.when(frameworkMock.getConfigurationPropertyService(Mockito.any())).thenThrow(new ConfigurationPropertyStoreException("exception"));
//        String expectedMessage = "Unable to request framework services";
//        ZosConsoleManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleManagerException.class, ()->{
//        	oeconsolZosConsoleManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(oeconsolZosConsoleManagerSpy, "generateAnnotatedFields", Mockito.any());
//        oeconsolZosConsoleManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(oeconsolZosConsoleManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosUnixCommandManagerMock);
//        oeconsolZosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(oeconsolZosConsoleManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(oeconsolZosConsoleManagerSpy);
//        oeconsolZosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(oeconsolZosConsoleManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//        	oeconsolZosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The zOS UNIX Command Manager is not available";
//        ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//        	oeconsolZosConsoleManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , oeconsolZosConsoleManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IZosUNIXCommandSpi" , oeconsolZosConsoleManagerSpy.areYouProvisionalDependentOn(zosUnixCommandManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , oeconsolZosConsoleManagerSpy.areYouProvisionalDependentOn(managerMock));
//    }
//    
//    @Test
//    public void testGenerateZosConsole() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosunixcommand.ZosUNIXCommand.class);
//        annotations.add(annotation);
//        
//        Object zosConsoleImplObject = oeconsolZosConsoleManagerSpy.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
//        Assert.assertTrue("Error in generateZosConsole() method", zosConsoleImplObject instanceof OeconsolZosConsoleImpl);
//        
//        HashMap<String, OeconsolZosConsoleImpl> taggedZosConsoles = new HashMap<>();
//        OeconsolZosConsoleImpl oeconsolZosConsoleMock = Mockito.mock(OeconsolZosConsoleImpl.class);
//        taggedZosConsoles.put("TAG", oeconsolZosConsoleMock);
//        Whitebox.setInternalState(oeconsolZosConsoleManagerSpy, "taggedZosConsoles", taggedZosConsoles);
//        
//        zosConsoleImplObject = oeconsolZosConsoleManagerSpy.generateZosConsole(DummyTestClass.class.getDeclaredField("zosConsole"), annotations);
//        Assert.assertEquals("generateZosConsole() should return the supplied instance of OeconsolZosConsoleImpl", oeconsolZosConsoleMock, zosConsoleImplObject);
//    }
//    
//    @Test
//    public void testGetZosManager() {
//        IZosManagerSpi zosManager = oeconsolZosConsoleManagerSpy.getZosManager();
//        Assert.assertNotNull("getZosUNIXCommand() should not be null", zosManager);
//        IZosManagerSpi zosManager2 = oeconsolZosConsoleManagerSpy.getZosManager();
//        Assert.assertEquals("getZosUNIXCommand() should return the existing IZosManagerSpi instance", zosManager, zosManager2);
//    }
//    
//    @Test
//    public void testGetZosUNIXCommand() {
//        IZosUNIXCommand zosUnix = oeconsolZosConsoleManagerSpy.getZosUNIXCommand(zosImageMock);
//        Assert.assertNotNull("getZosUNIXCommand() should not be null", zosUnix);
//        IZosUNIXCommand zosUnix2 = oeconsolZosConsoleManagerSpy.getZosUNIXCommand(zosImageMock);
//        Assert.assertEquals("getZosUNIXCommand() should return the existing IZosUNIXCommand instance", zosUnix, zosUnix2);
//    }
//    
//    @Test
//    public void testGetZosConsole() throws ZosConsoleManagerException {
//    	HashMap<String, OeconsolZosConsoleImpl> zosConsoles = new HashMap<>();
//    	OeconsolZosConsoleImpl oeconsolZosConsoleMock = Mockito.mock(OeconsolZosConsoleImpl.class);
//    	zosConsoles.put(IMAGE_ID, oeconsolZosConsoleMock);
//    	Whitebox.setInternalState(oeconsolZosConsoleManagerSpy, "zosConsoles", zosConsoles);
//    	Assert.assertEquals("getZosConsole() should return the expected object", oeconsolZosConsoleMock, oeconsolZosConsoleManagerSpy.getZosConsole(zosImageMock));
//    
//    	zosConsoles = new HashMap<>();
//    	Whitebox.setInternalState(oeconsolZosConsoleManagerSpy, "zosConsoles", zosConsoles);
//    	PowerMockito.mockStatic(OeconsolPath.class);
//        Mockito.when(OeconsolPath.get(Mockito.any())).thenReturn("/oeconsol");
//    	IZosConsole zosConsole = oeconsolZosConsoleManagerSpy.getZosConsole(zosImageMock);
//    	Assert.assertEquals("getZosConsole() should return the expected object", IMAGE_ID, zosConsole.toString());
//    }
//    
//    @Test
//    public void testGetCredentials() throws ZosManagerException {
//    	ICredentials credentialsMock = Mockito.mock(ICredentials.class);
//		Mockito.when(zosManagerMock.getCredentials(Mockito.any(), Mockito.any())).thenReturn(credentialsMock);
//		Assert.assertEquals("getCredentials() should return the expected object", credentialsMock, oeconsolZosConsoleManagerSpy.getCredentials("USER", zosImageMock));		
//
//		Mockito.when(zosManagerMock.getCredentials(Mockito.any(), Mockito.any())).thenThrow(new ZosManagerException());
//        String expectedMessage = "oeconsol requires 'Console Name' to be a valid credentials id";
//        ZosConsoleException expectedException = Assert.assertThrows("expected exception should be thrown", ZosConsoleException.class, ()->{
//        	oeconsolZosConsoleManagerSpy.getCredentials("USER", zosImageMock);
//        });
//    	Assert.assertEquals("exception should contain expected message", expectedMessage, expectedException.getMessage());
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zosconsole.ZosConsole(imageTag="TAG")
//        public dev.galasa.zosconsole.IZosConsole zosConsole;
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosConsoleException {
//        	zosConsole.issueCommand("command");
//        }
//    }
}