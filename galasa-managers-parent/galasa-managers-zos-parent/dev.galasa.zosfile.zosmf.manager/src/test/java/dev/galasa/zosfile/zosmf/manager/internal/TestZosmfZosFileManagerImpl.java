/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.io.File;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;
import dev.galasa.zosunixcommand.ssh.manager.internal.ZosUNIXCommandManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosFileManagerImpl {
//
//	private ZosmfZosFileManagerImpl zosFileManager;
//    
//    private ZosmfZosFileManagerImpl zosFileManagerSpy;
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
//    private ZosUNIXCommandManagerImpl zosUNIXCommandManagerMock;
//
//    @Mock
//    private IZosImage zosImageMock;
//    
//    private static final Path ARTIFACT_ROOT = new File(".").toPath();
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
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        
//        zosFileManager = new ZosmfZosFileManagerImpl();
//        zosFileManagerSpy = Mockito.spy(zosFileManager);
//        Mockito.when(zosFileManagerSpy.getFramework()).thenReturn(frameworkMock);        
//        Mockito.when(zosFileManagerSpy.getZosManager()).thenReturn(zosManagerMock);
//        Mockito.when(zosFileManagerSpy.getZosmfManager()).thenReturn(zosmfManagerMock);
//        Mockito.when(frameworkMock.getResultArchiveStore()).thenReturn(resultArchiveStoreMock);
//        Mockito.when(resultArchiveStoreMock.getStoredArtifactsRoot()).thenReturn(ARTIFACT_ROOT);
//        
//        allManagers = new ArrayList<>();
//        activeManagers = new ArrayList<>();
//    }
//    
//    @Test
//    public void testInitialise() throws ManagerException {
//        allManagers.add(managerMock);
//        zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(TestZosmfZosFileManagerImpl.class));
//        Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
//        
//        Assert.assertEquals("Error in getArtifactsRoot() should return the expected value", ARTIFACT_ROOT, zosFileManagerSpy.getArtifactsRoot());
//        
//        zosFileManagerSpy.setCurrentTestMethodArchiveFolderName("");
//        Assert.assertEquals("Error in getDatasetCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_Datasets"), zosFileManagerSpy.getDatasetCurrentTestMethodArchiveFolder());
//        Assert.assertEquals("Error in getVsamDatasetCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_VSAM_Datasets"), zosFileManagerSpy.getVsamDatasetCurrentTestMethodArchiveFolder());
//        Assert.assertEquals("Error in getUnixPathCurrentTestMethodArchiveFolder() should return the expected value", ARTIFACT_ROOT.resolve("zOS_Unix_Paths"), zosFileManagerSpy.getUnixPathCurrentTestMethodArchiveFolder());
//    }
//    
//    @Test
//    public void testInitialise1() throws ManagerException {
//        Mockito.doNothing().when(zosFileManagerSpy).youAreRequired(Mockito.any(), Mockito.any(), Mockito.any());
//        zosFileManagerSpy.initialise(frameworkMock, allManagers, activeManagers, new GalasaTest(DummyTestClass.class));
//        Assert.assertEquals("Error in initialise() method", zosFileManagerSpy.getFramework(), frameworkMock);
//    }
//    
//    @Test
//    public void testProvisionGenerate() throws Exception {
//        PowerMockito.doNothing().when(zosFileManagerSpy, "generateAnnotatedFields", Mockito.any());
//        zosFileManagerSpy.provisionGenerate();
//        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(1)).invoke("generateAnnotatedFields", Mockito.any());
//    }
//    
//    @Test
//    public void testYouAreRequired() throws Exception {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosmfManagerMock);
//        allManagers.add(zosUNIXCommandManagerMock);
//        zosFileManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(3)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Mockito.clearInvocations(zosFileManagerSpy);        
//        zosFileManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        PowerMockito.verifyPrivate(zosFileManagerSpy, Mockito.times(0)).invoke("addDependentManager", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
//        
//        Assert.assertEquals("Method should return expected object", zosUNIXCommandManagerMock, zosFileManagerSpy.getZosUnixCommandManager());
//    }
//    
//    @Test
//    public void testYouAreRequiredException1() throws ManagerException {
//        allManagers.add(zosmfManagerMock);
//        allManagers.add(zosUNIXCommandManagerMock);
//        String expectedMessage = "The zOS Manager is not available";
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
//        	zosFileManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException2() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosUNIXCommandManagerMock);
//        allManagers.add(zosManagerMock);
//        String expectedMessage = "The zOSMF Manager is not available";
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
//        	zosFileManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testYouAreRequiredException3() throws ManagerException {
//        allManagers.add(zosManagerMock);
//        allManagers.add(zosmfManagerMock);
//        String expectedMessage = "The zOS UNIX Command Manager is not available";
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
//        	zosFileManagerSpy.youAreRequired(allManagers, activeManagers, null);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
//    }
//    
//    @Test
//    public void testAreYouProvisionalDependentOn() {
//        Assert.assertTrue("Should be dependent on IZosManagerSpi" , zosFileManagerSpy.areYouProvisionalDependentOn(zosManagerMock));
//        Assert.assertTrue("Should be dependent on IZosmfManagerSpi" , zosFileManagerSpy.areYouProvisionalDependentOn(zosmfManagerMock));
//        Assert.assertFalse("Should not be dependent on IManager" , zosFileManagerSpy.areYouProvisionalDependentOn(managerMock));
//    }
//
//    @Test
//    public void testProvisionBuild() throws Exception {
//        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
//        zosFileManagerSpy.provisionBuild();
//        Assert.assertEquals("datasetArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_Datasets"), zosFileManagerSpy.datasetArtifactRoot);
//        Assert.assertEquals("vsamDatasetArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_VSAM_Datasets"), zosFileManagerSpy.vsamDatasetArtifactRoot);
//        Assert.assertEquals("unixPathArtifactRoot should contain the supplied value", new File(".").toPath().resolve("provisioning").resolve("zOS_Unix_Paths"), zosFileManagerSpy.unixPathArtifactRoot);
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", "preTest", zosFileManagerSpy.currentTestMethodArchiveFolderName);
//    }
//    
//    @Test
//    public void testStartOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
//        Mockito.doNothing().when(zosFileManagerSpy).cleanup();
//        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
//        zosFileManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset"), null));
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", "dummyTestMethodDataset", zosFileManagerSpy.currentTestMethodArchiveFolderName);
//        
//        zosFileManagerSpy.startOfTestMethod(new GalasaMethod(DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod"), DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset")));
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName() + "." + DummyTestClass.class.getDeclaredMethod("dummyBeforeMethod").getName(), zosFileManagerSpy.currentTestMethodArchiveFolderName);
//    }
//    
//    @Test
//    public void testEndOfTestMethod() throws NoSuchMethodException, SecurityException, ManagerException {
//        Mockito.doNothing().when(zosFileManagerSpy).cleanup();
//        zosFileManagerSpy.setCurrentTestMethodArchiveFolderName("dummyTestMethodDataset");
//        zosFileManagerSpy.endOfTestMethod(new GalasaMethod(null, DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset")), "pass", null);
//        Assert.assertEquals("currentTestMethodArchiveFolderName should contain the supplied value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName(), zosFileManagerSpy.currentTestMethodArchiveFolderName);
//    }
//    
//    @Test
//    public void testEndOfTestClass() throws Exception {
//        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
//        Whitebox.setInternalState(zosFileManagerSpy, "zosFileHandlers", new HashMap<>());
//        zosFileManagerSpy.endOfTestClass("pass", null);
//        Assert.assertEquals("currentTestMethodArchiveFolderName should be should be set to the expected value", "postTest", zosFileManagerSpy.currentTestMethodArchiveFolderName);
//    }
//    
//    @Test
//    public void testCleanup() throws Exception {
//        zosFileManagerSpy.cleanup();
//        zosFileManagerSpy.cleanup();
//
//        Map<String, ZosmfZosFileHandlerImpl> zosFileHandlers = new HashMap<>();
//        ZosmfZosFileHandlerImpl zosmfZosFileHandlerImplMock = Mockito.mock(ZosmfZosFileHandlerImpl.class);
//        Mockito.doNothing().when(zosmfZosFileHandlerImplMock).cleanup();
//        zosFileHandlers.put(zosmfZosFileHandlerImplMock.toString(), zosmfZosFileHandlerImplMock);
//        Whitebox.setInternalState(zosFileManagerSpy, "zosFileHandlers", zosFileHandlers);
//        
//        zosFileManagerSpy.cleanup();
//        Mockito.verify(zosFileManagerSpy, Mockito.atLeast(1)).cleanup();
//        
//        Mockito.clearInvocations(zosFileManagerSpy);
//        zosFileHandlers = new HashMap<>();
//        Mockito.doNothing().when(zosFileManagerSpy).cleanup();
//        zosFileHandlers.put(zosmfZosFileHandlerImplMock.toString(), zosmfZosFileHandlerImplMock);
//        Whitebox.setInternalState(zosFileManagerSpy, "zosFileHandlers", zosFileHandlers);
//        
//        zosFileManagerSpy.cleanup();
//        Mockito.verify(zosmfZosFileHandlerImplMock, Mockito.atLeast(1)).cleanup();
//    }
//    
//    @Test
//    public void testEndOfTestRun() throws NoSuchMethodException, SecurityException, ManagerException {
//        Whitebox.setInternalState(zosFileManagerSpy, "artifactsRoot", new File(".").toPath());
//        Mockito.doNothing().when(zosFileManagerSpy).cleanup();
//        zosFileManagerSpy.setCurrentTestMethodArchiveFolderName(DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName());
//        zosFileManagerSpy.endOfTestRun();
//        Assert.assertEquals("currentTestMethodArchiveFolderName should be expeacted value", DummyTestClass.class.getDeclaredMethod("dummyTestMethodDataset").getName(), zosFileManagerSpy.currentTestMethodArchiveFolderName);
//
//        Mockito.doThrow(new ZosFileManagerException()).when(zosFileManagerSpy).cleanup();
//        zosFileManagerSpy.endOfTestRun();
//        Assert.assertEquals("testEndOfTestRun() should log expected message", "Problem in endOfTestRun()", logMessage);
//    }
//    
//    @Test
//    public void testGenerateZosFileHandler() throws NoSuchMethodException, SecurityException, ManagerException, NoSuchFieldException {
//        List<Annotation> annotations = new ArrayList<>();
//        Annotation annotation = DummyTestClass.class.getAnnotation(dev.galasa.zosfile.ZosFileHandler.class);
//        annotations.add(annotation);
//        
//        Object zosFileHandlerObject = zosFileManagerSpy.generateZosFileHandler(DummyTestClass.class.getDeclaredField("zosFileHandler"), annotations);
//        Assert.assertTrue("Error in generateZosFileHandler() method", zosFileHandlerObject instanceof ZosmfZosFileHandlerImpl);
//    }
//    
//    @Test
//    public void testRunid() {
//        zosFileManagerSpy.setRunId("RUNID");
//        Assert.assertEquals("getRunid() should return the supplied value", "RUNID", zosFileManagerSpy.getRunId());
//    }
//    
//    @Test
//    public void testDatasetArtifactRoot() {
//        Path path = new File("datasetArtifactRoot").toPath();
//        zosFileManagerSpy.setDatasetArtifactRoot(path);
//        Assert.assertEquals("getDatasetArtifactRoot() should return the supplied value", path, zosFileManagerSpy.getDatasetArtifactRoot());
//    }
//    
//    @Test
//    public void testVsamDatasetArtifactRoot() {
//        Path path = new File("vsamDatasetArtifactRoot").toPath();
//        zosFileManagerSpy.setVsamDatasetArtifactRoot(path);
//        Assert.assertEquals("getVsamDatasetArtifactRoot() should return the supplied value", path, zosFileManagerSpy.getVsamDatasetArtifactRoot());
//    }
//    
//    @Test
//    public void testUnixPathArtifactRoot() {
//        Path path = new File("unixPathArtifactRoot").toPath();
//        zosFileManagerSpy.setUnixPathArtifactRoot(path);
//        Assert.assertEquals("getUnixPathArtifactRoot() should return the supplied value", path, zosFileManagerSpy.getUnixPathArtifactRoot());
//    }
//    
//    @Test
//    public void testNewZosFileHandler() {
//        Assert.assertNotNull("newZosFileHandler() should return a new ZosmfZosFileHandlerImpl", zosFileManagerSpy.newZosFileHandler());
//    }
//    
//    @Test
//    public void testGetRunDatasetHLQ() throws ZosManagerException {
//        Whitebox.setInternalState(zosFileManagerSpy, "zosManager", zosManagerMock); 
//        Mockito.when(zosManagerMock.getRunDatasetHLQ(Mockito.any())).thenReturn("HLQ");
//        
//        Assert.assertEquals("getRunDatasetHLQ() should return the supplied value", "HLQ", zosFileManagerSpy.getRunDatasetHLQ(zosImageMock));
//    }
//    
//    @Test
//    public void testGetRunDatasetHLQException() throws ZosManagerException {
//        Whitebox.setInternalState(zosFileManagerSpy, "zosManager", zosManagerMock); 
//        Mockito.when(zosFileManagerSpy.getZosManager().getRunDatasetHLQ(Mockito.any())).thenThrow(new ZosManagerException("exception"));
//        String expectedMessage = "exception";
//        ZosFileManagerException expectedException = Assert.assertThrows("expected exception should be thrown", ZosFileManagerException.class, ()->{
//        	zosFileManagerSpy.getRunDatasetHLQ(zosImageMock);
//        });
//        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getCause().getMessage());
//    }
//    
//    @Test
//    public void testGetZosFileHandler() throws ZosFileManagerException {
//        Assert.assertEquals("getZosFileHandler() should return the same IZosFileHandler", zosFileManagerSpy.getZosFileHandler(), zosFileManagerSpy.getZosFileHandler());
//    }
//    
//    class DummyTestClass {
//        @dev.galasa.zos.ZosImage
//        public dev.galasa.zos.IZosImage zosImage;
//        @dev.galasa.zosfile.ZosFileHandler
//        public dev.galasa.zosfile.IZosFileHandler zosFileHandler;
//        @dev.galasa.Before
//        public void dummyBeforeMethod() throws ZosDatasetException {
//            zosFileHandler.newDataset("dsname", zosImage);
//        }
//        @dev.galasa.Test
//        public void dummyTestMethodDataset() throws ZosDatasetException {
//            zosFileHandler.newDataset("dsname", zosImage);
//        }
//        @dev.galasa.Test
//        public void dummyTestMethodUNIXFile() throws ZosUNIXFileException {
//            zosFileHandler.newUNIXFile("filePath", zosImage);
//        }
//        @dev.galasa.Test
//        public void dummyTestMethodVSAMDataset() throws ZosVSAMDatasetException {
//            zosFileHandler.newVSAMDataset("dsname", zosImage);
//        }
//    }
}
