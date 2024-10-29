/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosbatch.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//import org.powermock.reflect.Whitebox;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResultArchiveStore;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJobname;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.ZosBatchManagerException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosBatchManagerImpl {
//
//	private ZosmfZosBatchManagerImpl zosBatchManager; 
//    
//    private ZosmfZosBatchManagerImpl zosBatchManagerSpy;
//
//    private List<IManager> allManagers;
//    
//    private List<IManager> activeManagers;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
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
//    @Mock
//    private IZosBatchJobname zosJobnameMock;
//    
//    private static final String JOBNAME_PREFIX = "PFX";
//
//    private static final String EXCEPTION = "exception";    
//
//    @Before
//    public void setup() throws Exception {
//        PowerMockito.mockStatic(LogFactory.class);
//        Mockito.when(LogFactory.getLog(Mockito.any(Class.class))).thenReturn(logMock);
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
//        Mockito.doAnswer(answer).when(logMock).error(Mockito.any(), Mockito.any());
//        
//        Mockito.when(zosManagerMock.newZosBatchJobname(Mockito.anyString())).thenReturn(zosJobnameMock);        
//        Mockito.when(zosManagerMock.newZosBatchJobname(Mockito.any(IZosImage.class))).thenReturn(zosJobnameMock);
//        
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        
//        zosBatchManager = new ZosmfZosBatchManagerImpl();
//        zosBatchManagerSpy = Mockito.spy(zosBatchManager);
//        Whitebox.setInternalState(zosBatchManagerSpy, "zosManager", zosManagerMock);
//        Whitebox.setInternalState(zosBatchManagerSpy, "zosmfManager", zosmfManagerMock);
//        Whitebox.setInternalState(zosBatchManagerSpy, "framework", frameworkMock);
//        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
//        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(new File("/").toPath());
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testGetters( ) {
//    	Assert.assertEquals("Error in getZosManager() method", zosManagerMock, zosBatchManagerSpy.getZosManager());
//    	Assert.assertEquals("Error in getZosmfManager() method", zosmfManagerMock, zosBatchManagerSpy.getZosmfManager());
//    	Path artifactsRootMock = Mockito.mock(Path.class);
//    	Mockito.when(artifactsRootMock.toString()).thenReturn("artifactsRootMock");
//    	Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", artifactsRootMock);
//    	Assert.assertEquals("Error in getArtifactsRoot() method", artifactsRootMock, zosBatchManagerSpy.getArtifactsRoot());
//    	Path archivePathMock = Mockito.mock(Path.class);
//    	Mockito.when(archivePathMock.toString()).thenReturn("archivePathMock");
//    	Whitebox.setInternalState(zosBatchManagerSpy, "archivePath", archivePathMock);
//    	Assert.assertEquals("Error in getArchivePath() method", archivePathMock, zosBatchManagerSpy.getArchivePath());
//    	Whitebox.setInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName", "currentTestMethodArchiveFolderName");
//    	Path currentTestMethodArchiveFolderMock = Mockito.mock(Path.class);
//    	Mockito.when(currentTestMethodArchiveFolderMock.toString()).thenReturn("currentTestMethodArchiveFolderMock");
//    	Mockito.when(archivePathMock.resolve(Mockito.anyString())).thenReturn(currentTestMethodArchiveFolderMock);
//    	Assert.assertEquals("Error in getCurrentTestMethodArchiveFolder() method", currentTestMethodArchiveFolderMock, zosBatchManagerSpy.getCurrentTestMethodArchiveFolder());
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosmfZosBatchManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosBatchManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosBatchManagerSpy.getFramework(), frameworkMock);
//    }
//
//    @Test
//    public void testInitialiseException() throws ManagerException {
//        PowerMockito.doThrow(new ManagerException(EXCEPTION)).when(zosBatchManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//		ManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ManagerException.class, ()->{
//			zosBatchManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//		});
//    	Assert.assertEquals("exception should contain expected cause", EXCEPTION, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosBatchManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosBatchManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosmfManagerMock);
//        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(2)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosBatchManagerSpy);
//        zosBatchManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosBatchManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        String expectedMessage = "The zOS Manager is not available";
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//			zosBatchManagerSpy.youAreRequired(allManagers, activeManagers, null);
//		});
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The zOSMF Manager is not available";
//        ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//			zosBatchManagerSpy.youAreRequired(allManagers, activeManagers, null);
//		});
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosBatchManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosBatchManagerSpy.areYouProvisionalDependentOn(zosmfManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosBatchManagerSpy.areYouProvisionalDependentOn(managerMock));
//    }
//
//    @Test
//    public void testStartOfTestMethod() throws Exception {
//        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
//        zosBatchManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethod"), null));
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName(), Whitebox.getInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName"));
//        zosBatchManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod"), DummyTestClass.class.getDeclaredMethod("dummyTestMethod")));
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName() + "." + DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod").getName(), Whitebox.getInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName"));
//    }
//    
//    @Test
//    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
//        Mockito.doNothing().when(zosBatchManagerSpy).cleanup(true);
//        Whitebox.setInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName());
//        zosBatchManagerSpy.endOfTestMethod(new GalasaMethod(null, DummyTestClass.class.getDeclaredMethod("dummyTestMethod")), "pass", null);
//        Assert.assertEquals("currentTestMethodArchiveFolderName should be expected value", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName(), Whitebox.getInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName"));
//    }
//    
//    @Test
//    public void testEndOfTestClass() throws NoSuchMethodException, SecurityException, ManagerException {
//        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
//        Mockito.doNothing().when(zosBatchManagerSpy).cleanup(true);
//        zosBatchManagerSpy.endOfTestClass(null, null);
//        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", "postTest", Whitebox.getInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName"));
//    }
//    
//    @Test
//    public void testEndOfTestRun() throws NoSuchMethodException, SecurityException, ManagerException {
//        Whitebox.setInternalState(zosBatchManagerSpy, "artifactsRoot", new File("/").toPath());
//        Mockito.doNothing().when(zosBatchManagerSpy).cleanup(true);
//        Whitebox.setInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName", DummyTestClass.class.getDeclaredMethod("dummyTestMethod").getName());
//        zosBatchManagerSpy.endOfTestRun();
//        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", "dummyTestMethod", Whitebox.getInternalState(zosBatchManagerSpy, "currentTestMethodArchiveFolderName"));
//
//        Mockito.doThrow(new ZosBatchException()).when(zosBatchManagerSpy).cleanup(true);
//        zosBatchManagerSpy.endOfTestRun();
//        Assert.assertEquals("endOfTestRun() should log expected message", "Problem in endOfTestRun()", logMessage);
//    }
//    
//    @Test
//    public void testCleanup() throws Exception {
//        zosBatchManagerSpy.cleanup(true);
//        
//        HashMap<String, ZosmfZosBatchImpl> taggedZosBatches = new HashMap<>();
//        ZosmfZosBatchImpl zosBatchImpl = Mockito.mock(ZosmfZosBatchImpl.class);
//        Mockito.doNothing().when(zosBatchImpl).cleanup(true);
//        taggedZosBatches.put("TAG", zosBatchImpl);
//        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);
//        Whitebox.setInternalState(zosBatchManagerSpy, "zosBatches", taggedZosBatches);
//        zosBatchManagerSpy.cleanup(true);
//        PowerMockito.verifyPrivate(zosBatchImpl, Mockito.times(2)).invoke("cleanup", Mockito.anyBoolean());        
//    }
//    
//    @Test
//    public void testGenerateZosBatch() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatch.class);
//        annotations.add(annotation);
//        
//        Object zosBatchImplObject = zosBatchManagerSpy.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
//        Assert.assertTrue("Error in generateZosBatch() method", zosBatchImplObject instanceof ZosmfZosBatchImpl);
//        
//        HashMap<String, ZosmfZosBatchImpl> taggedZosBatches = new HashMap<>();
//        ZosmfZosBatchImpl zosBatchImpl = Mockito.mock(ZosmfZosBatchImpl.class);
//        taggedZosBatches.put("TAG", zosBatchImpl);
//        Whitebox.setInternalState(zosBatchManagerSpy, "taggedZosBatches", taggedZosBatches);
//        
//        zosBatchImplObject = zosBatchManagerSpy.generateZosBatch(DummyTestClass.class.getDeclaredField("zosBatch"), annotations);
//        Assert.assertEquals("generateZosBatch() should retrn the supplied instance of ZosBatchImpl", zosBatchImpl, zosBatchImplObject);
//    }
//    
//    @Test
//    public void testGenerateZosBatchJobname() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatchJobname.class);
//        annotations.add(annotation);
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenReturn(zosImageMock);
//        
//        Assert.assertEquals("generateZosBatchJobname() should return the mocked ZosBatchJobnameImpl", zosBatchManagerSpy.generateZosBatchJobname(DummyTestClass.class.getDeclaredField("zosBatchJobname"), annotations), zosJobnameMock);
//    }
//    
//    @Test
//    public void testGenerateZosBatchJobnameException() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosbatch.ZosBatchJobname.class);
//        annotations.add(annotation);
//        Mockito.when(zosManagerMock.getImageForTag(Mockito.any())).thenThrow(new ZosBatchManagerException(EXCEPTION));
//
//        String expectedMessage = "Unable to get image for tag \"TAG\"";
//		ZosBatchManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosBatchManagerException.class, ()->{
//			zosBatchManagerSpy.generateZosBatchJobname(DummyTestClass.class.getDeclaredField("zosBatchJobname"), annotations);
//		});
//    	Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testNewZosBatchJobname() throws Exception {
//    	Mockito.when(zosJobnameMock.getName()).thenReturn(JOBNAME_PREFIX);        
//        IZosBatchJobname zosBatchJobname = zosBatchManagerSpy.newZosBatchJobname("image");
//        Assert.assertTrue("IZosBatchJobname getName() should start with the supplied value", zosBatchJobname.getName().startsWith(JOBNAME_PREFIX));
//    }
//    
//    @Test
//    public void testGetZosBatch() {
//        IZosBatch zosBatch = zosBatchManagerSpy.getZosBatch(zosImageMock);
//        Assert.assertNotNull("getZosBatch() should not be null", zosBatch);
//        IZosBatch zosBatch2 = zosBatchManagerSpy.getZosBatch(zosImageMock);
//        Assert.assertEquals("getZosBatch() should return the existing IZosBatch instance", zosBatch, zosBatch2);
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zosbatch.ZosBatch(imageTag="TAG")
//        public dev.galasa.zosbatch.IZosBatch zosBatch;
//        @dev.galasa.zosbatch.ZosBatchJobname(imageTag="TAG")
//        public dev.galasa.zosbatch.IZosBatchJobname zosBatchJobname;
//        @dev.galasa.Before
//        public void dummyBeforeMethod() throws ZosBatchException {
//            zosBatch.submitJob("JCL", null);
//        }
//        @dev.galasa.Test
//        public void dummyTestMethod() throws ZosBatchException {
//            zosBatch.submitJob("JCL", null);
//        }
//    }
}
