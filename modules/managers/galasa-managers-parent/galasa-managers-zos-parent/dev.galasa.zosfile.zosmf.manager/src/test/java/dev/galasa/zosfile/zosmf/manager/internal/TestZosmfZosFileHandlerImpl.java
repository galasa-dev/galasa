/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.zosmf.manager.internal;

import java.nio.file.Path;
import java.util.ArrayList;
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

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.internal.ZosManagerImpl;
import dev.galasa.zosfile.ZosDatasetException;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.ZosVSAMDatasetException;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.internal.ZosmfManagerImpl;

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({LogFactory.class})
public class TestZosmfZosFileHandlerImpl {
//    
//    private ZosmfZosFileHandlerImpl zosFileHandler;
//
//    private ZosmfZosFileHandlerImpl zosFileHandlerSpy;
//
//    @Mock
//    private IZosImage zosImageMock;
//
//    @Mock
//    private ZosManagerImpl zosManagerMock;
//
//    @Mock
//    private ZosmfZosFileManagerImpl zosFileManagerMock;
//    
//    @Mock
//    private ZosmfManagerImpl zosmfManagerMock;
//    
//    @Mock
//    private IZosmfRestApiProcessor zosmfApiProcessorMock;
//    
//    @Mock
//    private ZosmfZosDatasetImpl zosDatasetImplMock;
//    
//    @Mock
//    private ZosmfZosVSAMDatasetImpl zosVSAMDatasetImplMock;
//    
//    @Mock
//    private ZosmfZosUNIXFileImpl zosUNIXFileImplMock;
//    
//    @Mock
//    private Log logMock;
//    
//    private static String logMessage;
//
//    private static final String DATASET_NAME = "DATA.SET.NAME";
//
//    private static final String UNIX_FILE_NAME = "/unix/file";
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
//        Mockito.when(zosImageMock.getImageID()).thenReturn("image");
//        Mockito.when(zosManagerMock.getZosFilePropertyFileRestrictToImage(Mockito.any())).thenReturn(true);
//        Mockito.when(zosFileManagerMock.getZosManager()).thenReturn(zosManagerMock);
//        PowerMockito.doReturn(zosmfApiProcessorMock).when(zosmfManagerMock).newZosmfRestApiProcessor(Mockito.any(), Mockito.anyBoolean());
//        Mockito.when(zosFileManagerMock.getZosmfManager()).thenReturn(zosmfManagerMock);
//
//        zosFileHandler = new ZosmfZosFileHandlerImpl(zosFileManagerMock);
//        zosFileHandlerSpy = Mockito.spy(zosFileHandler);
//    }
//    
//    @Test
//    public void testConstructor() {
//        Assert.assertEquals("Method should return expected object", zosFileManagerMock, zosFileHandlerSpy.getZosFileManager());
//        Path pathMock = Mockito.mock(Path.class);
//        Mockito.when(zosFileManagerMock.getArtifactsRoot()).thenReturn(pathMock);
//        Assert.assertEquals("Method should return expected object", pathMock, zosFileHandlerSpy.getArtifactsRoot());
//        Assert.assertEquals("Constructor should return ", zosFileHandler.toString(), new ZosmfZosFileHandlerImpl(zosFileManagerMock, "INTERNAL").toString());
//    }
//    
//    @Test
//    public void testNewDataset() throws ZosDatasetException {
//        Object obj = zosFileHandlerSpy.newDataset(DATASET_NAME, zosImageMock);
//        Assert.assertTrue("Error in newDataset() method", obj instanceof ZosmfZosDatasetImpl);
//    }
//    
//    @Test
//    public void testNewUNIXFile() throws Exception {
//        Object obj = zosFileHandlerSpy.newUNIXFile(UNIX_FILE_NAME, zosImageMock);
//        Assert.assertTrue("Error in newUNIXFile() method", obj instanceof ZosmfZosUNIXFileImpl);
//    }
//    
//    @Test
//    public void testNewVSAMDataset() throws ZosVSAMDatasetException {
//        Object obj = zosFileHandlerSpy.newVSAMDataset(DATASET_NAME, zosImageMock);
//        Assert.assertTrue("Error in newVSAMDataset() method", obj instanceof ZosmfZosVSAMDatasetImpl);
//    }
//    
//    @Test
//    public void testCleanupMethods() throws Exception {
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupDatasets();
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupVsamDatasets();
//        Mockito.doNothing().when(zosFileHandlerSpy).cleanupUnixFiles();
//        zosFileHandlerSpy.cleanup();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets");
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//    }
//    
//    @Test
//    public void testCleanupDatasets() throws Exception {
//        List<ZosmfZosDatasetImpl> zosDatasets = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosDatasetImplMock).exists();
//        zosDatasets.add(zosDatasetImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosDatasets", zosDatasets);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(false).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();        
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosDatasetImplMock).exists(); 
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();       
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupDatasets");
//        
//        Mockito.doReturn(true).when(zosDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosDatasetImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosDatasetImplMock).delete();
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupDatasets");
//        
//        Mockito.doThrow(new ZosDatasetException()).when(zosDatasetImplMock).exists();        
//        zosDatasets.add(zosDatasetImplMock);
//        zosFileHandlerSpy.cleanupDatasets();
//        Assert.assertEquals("cleanupDatasets() should log expected message ", "Problem in data set cleanup phase", logMessage);
//    }
//    
//    @Test
//    public void testCleanupVsamDatasets() throws Exception {
//        List<ZosmfZosVSAMDatasetImpl> zosVsamDatasets = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosVsamDatasets", zosVsamDatasets);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(7)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(8)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(false).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(9)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).created();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).exists();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosVSAMDatasetImplMock).shouldCleanup();
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(10)).invoke("cleanupVsamDatasets");
//        
//        Mockito.doThrow(new ZosVSAMDatasetException()).when(zosVSAMDatasetImplMock).exists();        
//        zosVsamDatasets.add(zosVSAMDatasetImplMock);
//        zosFileHandlerSpy.cleanupVsamDatasets();
//        Assert.assertEquals("cleanupVsamDatasets() should log expected message ", "Problem in VSAM data set cleanup phase", logMessage);
//    }
//    
//    @Test
//    public void testCleanupUnixFiles() throws Exception {
//        List<ZosmfZosUNIXFileImpl> zosUnixFiles = new ArrayList<>();        
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        Whitebox.setInternalState(zosFileHandlerSpy, "zosUnixFiles", zosUnixFiles);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(1)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(2)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(3)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(4)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(5)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(6)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(7)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(8)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(9)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(10)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(11)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(12)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(13)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(14)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(15)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).isDirectory();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(16)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(17)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(18)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).deleted();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).exists();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldArchive();
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).shouldCleanup();
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        PowerMockito.verifyPrivate(zosFileHandlerSpy, Mockito.times(19)).invoke("cleanupUnixFiles");
//
//        Mockito.doReturn(true).when(zosUNIXFileImplMock).created();
//        Mockito.doReturn(false).when(zosUNIXFileImplMock).deleted();
//        Mockito.doThrow(new ZosUNIXFileException()).when(zosUNIXFileImplMock).exists();        
//        zosUnixFiles.add(zosUNIXFileImplMock);
//        zosFileHandlerSpy.cleanupUnixFiles();
//        Assert.assertEquals("cleanupUnixFiles() should log expected message ", "Problem in UNIX file cleanup phase", logMessage);
//    }
}
